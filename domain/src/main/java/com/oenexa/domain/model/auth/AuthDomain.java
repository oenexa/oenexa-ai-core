package com.oenexa.domain.model.auth;

import com.oenexa.domain.exception.*;
import com.oenexa.domain.model.agent.*;
import com.oenexa.domain.model.auth.*;
import com.oenexa.domain.model.chat.*;
import com.oenexa.domain.model.code.*;
import com.oenexa.domain.model.cognitive.*;
import com.oenexa.domain.model.common.*;
import com.oenexa.domain.model.knowledge.*;
import com.oenexa.domain.model.memory.*;
import com.oenexa.domain.model.orchestration.*;

import java.time.Instant;

/**
 * Domain contracts for Authentication and JWT Tokens.
 */
public class AuthDomain {

    public record TokenPair(String accessToken, String refreshToken, long expiresInSeconds) {
        public String getToken() {
            return accessToken;
        }

        public String getTokenType() {
            return "Bearer";
        }
    }

    public record AuthClaims(String userId, String username, String role, Instant issuedAt, Instant expiresAt) {}

    public record LoginRequest(String usernameOrEmail, String password, DeviceFingerprint device, String email, String username) {
        public LoginRequest(String usernameOrEmail, String password, DeviceFingerprint device) {
            this(usernameOrEmail, password, device, null, null);
        }

        public LoginRequest(String usernameOrEmail, String password) {
            this(usernameOrEmail, password, null, null, null);
        }

        public LoginRequest {
            if (usernameOrEmail == null || usernameOrEmail.isBlank()) {
                if (email != null && !email.isBlank()) {
                    usernameOrEmail = email.trim();
                } else if (username != null && !username.isBlank()) {
                    usernameOrEmail = username.trim();
                }
            }
        }

        public String resolveIdentifier() {
            if (usernameOrEmail != null && !usernameOrEmail.isBlank()) return usernameOrEmail.trim();
            if (email != null && !email.isBlank()) return email.trim();
            if (username != null && !username.isBlank()) return username.trim();
            return "";
        }
    }

    public record RegisterRequest(String username, String email, String password, String fullName, String firstName, String lastName) {
        public RegisterRequest(String username, String email, String password, String fullName) {
            this(username, email, password, fullName, null, null);
        }

        public RegisterRequest {
            if ((username == null || username.isBlank()) && email != null && email.contains("@")) {
                String prefix = email.substring(0, email.indexOf('@')).trim();
                if (prefix.length() >= 3) {
                    username = prefix;
                }
            }
            if ((fullName == null || fullName.isBlank()) && (firstName != null || lastName != null)) {
                String f = firstName != null ? firstName.trim() : "";
                String l = lastName != null ? lastName.trim() : "";
                fullName = (f + " " + l).trim();
            }
        }

        public String resolveUsername() {
            if (username != null && !username.isBlank()) return username.trim();
            if (email != null && email.contains("@")) {
                String prefix = email.substring(0, email.indexOf('@')).trim();
                if (prefix.length() >= 3) return prefix;
            }
            return "";
        }

        public String resolveFullName() {
            if (fullName != null && !fullName.isBlank()) return fullName.trim();
            String f = firstName != null ? firstName.trim() : "";
            String l = lastName != null ? lastName.trim() : "";
            return (f + " " + l).trim();
        }
    }

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
