package com.oenexa.domain;

import java.time.Instant;

/**
 * Domain contracts for Authentication and JWT Tokens.
 */
public class AuthDomain {

    public record TokenPair(String accessToken, String refreshToken, long expiresInSeconds) {}

    public record AuthClaims(String userId, String username, String role, Instant issuedAt, Instant expiresAt) {}

    public record LoginRequest(String usernameOrEmail, String password, DeviceFingerprint device) {
        public LoginRequest(String usernameOrEmail, String password) {
            this(usernameOrEmail, password, null);
        }
    }

    public record RegisterRequest(String username, String email, String password, String fullName) {}

    public record RefreshRequest(String refreshToken) {}

    public record UserProfile(
            Long id,
            String username,
            String email,
            String firstName,
            String lastName,
            String country,
            String profileImageUrl,
            String bio,
            String company,
            String profession,
            String language,
            String timezone,
            String subscriptionPlan,
            String role,
            String tenantId,
            boolean twoFactorEnabled,
            Instant createdAt
    ) {}

    public record UserProfileUpdateRequest(
            String firstName,
            String lastName,
            String country,
            String bio,
            String company,
            String profession,
            String language,
            String timezone,
            String profileImageUrl
    ) {}

    public record TwoFactorSetupResponse(String secret, String otpauthUrl, java.util.List<String> backupCodes) {}

    public record TwoFactorVerifyRequest(String usernameOrEmail, String code, String secret) {}

    public record OtpRequest(String email, String purpose) {}

    public record OtpVerifyRequest(String email, String code, String purpose) {}

    public record PasswordResetRequest(String email) {}

    public record PasswordResetConfirmRequest(String email, String resetToken, String newPassword) {}

    public record OAuthLoginRequest(String provider, String providerId, String email, String fullName, String avatarUrl, String accessToken) {}

    public record DeviceFingerprint(String ipAddress, String userAgent, String deviceType, String browser, String os, String country) {}

    public record UserSession(
            String sessionId,
            String userId,
            String username,
            DeviceFingerprint device,
            Instant createdAt,
            Instant lastActiveAt,
            boolean active
    ) {}

    public record AuditLogEntry(
            String id,
            String userId,
            String username,
            String action,
            String ipAddress,
            String userAgent,
            Instant timestamp,
            String status,
            java.util.Map<String, String> details
    ) {}
}
