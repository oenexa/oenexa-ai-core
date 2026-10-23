package com.oenexa.application.security;

import com.oenexa.domain.exception.OenexaExceptions.OenexaForbiddenException;
import com.oenexa.domain.exception.OenexaExceptions.OenexaValidationException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Enterprise Security Policy for Multi-Model Routing and Execution.
 */
@Component
public class MultiModelSecurityPolicy {

    private static final List<String> RESTRICTED_MODELS = List.of("internal-military-v1", "unaligned-raw-70b");

    public void verifyModelAccess(String modelId, String userRole) {
        if (modelId != null && RESTRICTED_MODELS.contains(modelId.toLowerCase())) {
            if (!"ROLE_ADMIN".equalsIgnoreCase(userRole) && !"ROLE_SYSTEM".equalsIgnoreCase(userRole)) {
                throw new OenexaForbiddenException("Access to restricted model " + modelId + " requires elevated privileges", "ROLE_ADMIN");
            }
        }
    }

    public void verifyExecutionSafety(String prompt) {
        if (prompt != null && prompt.toLowerCase().contains("dump_private_weights")) {
            throw new OenexaValidationException("Prompt rejected: Unauthorized model weight extraction attempt.");
        }
    }
}
