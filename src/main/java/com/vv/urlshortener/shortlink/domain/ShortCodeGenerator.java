package com.vv.urlshortener.shortlink.domain;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

/**
 * Generates random short codes using an unambiguous alphanumeric alphabet.
 * Collision handling is the responsibility of the application service and the
 * database unique constraint.
 */
@Component
public final class ShortCodeGenerator {

    public static final int DEFAULT_LENGTH = 7;

    private static final char[] ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();

    private static final ThreadLocal<SecureRandom> RANDOM = ThreadLocal.withInitial(SecureRandom::new);

    public String generate() {
        return generate(DEFAULT_LENGTH);
    }

    public String generate(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("length must be positive");
        }
        char[] c = new char[length];
        SecureRandom rnd = RANDOM.get();
        for (int i = 0; i < length; i++) {
            c[i] = ALPHABET[rnd.nextInt(ALPHABET.length)];
        }
        return new String(c);
    }
}
