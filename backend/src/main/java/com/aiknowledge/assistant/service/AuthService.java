package com.aiknowledge.assistant.service;

import com.aiknowledge.assistant.dto.auth.AuthResponse;
import com.aiknowledge.assistant.dto.auth.LoginRequest;
import com.aiknowledge.assistant.dto.auth.RegisterRequest;
import com.aiknowledge.assistant.dto.user.UserResponse;
import com.aiknowledge.assistant.entity.User;
import com.aiknowledge.assistant.exception.ConflictException;
import com.aiknowledge.assistant.repository.UserRepository;
import com.aiknowledge.assistant.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("An account with this email already exists");
        }
        User user = userRepository.save(
                new User(email, passwordEncoder.encode(request.password()), request.displayName().trim()));
        log.info("Registered new user id={}", user.getId());
        return toAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(normalizeEmail(request.email()))
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        log.info("User id={} logged in", user.getId());
        return toAuthResponse(user);
    }

    private AuthResponse toAuthResponse(User user) {
        String token = jwtService.issueToken(user.getId(), user.getEmail());
        return new AuthResponse(token, UserResponse.from(user));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
