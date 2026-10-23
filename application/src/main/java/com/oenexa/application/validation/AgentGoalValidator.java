package com.oenexa.application.validation;

import com.oenexa.domain.model.chat.ChatDomain.OenexaAutonomousGoalRequest;
import com.oenexa.domain.model.cognitive.OenexaCoreBrainDomain.CoreBrainDirective;
import com.oenexa.domain.exception.OenexaExceptions.OenexaValidationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Enterprise Validation Layer for Autonomous Agent & Brain Domain.
 * Validates goal descriptions, objective constraints, and execution budgets.
 */
@Component
public class AgentGoalValidator {

    public void validateGoalRequest(OenexaAutonomousGoalRequest request) {
        if (request == null) {
            throw new OenexaValidationException("Autonomous goal request must not be null");
        }
        List<String> violations = new ArrayList<>();
        if (request.goal() == null || request.goal().trim().isBlank()) {
            violations.add("goal description must not be blank");
        } else if (request.goal().trim().length() < 3) {
            violations.add("goal description must be at least 3 characters long");
        }

        if (!violations.isEmpty()) {
            throw new OenexaValidationException("Autonomous goal validation failed", violations);
        }
    }

    public void validateBrainDirective(CoreBrainDirective directive) {
        if (directive == null) {
            throw new OenexaValidationException("Core brain directive must not be null");
        }
        List<String> violations = new ArrayList<>();
        if (directive.objective() == null || directive.objective().trim().isBlank()) {
            violations.add("directive objective must not be blank");
        }

        if (!violations.isEmpty()) {
            throw new OenexaValidationException("Brain directive validation failed", violations);
        }
    }
}

