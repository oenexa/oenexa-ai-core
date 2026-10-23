package com.oenexa.application.validation;

import com.oenexa.domain.exception.OenexaExceptions.OenexaValidationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Enterprise Validation Layer for Multi-Model Routing & Orchestration Domain.
 */
@Component
public class MultiModelValidator {

    private static final int MAX_PROMPT_LENGTH = 64_000;

    public void validateRouteRequest(String prompt, String taskType, String strategy) {
        List<String> violations = new ArrayList<>();
        if (prompt == null || prompt.trim().isBlank()) {
            violations.add("prompt must not be blank");
        } else if (prompt.length() > MAX_PROMPT_LENGTH) {
            violations.add("prompt exceeds maximum allowed length of " + MAX_PROMPT_LENGTH + " characters");
        }

        if (strategy != null && !isValidStrategy(strategy)) {
            violations.add("unsupported optimization strategy: " + strategy);
        }

        if (!violations.isEmpty()) {
            throw new OenexaValidationException("MultiModel route request validation failed", violations);
        }
    }

    public void validateFallbackRequest(String message, String strategy) {
        List<String> violations = new ArrayList<>();
        if (message == null || message.trim().isBlank()) {
            violations.add("message must not be blank");
        }
        if (strategy != null && !isValidStrategy(strategy)) {
            violations.add("unsupported optimization strategy: " + strategy);
        }
        if (!violations.isEmpty()) {
            throw new OenexaValidationException("MultiModel fallback request validation failed", violations);
        }
    }

    private boolean isValidStrategy(String strategy) {
        return "AUTO".equalsIgnoreCase(strategy)
                || "COST".equalsIgnoreCase(strategy)
                || "SPEED".equalsIgnoreCase(strategy)
                || "QUALITY".equalsIgnoreCase(strategy)
                || "LOCAL_FIRST".equalsIgnoreCase(strategy);
    }
}
