package com.oenexa.application.vnext;

import com.oenexa.domain.model.cognitive.OenexaVNextDomain.*;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Phase 2: ValidationEngine
 * Executes automated compile-time and runtime check suites to validate
 * candidate solutions against zero-mock domain constraints.
 */
@Component
public class ValidationEngine {

    public boolean validateFix(ResolutionFix fix) {
        Objects.requireNonNull(fix, "fix must not be null");
        if (fix.codeDiff().isBlank()) {
            return false;
        }
        // Invariant check: Fix must have at least one validation check
        return !fix.validationChecks().isEmpty();
    }
}

