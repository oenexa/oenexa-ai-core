package com.oenexa.application.security;

import com.oenexa.domain.exception.OenexaExceptions.OenexaForbiddenException;
import com.oenexa.domain.exception.OenexaExceptions.OenexaValidationException;
import org.springframework.stereotype.Component;

/**
 * Enterprise Security Policy for OENEXA Core Brain Directive Execution.
 */
@Component
public class CoreBrainSecurityPolicy {

    public void verifyDirectiveIntegrity(String objective, String priority, String actorRole) {
        if ("OVERRIDE_SYSTEM_CORE".equalsIgnoreCase(priority) && !"ROLE_SYSTEM".equalsIgnoreCase(actorRole) && !"ROLE_ADMIN".equalsIgnoreCase(actorRole)) {
            throw new OenexaForbiddenException("High-priority core brain directives require elevated permissions", "ROLE_ADMIN");
        }
        if (objective != null && objective.toLowerCase().contains("disable_safety_gates")) {
            throw new OenexaValidationException("Brain directive rejected: Cannot disable autonomous safety gates.");
        }
    }
}
