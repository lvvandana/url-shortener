package com.vv.urlshortener.shortlink.application;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.vv.urlshortener.shortlink.api.dto.CreateShortLinkRequest;
import com.vv.urlshortener.shortlink.api.dto.ShortLinkResponse;
import com.vv.urlshortener.shortlink.domain.AliasAlreadyExistsException;
import com.vv.urlshortener.shortlink.domain.InvalidExpirationException;
import com.vv.urlshortener.shortlink.domain.DestinationUrlValidator;
import com.vv.urlshortener.shortlink.domain.ShortCodeGenerationException;
import com.vv.urlshortener.shortlink.domain.ShortCodeGenerator;
import com.vv.urlshortener.shortlink.persistence.ShortLinkEntity;
import com.vv.urlshortener.shortlink.persistence.ShortLinkRepository;

@Service
public class ShortLinkService {

    static final int MAX_GENERATION_ATTEMPTS = 5;

    private final ShortLinkRepository shortLinkRepository;
    private final ShortCodeGenerator shortCodeGenerator;
    private final DestinationUrlValidator destinationUrlValidator;
    private final Clock clock;
    private final String shortBaseUrl;

    public ShortLinkService(
            ShortLinkRepository shortLinkRepository,
            ShortCodeGenerator shortCodeGenerator,
            DestinationUrlValidator destinationUrlValidator,
            ObjectProvider<Clock> clockProvider,
            @Value("${app.short-link.base-url:http://localhost:8080}") String shortBaseUrl
    ) {
        this.shortLinkRepository = shortLinkRepository;
        this.shortCodeGenerator = shortCodeGenerator;
        this.destinationUrlValidator = destinationUrlValidator;
        this.clock = clockProvider.getIfAvailable(Clock::systemUTC);
        this.shortBaseUrl = shortBaseUrl;
    }

    @Transactional
    public ShortLinkResponse create(CreateShortLinkRequest request) {
        URI normalizedDestination = destinationUrlValidator.validateAndNormalize(request.originalUrl());
        Instant now = Instant.now(clock);

        String customAlias = normalizeAlias(request.customAlias());
        // parse optional expiresAt (ISO-8601) from request
        Instant expires = null;
        if (request.expiresAt() != null && !request.expiresAt().isBlank()) {
            try {
                expires = Instant.parse(request.expiresAt());
            } catch (java.time.format.DateTimeParseException ex) {
                throw new InvalidExpirationException("expiresAt must be a valid ISO-8601 timestamp", ex);
            }
            if (!expires.isAfter(now)) {
                throw new InvalidExpirationException("expiresAt must be in the future");
            }
        }

        if (customAlias != null) {
            return createWithCustomAlias(customAlias, normalizedDestination, now, expires);
        }

        return createWithGeneratedCode(normalizedDestination, now, expires);
    }

    private ShortLinkResponse createWithCustomAlias(String alias, URI destination, Instant now, Instant expires) {
        if (shortLinkRepository.existsByCode(alias)) {
            throw new AliasAlreadyExistsException("Custom alias is already in use");
        }

        ShortLinkEntity entity = new ShortLinkEntity();
        entity.setCode(alias);
        entity.setOriginalUrl(destination.toString());
        entity.setCreatedAt(now);
        entity.setExpiresAt(expires);
        entity.setEnabled(true);

        try {
            ShortLinkEntity saved = shortLinkRepository.saveAndFlush(entity);
            return toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            if (isCodeUniquenessViolation(ex)) {
                throw new AliasAlreadyExistsException("Custom alias is already in use");
            }
            throw ex;
        }
    }

    private ShortLinkResponse createWithGeneratedCode(URI destination, Instant now, Instant expires) {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String generatedCode = shortCodeGenerator.generate(ShortCodeGenerator.DEFAULT_LENGTH);
            ShortLinkEntity entity = new ShortLinkEntity();
            entity.setCode(generatedCode);
            entity.setOriginalUrl(destination.toString());
            entity.setCreatedAt(now);
            entity.setExpiresAt(expires);
            entity.setEnabled(true);

            try {
                ShortLinkEntity saved = shortLinkRepository.saveAndFlush(entity);
                return toResponse(saved);
            } catch (DataIntegrityViolationException ex) {
                if (!isCodeUniquenessViolation(ex)) {
                    throw ex;
                }
            }
        }

        throw new ShortCodeGenerationException("Unable to generate a unique short code after 5 attempts");
    }

    private ShortLinkResponse toResponse(ShortLinkEntity entity) {
        String shortUrl = shortBaseUrl.endsWith("/")
                ? shortBaseUrl + entity.getCode()
                : shortBaseUrl + "/" + entity.getCode();

        return new ShortLinkResponse(
                entity.getCode(),
                shortUrl,
                entity.getOriginalUrl(),
                entity.getCreatedAt(),
                entity.getExpiresAt()
        );
    }

    private String normalizeAlias(String alias) {
        if (!StringUtils.hasText(alias)) {
            return null;
        }
        return alias.trim();
    }

    private boolean isCodeUniquenessViolation(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();

        if (message == null) {
            return false;
        }

        String normalized = message.toLowerCase();
        return normalized.contains("unique")
                && (normalized.contains("code")
                || normalized.contains("short_links_code_key")
                || normalized.contains("uq_short_links_code")
                || normalized.contains("uq_short_links_code"));
    }
}
