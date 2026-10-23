package com.oenexa.application.security;

import com.oenexa.domain.exception.OenexaExceptions.OenexaForbiddenException;
import org.springframework.stereotype.Component;

/**
 * Enterprise Security Policy for Autonomous Self-Healing and Resolution Cycles.
 */
@Component
public class AutonomousIssueSecurityPolicy {

    public void verifySelfHealingPermission(String environment, String actorRole) {
        if ("PRODUCTION".equalsIgnoreCase(environment)) {
            if (!"ROLE_SYSTEM".equalsIgnoreCase(actorRole) && !"ROLE_ADMIN".equalsIgnoreCase(actorRole)) {
                throw new OenexaForbiddenException("Production self-healing triggers require autonomous system or admin role", "ROLE_ADMIN");
            }
        }
    }
}
