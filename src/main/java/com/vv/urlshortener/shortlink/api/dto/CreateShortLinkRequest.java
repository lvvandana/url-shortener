package com.vv.urlshortener.shortlink.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateShortLinkRequest(
        @NotBlank(message = "originalUrl is required")
        String originalUrl,
        @Size(min = 4, max = 32, message = "customAlias length must be between 4 and 32")
        @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "customAlias must contain only letters, numbers, hyphen and underscore")
        String customAlias,
        // Optional ISO-8601 timestamp, validated in service layer
        String expiresAt
) {
}
