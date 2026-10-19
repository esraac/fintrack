package com.fintrack.backend.auth.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegisterRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validRequest_hasNoViolations() {
        assertTrue(validator.validate(new RegisterRequest("esra@test.com", "Sifre1234")).isEmpty());
    }

    @Test
    void invalidEmail_isRejected() {
        assertFalse(validator.validate(new RegisterRequest("email-degil", "Sifre1234")).isEmpty());
    }

    @Test
    void shortPassword_isRejected() {
        assertFalse(validator.validate(new RegisterRequest("esra@test.com", "kisa")).isEmpty());
    }

    @Test
    void tooLongPassword_isRejected() {
        assertFalse(validator.validate(new RegisterRequest("esra@test.com", "a".repeat(73))).isEmpty());
    }

    @Test
    void blankFields_areRejected() {
        assertFalse(validator.validate(new RegisterRequest("", "")).isEmpty());
    }
}
