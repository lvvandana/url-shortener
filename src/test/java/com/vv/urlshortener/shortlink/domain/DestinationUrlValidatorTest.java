package com.vv.urlshortener.shortlink.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;

import org.junit.jupiter.api.Test;

class DestinationUrlValidatorTest {

    private final DestinationUrlValidator validator = new DestinationUrlValidator();

    @Test
    void acceptsAbsoluteHttpsUrl() {
        URI normalized = validator.validateAndNormalize("https://example.com/page?q=1");
        assertEquals(URI.create("https://example.com/page?q=1"), normalized);
    }

    @Test
    void rejectsNullOrBlank() {
        assertThrows(InvalidUrlException.class, () -> validator.validateAndNormalize(null));
        assertThrows(InvalidUrlException.class, () -> validator.validateAndNormalize("   "));
    }

    @Test
    void rejectsUnsupportedScheme() {
        assertThrows(InvalidUrlException.class, () -> validator.validateAndNormalize("ftp://example.com"));
    }

    @Test
    void rejectsRelativeUrl() {
        assertThrows(InvalidUrlException.class, () -> validator.validateAndNormalize("/path-only"));
    }
}
