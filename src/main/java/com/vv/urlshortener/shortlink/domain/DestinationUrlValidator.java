package com.vv.urlshortener.shortlink.domain;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import org.springframework.stereotype.Component;

@Component
public final class DestinationUrlValidator {

    private static final int MAX_LENGTH = 2048;

    public URI validateAndNormalize(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            throw new InvalidUrlException("URL must not be null or blank");
        }
        String candidate = rawUrl.trim();
        if (candidate.length() > MAX_LENGTH) {
            throw new InvalidUrlException("URL must not exceed 2048 characters");
        }
        URI uri;
        try {
            uri = new URI(candidate);
        } catch (URISyntaxException ex) {
            throw new InvalidUrlException("URL syntax is invalid", ex);
        }
        if (!uri.isAbsolute()) {
            throw new InvalidUrlException("URL must be absolute");
        }
        String scheme = uri.getScheme();
        if (scheme == null) {
            throw new InvalidUrlException("URL scheme is required");
        }
        String s = scheme.toLowerCase(Locale.ROOT);
        if (!"http".equals(s) && !"https".equals(s)) {
            throw new InvalidUrlException("Only HTTP and HTTPS URLs are supported");
        }
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new InvalidUrlException("URL host is required");
        }
        return uri.normalize();
    }
}
