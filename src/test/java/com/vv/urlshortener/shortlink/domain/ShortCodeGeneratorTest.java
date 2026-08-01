package com.vv.urlshortener.shortlink.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class ShortCodeGeneratorTest {

    private static final String ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";

    private final ShortCodeGenerator generator = new ShortCodeGenerator();

    @Test
    void generate_usesDefaultLength() {
        String code = generator.generate();
        assertEquals(ShortCodeGenerator.DEFAULT_LENGTH, code.length());
    }

    @Test
    void generate_containsOnlySupportedCharacters() {
        Set<Character> allowed = toCharacterSet(ALPHABET);
        for (int i = 0; i < 200; i++) {
            String code = generator.generate();
            for (char c : code.toCharArray()) {
                assertTrue(allowed.contains(c), "Unsupported character: " + c);
            }
        }
    }

    @Test
    void generate_multipleValuesAreNotAllIdentical() {
        String first = generator.generate();
        boolean allMatchFirst = true;
        for (int i = 0; i < 50; i++) {
            if (!first.equals(generator.generate())) {
                allMatchFirst = false;
                break;
            }
        }
        assertFalse(allMatchFirst, "All generated values were identical");
    }

    @Test
    void generate_rejectsNonPositiveLength() {
        assertThrows(IllegalArgumentException.class, () -> generator.generate(0));
        assertThrows(IllegalArgumentException.class, () -> generator.generate(-1));
    }

    private static Set<Character> toCharacterSet(String alphabet) {
        Set<Character> characters = new HashSet<>();
        for (char c : alphabet.toCharArray()) {
            characters.add(c);
        }
        return characters;
    }
}
