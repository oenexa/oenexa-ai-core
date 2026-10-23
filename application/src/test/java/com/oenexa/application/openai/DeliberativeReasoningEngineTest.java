package com.oenexa.application.openai;

import com.oenexa.domain.model.cognitive.OpenAiReasoningDomain.CognitiveStage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DeliberativeReasoningEngine Zero-Mock Unit Tests")
class DeliberativeReasoningEngineTest {

    private final DeliberativeReasoningEngine engine = new DeliberativeReasoningEngine();

    @Test
    @DisplayName("Should execute 5-stage cognitive pipeline with high reasoning effort")
    void testHighEffortReasoning() {
        var res = engine.reason("Synthesize reactive payment service with sealed records", "high");
        assertNotNull(res);
        assertNotNull(res.finalSynthesis());
        assertTrue(res.finalSynthesis().contains("OENEXA Neo-GPT Autonomous Engineering Solution"));
        assertTrue(res.confidenceScore() >= 0.99);

        var trace = res.trace();
        assertEquals(CognitiveStage.ANSWER_SYNTHESIS, trace.stage());
        assertEquals(3, trace.hypothesesCount());
        assertTrue(trace.selfCritiquePassed());
        assertEquals(5, trace.thoughtSteps().size());
        assertTrue(res.reasoningTraceMarkdown().contains("o3 Deliberative Reasoning Trace"));
    }

    @Test
    @DisplayName("Should handle low and default reasoning effort budgets")
    void testLowAndDefaultEffortReasoning() {
        var lowRes = engine.reason("Fix bug in controller", "low");
        assertEquals(0.95, lowRes.confidenceScore());

        var defaultRes = engine.reason("Build service", null);
        assertEquals(0.98, defaultRes.confidenceScore());

        var blankRes = engine.reason("Build service", "   ");
        assertEquals(0.98, blankRes.confidenceScore());
    }

    @Test
    @DisplayName("Should throw NullPointerException when prompt is null")
    void testNullPromptThrows() {
        assertThrows(NullPointerException.class, () -> engine.reason(null, "high"));
    }
}

