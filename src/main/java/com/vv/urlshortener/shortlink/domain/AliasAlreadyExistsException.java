package com.vv.urlshortener.shortlink.domain;

public final class AliasAlreadyExistsException extends RuntimeException {
    public AliasAlreadyExistsException(String message) { super(message); }
}
