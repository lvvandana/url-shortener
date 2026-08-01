package com.vv.urlshortener.shortlink.domain;

public final class InvalidExpirationException extends RuntimeException {
    public InvalidExpirationException(String message) { super(message); }
    public InvalidExpirationException(String message, Throwable cause) { super(message, cause); }
}
