package com.oenexa.application.vnext;

import com.oenexa.domain.OenexaVNextDomain.*;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Phase 1: RiskAssessmentEngine
 * Calculates multidimensional risk scores (0.0 - 1.0), evaluates blast radius,
 * and determines if immediate mitigation is mandated.
 */
@Component
public class RiskAssessmentEngine {

    public RiskAssessment evaluateRisk(ClassifiedIssue issue, RootCauseReport rootCause) {
        Objects.requireNonNull(issue, "issue must not be null");
        Objects.requireNonNull(rootCause, "rootCause must not be null");

        double baseScore = switch (issue.type()) {
            case SECURITY_RISK, MEMORY_LEAK -> 0.95;
            case BUG, RUNTIME_FAILURE -> 0.75;
            case ARCHITECTURE_VIOLATION, DEPENDENCY_CONFLICT -> 0.60;
            case PERFORMANCE_BOTTLENECK -> 0.50;
            case UNKNOWN_SCENARIO -> 0.30;
        };

        boolean immediate = baseScore >= 0.70;
        String level = baseScore >= 0.85 ? "CRITICAL" : baseScore >= 0.65 ? "HIGH" : "MODERATE";

        List<String> impactAreas = new ArrayList<>();
        impactAreas.add("System Stability");
        if (issue.type() == VNextDetectionType.SECURITY_RISK) {
            impactAreas.add("Confidentiality & Ingress Boundary");
        }
        if (issue.type() == VNextDetectionType.ARCHITECTURE_VIOLATION) {
            impactAreas.add("Code Maintainability & Hexagonal Isolation");
        }

        return new RiskAssessment(issue.issueId(), baseScore, level, immediate, impactAreas);
    }
}
