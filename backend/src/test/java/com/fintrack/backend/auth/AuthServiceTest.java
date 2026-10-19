package com.fintrack.backend.auth;

import com.fintrack.backend.auth.dto.AuthResponse;
import com.fintrack.backend.auth.dto.LoginRequest;
import com.fintrack.backend.auth.dto.RegisterRequest;
import com.fintrack.backend.exception.EmailAlreadyExistsException;
import com.fintrack.backend.exception.InvalidCredentialsException;
import com.fintrack.backend.security.JwtService;
import com.fintrack.backend.user.User;
import com.fintrack.backend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @InjectMocks AuthService authService;

    @Test
    void register_savesUserWithHashedPassword_andNormalizedEmail() {
        when(userRepository.existsByEmail("esra@test.com")).thenReturn(false);
        when(passwordEncoder.encode("Sifre1234")).thenReturn("HASH");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthResponse response = authService.register(new RegisterRequest("  Esra@Test.com ", "Sifre1234"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("esra@test.com", captor.getValue().getEmail());
        assertEquals("HASH", captor.getValue().getPasswordHash());
        assertEquals("jwt-token", response.token());
    }

    @Test
    void register_duplicateEmail_throws_andDoesNotSave() {
        when(userRepository.existsByEmail("esra@test.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class,
                () -> authService.register(new RegisterRequest("esra@test.com", "Sifre1234")));

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_correctPassword_returnsToken() {
        User user = new User("esra@test.com", "HASH");
        when(userRepository.findByEmail("esra@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Sifre1234", "HASH")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.login(new LoginRequest("esra@test.com", "Sifre1234"));

        assertEquals("jwt-token", response.token());
    }

    @Test
    void login_wrongPassword_throws_andIssuesNoToken() {
        User user = new User("esra@test.com", "HASH");
        when(userRepository.findByEmail("esra@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("yanlis", "HASH")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginRequest("esra@test.com", "yanlis")));

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_unknownEmail_throws() {
        when(userRepository.findByEmail("yok@test.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginRequest("yok@test.com", "Sifre1234")));

        verify(passwordEncoder, never()).matches(any(), any());
    }
}