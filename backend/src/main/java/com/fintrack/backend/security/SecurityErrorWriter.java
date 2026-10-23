package com.fintrack.backend.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

final class SecurityErrorWriter {

    private SecurityErrorWriter() {}

    static void write(HttpServletResponse response, HttpStatus status,
                      String code, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(String.format(
                "{\"code\":\"%s\",\"message\":\"%s\",\"timestamp\":\"%s\"}",
                code, message, Instant.now()));
    }
}