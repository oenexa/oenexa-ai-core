package com.oenexa.application.validation;

import com.oenexa.domain.exception.OenexaExceptions.OenexaValidationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Enterprise Validation Layer for OENEXA Core Brain & Gateway Directives.
 */
@Component
public class CoreBrainValidator {

    public void validateDirective(String directiveId, String objective) {
        List<String> violations = new ArrayList<>();
        if (objective == null || objective.trim().isBlank()) {
            violations.add("directive objective must not be blank");
        }
        if (directiveId != null && directiveId.length() > 128) {
            violations.add("directiveId must not exceed 128 characters");
        }
        if (!violations.isEmpty()) {
            throw new OenexaValidationException("Core brain directive validation failed", violations);
        }
    }
}
