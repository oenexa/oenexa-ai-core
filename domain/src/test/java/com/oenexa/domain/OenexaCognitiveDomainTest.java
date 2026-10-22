package com.oenexa.domain;

import com.oenexa.domain.OenexaAgentOsDomain.*;
import com.oenexa.domain.OenexaCognitiveDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OenexaCognitiveDomain Zero-Mock Invariant & Contract Tests")
class OenexaCognitiveDomainTest {

    @Test
    @DisplayName("OenexaCognitiveRequest should enforce non-null invariants and defaults")
    void testCognitiveRequestInvariants() {
        assertThrows(NullPointerException.class, () -> new OenexaCognitiveRequest(
                null, List.of(OenexaCognitiveMessage.user("hi")), null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
        ));

        assertThrows(NullPointerException.class, () -> new OenexaCognitiveRequest(
                "oenexa-neo-gpt-o3-max", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
        ));

        assertThrows(IllegalArgumentException.class, () -> new OenexaCognitiveRequest(
                "oenexa-neo-gpt-o3-max", List.of(), null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
        ));

        OenexaCognitiveRequest req = OenexaCognitiveRequest.of("oenexa-neo-gpt-o3-max", "Write a reactive service");
        assertEquals("oenexa-neo-gpt-o3-max", req.model());
        assertEquals(1, req.messages().size());
        assertEquals("user", req.messages().get(0).role());
        assertEquals("Write a reactive service", req.messages().get(0).content());
        assertEquals(0.7, req.temperature());
        assertEquals(1.0, req.topP());
        assertEquals(1, req.n());
        assertFalse(req.stream());
        assertEquals("medium", req.reasoningEffort());
        assertTrue(req.stop().isEmpty());
        assertTrue(req.tools().isEmpty());
        assertTrue(req.logitBias().isEmpty());
    }

    @Test
    @DisplayName("OenexaCognitiveMessage should correctly instantiate all roles and reasoning")
    void testCognitiveMessageFactories() {
        assertThrows(NullPointerException.class, () -> new OenexaCognitiveMessage(null, "text", null, null, null, null, null));

        OenexaCognitiveMessage sys = OenexaCognitiveMessage.system("You are an AI architect.");
        assertEquals("system", sys.role());
        assertEquals("You are an AI architect.", sys.content());

        OenexaCognitiveMessage usr = OenexaCognitiveMessage.user("Hello");
        assertEquals("user", usr.role());
        assertEquals("Hello", usr.content());

        OenexaCognitiveMessage asst = OenexaCognitiveMessage.assistant("Done");
        assertEquals("assistant", asst.role());
        assertEquals("Done", asst.content());

        OenexaCognitiveMessage asstReasoning = OenexaCognitiveMessage.assistantWithReasoning("Done", "Stage 1: Intent analyzed.");
        assertEquals("Done", asstReasoning.content());
        assertEquals("Stage 1: Intent analyzed.", asstReasoning.reasoningContent());

        OenexaCognitiveMessage toolMsg = OenexaCognitiveMessage.tool("call_1", "Result output");
        assertEquals("tool", toolMsg.role());
        assertEquals("call_1", toolMsg.toolCallId());
        assertEquals("Result output", toolMsg.content());
    }

    @Test
    @DisplayName("OenexaCognitiveResponse single factory should build valid response")
    void testCognitiveResponse() {
        OenexaCognitiveUsage usage = OenexaCognitiveUsage.compute("Prompt text", "Answer text", "Reasoning trace");
        assertTrue(usage.promptTokens() > 0);
        assertTrue(usage.completionTokens() > 0);
        assertTrue(usage.reasoningTokens() > 0);
        assertEquals(usage.promptTokens() + usage.completionTokens(), usage.totalTokens());

        OenexaCognitiveResponse resp = OenexaCognitiveResponse.single(
                "oenexa-neo-gpt-o3-max",
                OenexaCognitiveMessage.assistant("Success"),
                usage
        );

        assertNotNull(resp.id());
        assertEquals("chat.completion", resp.object());
        assertEquals("oenexa-neo-gpt-o3-max", resp.model());
        assertEquals(1, resp.choices().size());
        assertEquals("stop", resp.choices().get(0).finishReason());
        assertEquals("Success", resp.choices().get(0).message().content());
        assertEquals("oenexa-neo-gpt-v3", resp.systemFingerprint());
    }

    @Test
    @DisplayName("OenexaModelDescriptor native catalog should list pure sovereign models")
    void testModelCatalog() {
        List<OenexaModelDescriptor> catalog = OenexaModelDescriptor.nativeCatalog();
        assertFalse(catalog.isEmpty());
        assertTrue(catalog.stream().anyMatch(m -> m.id().equals("oenexa-neo-gpt-o3-max")));
        assertTrue(catalog.stream().anyMatch(m -> m.id().equals("oenexa-neo-gpt-coder-32b")));
        assertTrue(catalog.stream().allMatch(OenexaModelDescriptor::sovereignInProcess));
    }

