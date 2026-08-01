package com.vv.urlshortener.shortlink.api.dto;

import java.time.Instant;
import org.springframework.lang.Nullable;

public record ShortLinkResponse(
        String code,
        String shortUrl,
        String originalUrl,
        Instant createdAt,
        @Nullable Instant expiresAt
) {
}
