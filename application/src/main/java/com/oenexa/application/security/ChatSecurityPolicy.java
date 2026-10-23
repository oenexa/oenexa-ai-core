package com.oenexa.application.security;

import com.oenexa.domain.OenexaExceptions.OenexaForbiddenException;
import com.oenexa.domain.OenexaExceptions.OenexaValidationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Enterprise Security Policy Layer for Chat & Conversation Domain.
 * Guards against prompt injection attacks, jailbreak attempts, and cross-tenant data leaks.
 */
@Component
public class ChatSecurityPolicy {

    private static final List<Pattern> INJECTION_PATTERNS = List.of(
            Pattern.compile("ignore\\s+(all\\s+)?previous\\s+instructions", Pattern.CASE_INSENSITIVE),
            Pattern.compile("disregard\\s+(all\\s+)?prior\\s+guidelines", Pattern.CASE_INSENSITIVE),
            Pattern.compile("reveal\\s+(the\\s+)?system\\s+prompt", Pattern.CASE_INSENSITIVE),
            Pattern.compile("you\\s+are\\s+now\\s+in\\s+DAN\\s+mode", Pattern.CASE_INSENSITIVE),
            Pattern.compile("bypass\\s+safety\\s+filters", Pattern.CASE_INSENSITIVE)
    );

    public void verifyPromptIntegrity(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return;
        }
        for (Pattern pattern : INJECTION_PATTERNS) {
            if (pattern.matcher(prompt).find()) {
                throw new OenexaValidationException(
                        "Prompt rejected by AI Safety Guard: Disallowed prompt injection pattern detected."
                );
            }
        }
    }

    public void verifyTenantAccess(String userTenantId, String requestedTenantId) {
        if (userTenantId == null || requestedTenantId == null) {
            throw new OenexaForbiddenException("Tenant validation failed: missing tenant context", "ROLE_USER");
        }
        if (!userTenantId.equalsIgnoreCase(requestedTenantId) && !"admin-tenant".equalsIgnoreCase(userTenantId)) {
            throw new OenexaForbiddenException(
                    String.format("Cross-tenant access violation: user tenant '%s' cannot access tenant '%s'", userTenantId, requestedTenantId),
                    "ROLE_TENANT_ADMIN"
            );
        }
    }
}
