package com.vv.urlshortener.shortlink.domain;

public final class ShortLinkNotFoundException extends RuntimeException {
    public ShortLinkNotFoundException(String message) { super(message); }
}
