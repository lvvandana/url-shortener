package com.vv.urlshortener.shortlink.domain;

public final class InvalidUrlException extends RuntimeException {
    public InvalidUrlException(String message) { super(message); }
    public InvalidUrlException(String message, Throwable cause) { super(message, cause); }
}
