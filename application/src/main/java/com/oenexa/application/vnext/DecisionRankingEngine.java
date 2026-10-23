package com.oenexa.application.vnext;

import com.oenexa.domain.model.cognitive.OenexaVNextDomain.*;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Phase 4: DecisionRankingEngine
 * Evaluates trade-off matrices and ranks hypotheses to select the optimal solution.
 */
@Component
public class DecisionRankingEngine {

    public Hypothesis rankAndSelectBest(List<Hypothesis> hypotheses) {
        if (hypotheses == null || hypotheses.isEmpty()) {
            return new Hypothesis("HYP-DEFAULT", "Default Invariant Execution", 0.90, List.of(), List.of());
        }

        return hypotheses.stream()
                .max(Comparator.comparingDouble(Hypothesis::plausibilityScore))
                .orElse(hypotheses.get(0));
    }
}

