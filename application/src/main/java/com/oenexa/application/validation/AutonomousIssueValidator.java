package com.oenexa.application.validation;

import com.oenexa.domain.exception.OenexaExceptions.OenexaValidationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Enterprise Validation Layer for Autonomous Issue Detection & Self-Healing.
 */
@Component
public class AutonomousIssueValidator {

    public void validateIssueDetection(String errorTrace, String component) {
        List<String> violations = new ArrayList<>();
        if (errorTrace == null || errorTrace.trim().isBlank()) {
            violations.add("errorTrace must not be blank");
        }
        if (component == null || component.trim().isBlank()) {
            violations.add("component name must not be blank");
        }
        if (!violations.isEmpty()) {
            throw new OenexaValidationException("Issue detection validation failed", violations);
        }
    }

    public void validateSelfHealingTrigger(String cycleId, String failureType) {
        List<String> violations = new ArrayList<>();
        if (cycleId == null || cycleId.trim().isBlank()) {
            violations.add("cycleId must not be blank");
        }
        if (failureType == null || failureType.trim().isBlank()) {
            violations.add("failureType must not be blank");
        }
        if (!violations.isEmpty()) {
            throw new OenexaValidationException("Self healing trigger validation failed", violations);
        }
    }
}
