package com.fintrack.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class PasswordEncoderTest {

    @Test
    void sameRawPassword_producesDifferentHashes_bothMatch() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String h1 = encoder.encode("Sifre1234");
        String h2 = encoder.encode("Sifre1234");

        assertNotEquals(h1, h2);
        assertTrue(encoder.matches("Sifre1234", h1));
        assertTrue(encoder.matches("Sifre1234", h2));
    }
}