package com.oenexa.application.vnext;

import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Phase 3: ConfidenceScoringEngine
 * Computes statistical and contextual confidence scores for reasoning traces,
 * architectural plans, and automated fixes.
 */
@Component
public class ConfidenceScoringEngine {

    public double calculateConfidence(int successes, int failures, double patternWeight) {
        if (successes + failures == 0) {
            return 0.85; // Default prior baseline
        }
        double empirical = (double) successes / (successes + failures);
        // Weighted combination of historical empirical score and pattern weight
        return Math.min(1.0, Math.max(0.1, (empirical * 0.7) + (patternWeight * 0.3)));
    }

    public boolean requiresDeepReasoningEscalation(double confidenceScore, double threshold) {
        return confidenceScore < threshold;
    }
}
