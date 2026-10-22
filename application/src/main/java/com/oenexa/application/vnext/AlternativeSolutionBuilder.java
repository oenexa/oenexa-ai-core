package com.oenexa.application.vnext;

import com.oenexa.domain.OenexaVNextDomain.*;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Phase 4: AlternativeSolutionBuilder
 * Builds concrete architectural and code alternatives corresponding to generated hypotheses.
 */
@Component
public class AlternativeSolutionBuilder {

    public List<String> buildAlternatives(List<Hypothesis> hypotheses) {
        if (hypotheses == null || hypotheses.isEmpty()) {
            return List.of("Standard Deterministic Invariant Solution");
        }

        List<String> alternatives = new ArrayList<>();
        for (Hypothesis h : hypotheses) {
            alternatives.add("Alternative (" + h.hypothesisId() + "): " + h.thesis() + " [Plausibility: " + h.plausibilityScore() + "]");
        }
        return alternatives;
    }
}
