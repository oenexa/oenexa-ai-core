package com.oenexa.application.security;

import com.oenexa.domain.exception.OenexaExceptions.OenexaForbiddenException;
import org.springframework.stereotype.Component;

/**
 * Enterprise Security Policy for Billing and Financial Operations.
 */
@Component
public class BillingSecurityPolicy {

    public void verifyBillingAccess(String requestingUserId, String targetUserId, String role) {
        if ("ROLE_ADMIN".equalsIgnoreCase(role) || "ROLE_BILLING_ADMIN".equalsIgnoreCase(role)) {
            return;
        }
        if (requestingUserId != null && targetUserId != null && !requestingUserId.equalsIgnoreCase(targetUserId)) {
            throw new OenexaForbiddenException("Access denied: You cannot view or modify another user's billing records", "ROLE_USER");
        }
    }
}
