package com.vv.urlshortener.shortlink.application;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.transaction.annotation.Transactional;

import com.vv.urlshortener.shortlink.domain.ShortLinkNotFoundException;
import com.vv.urlshortener.shortlink.persistence.ShortLinkRepository;
import com.vv.urlshortener.shortlink.persistence.ShortLinkEntity;

@Service
public class RedirectLookupService {

    private final ShortLinkRepository shortLinkRepository;
    private final Clock clock;

    public RedirectLookupService(ShortLinkRepository shortLinkRepository, ObjectProvider<Clock> clockProvider) {
        this.shortLinkRepository = shortLinkRepository;
        this.clock = clockProvider.getIfAvailable(Clock::systemUTC);
    }

    @Transactional(readOnly = true)
    public URI findActiveDestination(String code) {
        return shortLinkRepository.findByCodeAndEnabledTrue(code)
                .map(e -> {
                    Instant expires = e.getExpiresAt();
                    if (expires != null && !expires.isAfter(Instant.now(clock))) {
                        throw new com.vv.urlshortener.shortlink.domain.ShortLinkExpiredException("Short link expired");
                    }
                    return URI.create(e.getOriginalUrl());
                })
                .orElseThrow(() -> new ShortLinkNotFoundException("Short link not found"));
    }

    @Transactional(readOnly = true)
    public ShortLinkEntity findActiveShortLink(String code) {
        return shortLinkRepository.findByCodeAndEnabledTrue(code)
                .map(e -> {
                    Instant expires = e.getExpiresAt();
                    if (expires != null && !expires.isAfter(Instant.now(clock))) {
                        throw new com.vv.urlshortener.shortlink.domain.ShortLinkExpiredException("Short link expired");
                    }
                    return e;
                })
                .orElseThrow(() -> new ShortLinkNotFoundException("Short link not found"));
    }
}
