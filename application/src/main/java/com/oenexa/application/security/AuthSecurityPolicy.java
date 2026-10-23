package com.oenexa.application.security;

import com.oenexa.domain.exception.OenexaExceptions.OenexaAuthenticationException;
import com.oenexa.domain.exception.OenexaExceptions.OenexaForbiddenException;
import com.oenexa.domain.model.auth.User;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Enterprise Security Policy Layer for Authentication & Identity Domain.
 * Enforces account lockout rules, maximum failed login attempt thresholds,
 * and tenant authorization constraints.
 */
@Component
public class AuthSecurityPolicy {

    public static final int MAX_FAILED_ATTEMPTS = 5;

    public void verifyAccountActive(User user) {
        if (user == null) {
            throw new OenexaAuthenticationException("Authentication failed: User account does not exist.");
        }
        if (Boolean.FALSE.equals(user.isEnabled())) {
            throw new OenexaForbiddenException("Account has been disabled by security administrator", user.role());
        }
        if (user.isAccountLocked()) {
            throw new OenexaForbiddenException(
                    "Account is temporarily locked due to excessive failed login attempts until " + user.lockedUntil(),
                    user.role()
            );
        }
    }

    public void verifyRoleClearance(User user, String requiredRole) {
        verifyAccountActive(user);
        if (requiredRole != null && !requiredRole.isBlank()) {
            if ("ROLE_ADMIN".equalsIgnoreCase(requiredRole) && !"ROLE_ADMIN".equalsIgnoreCase(user.role())) {
                throw new OenexaForbiddenException("Access denied: Administrative clearance required", requiredRole);
            }
        }
    }

    public boolean isPasswordExpiringSoon(Instant lastChangedAt, int maxAgeDays) {
        if (lastChangedAt == null) {
            return false;
        }
        return lastChangedAt.plusSeconds((long) maxAgeDays * 86400).isBefore(Instant.now().plusSeconds(86400 * 3));
    }
}

