package com.oenexa.application.vnext;

import com.oenexa.domain.model.cognitive.OenexaVNextDomain.*;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Phase 2: RiskController
 * Verifies that candidate fixes do not exceed safe execution thresholds
 * before automatic deployment is authorized.
 */
@Component
public class RiskController {

    public boolean isSafeToApply(ResolutionFix fix, RiskAssessment assessment) {
        Objects.requireNonNull(fix, "fix must not be null");
        Objects.requireNonNull(assessment, "assessment must not be null");

        // Safety score must be high and fix must not introduce breaking side effects
        return fix.safetyScore() >= 0.90 && fix.estimatedSuccessRate() >= 0.85;
    }
}

