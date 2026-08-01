package com.vv.urlshortener.shortlink.api;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.vv.urlshortener.shortlink.application.RedirectLookupService;
import com.vv.urlshortener.shortlink.domain.ShortLinkNotFoundException;
import com.vv.urlshortener.shortlink.persistence.ShortLinkEntity;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@RestController
@Validated
public class RedirectController {

    private final RedirectLookupService redirectLookupService;
    private final com.vv.urlshortener.shortlink.service.AnalyticsService analyticsService;

    public RedirectController(RedirectLookupService redirectLookupService,
                              com.vv.urlshortener.shortlink.service.AnalyticsService analyticsService) {
        this.redirectLookupService = redirectLookupService;
        this.analyticsService = analyticsService;
    }

    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(
            @PathVariable
            @Size(min = 1, max = 32, message = "code length must be between 1 and 32")
            @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "code contains unsupported characters")
            String code
    ) {
        try {
            // resolve the full short link entity so we can record analytics
            var entity = redirectLookupService.findActiveShortLink(code);
            // record analytics if AnalyticsService is available (in tests it may be null)
            if (analyticsService != null) {
                analyticsService.recordClick(entity.getId());
            }
            URI destination = URI.create(entity.getOriginalUrl());
            return ResponseEntity.status(HttpStatus.FOUND).location(destination).build();
        } catch (ShortLinkNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Short link not found");
        }
    }
}
