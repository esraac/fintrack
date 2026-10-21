package com.fintrack.backend.security;

import com.fintrack.backend.user.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static String newSecret() {
        return Encoders.BASE64.encode(Jwts.SIG.HS256.key().build().getEncoded());
    }

    private final User user = createUser();

    private static User createUser() {
        User u = new User("esra@test.com", "hash");
        ReflectionTestUtils.setField(u, "id", UUID.randomUUID());
        return u;
    }

    @Test
    void generatedToken_isValid_andContainsEmail() {
        JwtService service = new JwtService(newSecret(), 60_000);
        String token = service.generateToken(user);

        assertTrue(service.isTokenValid(token));
        assertEquals("esra@test.com", service.extractEmail(token));
    }

    @Test
    void expiredToken_isInvalid() {
        JwtService service = new JwtService(newSecret(), -1_000);
        assertFalse(service.isTokenValid(service.generateToken(user)));
    }

    @Test
    void tokenSignedWithOtherSecret_isInvalid() {
        String token = new JwtService(newSecret(), 60_000).generateToken(user);
        JwtService other = new JwtService(newSecret(), 60_000);
        assertFalse(other.isTokenValid(token));
    }

    @Test
    void garbageToken_isInvalid() {
        JwtService service = new JwtService(newSecret(), 60_000);
        assertFalse(service.isTokenValid("bu.bir.token.degil"));
    }

    @Test
    void extractUserId_returnsIdFromToken() {
        JwtService service = new JwtService(newSecret(), 60_000);
        String token = service.generateToken(user);

        assertEquals(user.getId(), service.extractUserId(token));
    }
}