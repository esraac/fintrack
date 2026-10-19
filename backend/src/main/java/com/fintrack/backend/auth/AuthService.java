package com.fintrack.backend.auth;

import com.fintrack.backend.auth.dto.AuthResponse;
import com.fintrack.backend.auth.dto.LoginRequest;
import com.fintrack.backend.auth.dto.RegisterRequest;
import com.fintrack.backend.exception.EmailAlreadyExistsException;
import com.fintrack.backend.exception.InvalidCredentialsException;
import com.fintrack.backend.security.JwtService;
import com.fintrack.backend.user.User;
import com.fintrack.backend.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalize(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException();
        }
        User user = userRepository.save(
                new User(email, passwordEncoder.encode(request.password())));
        return new AuthResponse(jwtService.generateToken(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalize(request.email());
        User user = userRepository.findByEmail(email)
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(InvalidCredentialsException::new);
        return new AuthResponse(jwtService.generateToken(user));
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}