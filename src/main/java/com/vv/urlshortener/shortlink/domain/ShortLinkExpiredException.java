package com.vv.urlshortener.shortlink.domain;

public final class ShortLinkExpiredException extends RuntimeException {
    public ShortLinkExpiredException(String message) { super(message); }
}
