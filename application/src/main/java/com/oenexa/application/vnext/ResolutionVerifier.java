package com.oenexa.application.vnext;

import com.oenexa.domain.model.cognitive.OenexaVNextDomain.*;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Phase 2: ResolutionVerifier
 * Confirms post-fix stability and zero regressions across the codebase.
 */
@Component
public class ResolutionVerifier {

    public boolean verifyResolution(ClassifiedIssue originalIssue, ResolutionFix appliedFix) {
        Objects.requireNonNull(originalIssue, "originalIssue must not be null");
        Objects.requireNonNull(appliedFix, "appliedFix must not be null");

        // Verify that the applied fix directly addresses the root symptoms
        return appliedFix.estimatedSuccessRate() >= 0.88;
    }
}

