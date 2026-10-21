package com.fintrack.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter; //aynı istek için yalnızca bir kez çalışma

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith(BEARER_PREFIX)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = header.substring(BEARER_PREFIX.length());
            if (jwtService.isTokenValid(token)) {
                try {
                    AuthenticatedUser principal = new AuthenticatedUser(
                            jwtService.extractUserId(token),
                            jwtService.extractEmail(token));
                    var authentication = new UsernamePasswordAuthenticationToken(
                            principal, null, List.of());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (IllegalArgumentException | NullPointerException e) {
                    // uid claim'i bozuk: kimlik atama, istek anonim devam eder
                }
            }
        }
        chain.doFilter(request, response);
    }
}