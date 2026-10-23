package com.oenexa.application.vnext;

import com.oenexa.domain.model.cognitive.OenexaVNextDomain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Phase 3: LearningEngineV2
 * Orchestrates multi-channel learning across:
 * - Execution successes & failures
 * - User feedback & ratings
 * - Architectural decisions (ADRs)
 * - Code review outcomes
 */
@Service
public class LearningEngineV2 {

    private final PatternRepository patternRepository;
    private final KnowledgeEvolutionManager evolutionManager;
    private final ConfidenceScoringEngine scoringEngine;

    public LearningEngineV2() {
        this(new PatternRepository(), new KnowledgeEvolutionManager(), new ConfidenceScoringEngine());
    }

    @Autowired
    public LearningEngineV2(
            PatternRepository patternRepository,
            KnowledgeEvolutionManager evolutionManager,
            ConfidenceScoringEngine scoringEngine
    ) {
        this.patternRepository = Objects.requireNonNull(patternRepository, "patternRepository must not be null");
        this.evolutionManager = Objects.requireNonNull(evolutionManager, "evolutionManager must not be null");
        this.scoringEngine = Objects.requireNonNull(scoringEngine, "scoringEngine must not be null");
    }

    public VNextPattern recordExecutionOutcome(String patternId, boolean success, String contextDetails) {
        Objects.requireNonNull(patternId, "patternId must not be null");
        VNextPattern existing = patternRepository.findById(patternId)
                .orElseGet(() -> new VNextPattern(patternId, VNextPatternCategory.SOLUTION, "Auto-discovered Pattern", "Dynamic", "Autonomous", 0, 0, 0.5));

        VNextPattern updated = existing.updateOutcome(success);
        patternRepository.save(updated);

        String status = success ? "SUCCESS" : "FAILURE";
        evolutionManager.recordEvolution("ExecutionOutcome", "Pattern " + patternId + " recorded " + status + " | " + contextDetails);

        return updated;
    }

    public void ingestUserFeedback(String patternId, boolean positive, String comments) {
        recordExecutionOutcome(patternId, positive, "UserFeedback: " + comments);
    }

    public void ingestCodeReviewDecision(String patternId, boolean approved, String reviewNotes) {
        recordExecutionOutcome(patternId, approved, "CodeReview: " + reviewNotes);
    }

    public double evaluatePatternConfidence(String patternId) {
        return patternRepository.findById(patternId)
                .map(p -> scoringEngine.calculateConfidence(p.successTally(), p.failureTally(), p.dynamicWeight()))
                .orElse(0.85);
    }

    public int getTotalLearnedPatterns() {
        return patternRepository.count();
    }
}

