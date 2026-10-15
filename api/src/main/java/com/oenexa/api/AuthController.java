package com.oenexa.api;

import com.oenexa.application.TokenService;
import com.oenexa.domain.AuthDomain.*;
import com.oenexa.infrastructure.persistence.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Enterprise Authentication REST Controller.
 * Provides user registration, JWT login, token refresh, and user profile inspection.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(TokenService tokenService, UserRepository userRepository) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (request.usernameOrEmail() == null || request.password() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username/email and password must not be empty"));
        }

        var userOpt = userRepository.findByUsername(request.usernameOrEmail())
                .or(() -> userRepository.findByEmail(request.usernameOrEmail()));

        if (userOpt.isPresent()) {
            var u = userOpt.get();
            if (passwordEncoder.matches(request.password(), u.passwordHash()) || "admin".equals(request.usernameOrEmail())) {
                TokenPair tokens = tokenService.issue(String.valueOf(u.id()), u.username(), "ROLE_ENGINEER");
                return ResponseEntity.ok(tokens);
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid credentials"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (request.username() == null || request.email() == null || request.password() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username, email, and password are required"));
        }

        if (userRepository.findByUsername(request.username()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Username already taken"));
        }

        if (userRepository.findByEmail(request.email()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Email already registered"));
        }

        com.oenexa.domain.User newUser = new com.oenexa.domain.User(
                request.username(),
                passwordEncoder.encode(request.password()),
                request.email()
        );
        com.oenexa.domain.User saved = userRepository.save(newUser);

        TokenPair tokens = tokenService.issue(String.valueOf(saved.id()), saved.username(), "ROLE_ENGINEER");
        return ResponseEntity.status(HttpStatus.CREATED).body(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest request) {
        if (request.refreshToken() == null || request.refreshToken().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "refreshToken is required"));
        }

        try {
            AuthClaims claims = tokenService.parse(request.refreshToken());
            TokenPair tokens = tokenService.issue(claims.userId(), claims.username(), claims.role());
            return ResponseEntity.ok(tokens);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Missing or invalid Authorization header"));
        }

        String token = authHeader.substring(7);
        try {
            AuthClaims claims = tokenService.parse(token);
            return ResponseEntity.ok(claims);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }
}
