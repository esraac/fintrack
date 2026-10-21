package com.fintrack.backend.security;

import com.fintrack.backend.user.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtAuthFilterTest {

    private final String secret =
            Encoders.BASE64.encode(Jwts.SIG.HS256.key().build().getEncoded());
    private final JwtService jwtService = new JwtService(secret, 60_000);
    private final JwtAuthFilter filter = new JwtAuthFilter(jwtService);
    private final User user = createUser();

    private static User createUser() {
        User u = new User("esra@test.com", "hash");
        ReflectionTestUtils.setField(u, "id", UUID.randomUUID());
        return u;
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private MockFilterChain run(String authorizationHeader) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (authorizationHeader != null) {
            request.addHeader("Authorization", authorizationHeader);
        }
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, new MockHttpServletResponse(), chain);
        return chain;
    }

    @Test
    void validToken_setsAuthenticationWithPrincipal() throws Exception {
        String token = jwtService.generateToken(user);

        MockFilterChain chain = run("Bearer " + token);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        AuthenticatedUser principal = (AuthenticatedUser) auth.getPrincipal();
        assertEquals(user.getId(), principal.id());
        assertEquals("esra@test.com", principal.email());
        assertNotNull(chain.getRequest(), "zincir devam etmeli");
    }

    @Test
    void noHeader_leavesRequestAnonymous_andContinuesChain() throws Exception {
        MockFilterChain chain = run(null);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(chain.getRequest());
    }

    @Test
    void nonBearerHeader_isIgnored() throws Exception {
        run("Basic dXNlcjpwYXNz");

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void garbageToken_isIgnored_andChainContinues() throws Exception {
        MockFilterChain chain = run("Bearer bu.bir.token.degil");

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(chain.getRequest());
    }

    @Test
    void expiredToken_isIgnored() throws Exception {
        String expired = new JwtService(secret, -1_000).generateToken(user);

        run("Bearer " + expired);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}