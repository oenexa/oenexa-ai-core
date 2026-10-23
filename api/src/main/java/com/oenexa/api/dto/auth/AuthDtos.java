package com.oenexa.api.dto.auth;

import java.time.Instant;

/**
 * Standardized DTO Layer for Authentication & Identity Domain.
 */
public class AuthDtos {

    public record LoginRequestDto(String usernameOrEmail, String password) {
        public LoginRequestDto {
            usernameOrEmail = (usernameOrEmail != null) ? usernameOrEmail.trim() : "";
            password = (password != null) ? password : "";
        }
    }

    public record RegisterRequestDto(String username, String email, String password, String fullName) {
        public RegisterRequestDto {
            username = (username != null) ? username.trim() : "";
            email = (email != null) ? email.trim() : "";
            password = (password != null) ? password : "";
            fullName = (fullName != null) ? fullName.trim() : "";
        }
    }

    public record TokenResponseDto(String accessToken, String refreshToken, long expiresInSeconds, String tokenType) {
        public TokenResponseDto(String accessToken, String refreshToken, long expiresInSeconds) {
            this(accessToken, refreshToken, expiresInSeconds, "Bearer");
        }
    }

    public record UserProfileDto(
            Long id,
            String username,
            String email,
            String fullName,
            String role,
            String tenantId,
            String plan,
            boolean twoFactorEnabled,
            Instant createdAt
    ) {}
}
