package com.eventbook.service;

import com.eventbook.dto.AuthResponse;
import com.eventbook.dto.LoginRequest;
import com.eventbook.dto.RegisterRequest;
import com.eventbook.entity.Role;
import com.eventbook.entity.User;
import com.eventbook.exception.DuplicateResourceException;
import com.eventbook.exception.InvalidCredentialsException;
import com.eventbook.repository.UserRepository;
import com.eventbook.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtUtil);
    }

    @Test
    void register_newEmail_createsUserAndReturnsToken() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Priya Sharma");
        req.setEmail("priya@example.com");
        req.setPassword("secret123");
        req.setRole(Role.ATTENDEE);

        when(userRepository.existsByEmail("priya@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtUtil.generateToken(eq("priya@example.com"), anyMap())).thenReturn("jwt-token");

        AuthResponse response = authService.register(req);

        assertEquals("jwt-token", response.getToken());
        assertEquals("priya@example.com", response.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsException() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("existing@example.com");
        req.setPassword("secret123");
        req.setName("Test");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(req));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_wrongPassword_throwsInvalidCredentials() {
        LoginRequest req = new LoginRequest();
        req.setEmail("user@example.com");
        req.setPassword("wrongpass");

        User user = User.builder().id(1L).email("user@example.com").passwordHash("hashed").role(Role.ATTENDEE).build();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpass", "hashed")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(req));
    }
}
