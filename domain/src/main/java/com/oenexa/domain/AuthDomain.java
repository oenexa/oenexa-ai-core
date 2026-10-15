package com.oenexa.domain;

import java.time.Instant;

/**
 * Domain contracts for Authentication and JWT Tokens.
 */
public class AuthDomain {

    public record TokenPair(String accessToken, String refreshToken, long expiresInSeconds) {}

    public record AuthClaims(String userId, String username, String role, Instant issuedAt, Instant expiresAt) {}

    public record LoginRequest(String usernameOrEmail, String password) {}

    public record RegisterRequest(String username, String email, String password, String fullName) {}

    public record RefreshRequest(String refreshToken) {}
}