    @Test
    @DisplayName("OenexaEmbeddingRequest and Response should enforce invariants")
    void testEmbeddingInvariants() {
        assertThrows(NullPointerException.class, () -> new OenexaEmbeddingRequest(null, "m", "u"));
        assertThrows(IllegalArgumentException.class, () -> new OenexaEmbeddingRequest(List.of(), "m", "u"));

        OenexaEmbeddingRequest req = new OenexaEmbeddingRequest(List.of("code token"), null, "u1");
        assertEquals("oenexa-embedding-v3", req.model());

        OenexaEmbeddingData data = new OenexaEmbeddingData(null, 0, List.of(0.1, 0.2));
        OenexaEmbeddingResponse resp = new OenexaEmbeddingResponse(null, List.of(data), req.model(), null);
        assertEquals("list", resp.object());
        assertEquals(1, resp.data().size());
    }

    @Test
    @DisplayName("OenexaResponsesRequest and Response should construct structured output")
    void testResponsesInvariants() {
        OenexaResponsesRequest req = new OenexaResponsesRequest(null, "Design microservice", null, null);
        assertEquals("oenexa-neo-gpt-o3-max", req.model());
        assertEquals("high", req.reasoningEffort());

        OenexaResponsesResponse resp = new OenexaResponsesResponse(
                "resp-1", null, 1700000000L, req.model(),
                List.of(new OenexaResponseOutputItem(null, null, "Microservice spec")),
                null, null
        );
        assertEquals("resp-1", resp.id());
        assertEquals("response", resp.object());
        assertEquals("completed", resp.status());
        assertEquals(1, resp.output().size());
        assertEquals("Microservice spec", resp.output().get(0).content());
    }

    @Test
    @DisplayName("OenexaDeliberativeTrace should capture 5-stage cognitive steps")
    void testDeliberativeTrace() {
        OenexaDeliberativeTrace trace = new OenexaDeliberativeTrace(
                OenexaCognitiveStage.ANSWER_SYNTHESIS,
                3,
                true,
                0.98,
                "Synthesized best hypothesis with zero defects",
                List.of("Step 1: Analyzed AST", "Step 2: Proved invariants")
        );

        assertEquals(OenexaCognitiveStage.ANSWER_SYNTHESIS, trace.stage());
        assertEquals(3, trace.hypothesesCount());
        assertTrue(trace.selfCritiquePassed());
        assertEquals(0.98, trace.confidenceScore());
        assertEquals(2, trace.thoughtSteps().size());
    }

    @Test
    @DisplayName("OenexaAgentOsGoalRequest and Response should support 10 personas")
    void testAgentOsInvariants() {
        assertThrows(NullPointerException.class, () -> new OenexaAgentOsGoalRequest(null, null, null, false, null, null));

        OenexaAgentOsGoalRequest req = new OenexaAgentOsGoalRequest(
                "Refactor authentication to sealed records", null, null, true, null, null
        );
        assertEquals("repo-default", req.repositoryId());
        assertEquals("engineer", req.userId());
        assertTrue(req.sovereignAirGapped());
        assertEquals("high", req.reasoningEffort());
        assertEquals(10, req.selectedPersonas().size());

        OenexaDeliberativeTrace trace = new OenexaDeliberativeTrace(
                OenexaCognitiveStage.ANSWER_SYNTHESIS, 2, true, 0.99, "Done", List.of()
        );
        OenexaAgentOsTaskStep step = new OenexaAgentOsTaskStep(
                "s1", OenexaAgentPersona.ARCHITECT_AGENT, "Define boundaries", "Boundaries created", null, 120L
        );
        OenexaAgentOsMetrics metrics = new OenexaAgentOsMetrics(500L, 1200, 48.0, true, true, 0.99);

        OenexaAgentOsGoalResponse resp = new OenexaAgentOsGoalResponse(
                "exec-1", req.goal(), null, trace, List.of(step), List.of(), metrics
        );

        assertEquals("exec-1", resp.executionId());
        assertEquals("COMPLETED", resp.status());
        assertEquals(1, resp.executedSteps().size());
        assertEquals(OenexaAgentPersona.ARCHITECT_AGENT, resp.executedSteps().get(0).persona());
        assertEquals(0.99, resp.metrics().confidenceScore());
        assertTrue(resp.metrics().airGappedVerified());
        assertTrue(resp.metrics().zeroMockTestVerified());
    }
}
