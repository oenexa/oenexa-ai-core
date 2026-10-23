package com.oenexa.domain.model.cognitive;

import com.oenexa.domain.exception.*;
import com.oenexa.domain.model.agent.*;
import com.oenexa.domain.model.auth.*;
import com.oenexa.domain.model.chat.*;
import com.oenexa.domain.model.code.*;
import com.oenexa.domain.model.cognitive.*;
import com.oenexa.domain.model.common.*;
import com.oenexa.domain.model.knowledge.*;
import com.oenexa.domain.model.memory.*;
import com.oenexa.domain.model.orchestration.*;

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

