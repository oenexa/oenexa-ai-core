package com.oenexa.domain;

import java.util.List;
import java.util.Objects;

/**
 * Enterprise Domain Records for OpenAI Wire-Compatible Deliberative Reasoning (o1/o3) Specification.
 */
public final class OpenAiReasoningDomain {

    private OpenAiReasoningDomain() {}

    public enum CognitiveStage {
        INTENT_UNDERSTANDING,
        MULTI_HYPOTHESIS_EXPLORATION,
        DEEP_ANALYTICAL_REASONING,
        SELF_CRITIQUE_ENGINE,
        ANSWER_SYNTHESIS
    }

    public record DeliberativeReasoningTrace(
            CognitiveStage stage,
            int hypothesesCount,
            boolean selfCritiquePassed,
            double confidenceScore,
            String reasoningSummary,
            List<String> thoughtSteps
    ) {
        public DeliberativeReasoningTrace {
            Objects.requireNonNull(stage, "stage must not be null");
            reasoningSummary = (reasoningSummary != null) ? reasoningSummary : "";
            thoughtSteps = (thoughtSteps != null) ? List.copyOf(thoughtSteps) : List.of();
        }
    }
}
