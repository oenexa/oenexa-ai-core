package com.oenexa.domain;

import com.oenexa.domain.OpenAiAgentOsDomain.*;
import com.oenexa.domain.OpenAiDomain.*;
import com.oenexa.domain.OpenAiMediaDomain.*;
import com.oenexa.domain.OpenAiReasoningDomain.*;
import com.oenexa.domain.OpenAiToolProtocolDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OpenAiDomain Zero-Mock Invariant & Contract Tests")
class OpenAiDomainTest {

    @Test
    @DisplayName("OpenAiChatCompletionRequest should enforce non-null invariants and defaults")
    void testChatCompletionRequestInvariants() {
        assertThrows(NullPointerException.class, () -> new OpenAiChatCompletionRequest(
                null, List.of(OpenAiChatMessage.user("hi")), null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
        ));

        assertThrows(NullPointerException.class, () -> new OpenAiChatCompletionRequest(
                "gpt-4o", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
        ));

        assertThrows(IllegalArgumentException.class, () -> new OpenAiChatCompletionRequest(
                "gpt-4o", List.of(), null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
        ));

        OpenAiChatCompletionRequest req = OpenAiChatCompletionRequest.of("oenexa-neo-gpt-o1", "Write a reactive service");
        assertEquals("oenexa-neo-gpt-o1", req.model());
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
    @DisplayName("OpenAiChatMessage should correctly instantiate all roles and reasoning")
    void testChatMessageFactories() {
        assertThrows(NullPointerException.class, () -> new OpenAiChatMessage(null, "text", null, null, null, null, null));

        OpenAiChatMessage sys = OpenAiChatMessage.system("You are an AI architect.");
        assertEquals("system", sys.role());
        assertEquals("You are an AI architect.", sys.content());

        OpenAiChatMessage usr = OpenAiChatMessage.user("Hello");
        assertEquals("user", usr.role());
        assertEquals("Hello", usr.content());

        OpenAiChatMessage asst = OpenAiChatMessage.assistant("Done");
        assertEquals("assistant", asst.role());
        assertEquals("Done", asst.content());

        OpenAiChatMessage asstReasoning = OpenAiChatMessage.assistantWithReasoning("Done", "Stage 1: Intent analyzed.");
        assertEquals("Done", asstReasoning.content());
        assertEquals("Stage 1: Intent analyzed.", asstReasoning.reasoningContent());

        OpenAiToolCall tc = new OpenAiToolCall("call_1", "function", new OpenAiFunctionCall("ast_analyzer", "{}"));
        OpenAiChatMessage asstTools = OpenAiChatMessage.assistantWithTools("Calling tool", List.of(tc));
        assertEquals(1, asstTools.toolCalls().size());
        assertEquals("call_1", asstTools.toolCalls().get(0).id());

        OpenAiChatMessage toolMsg = OpenAiChatMessage.tool("call_1", "Result output");
        assertEquals("tool", toolMsg.role());
        assertEquals("call_1", toolMsg.toolCallId());
        assertEquals("Result output", toolMsg.content());
    }

    @Test
    @DisplayName("OpenAiChatCompletionResponse single factory should build valid response")
    void testChatCompletionResponse() {
        OpenAiUsage usage = OpenAiUsage.compute("Prompt text", "Answer text", "Reasoning trace");
        assertTrue(usage.promptTokens() > 0);
        assertTrue(usage.completionTokens() > 0);
        assertTrue(usage.reasoningTokens() > 0);
        assertEquals(usage.promptTokens() + usage.completionTokens() + usage.reasoningTokens(), usage.totalTokens());

        OpenAiChatCompletionResponse resp = OpenAiChatCompletionResponse.single(
                "chatcmpl-001",
                "oenexa-neo-gpt-o1",
                OpenAiChatMessage.assistant("Success"),
                "stop",
                usage
        );

        assertEquals("chatcmpl-001", resp.id());
        assertEquals("chat.completion", resp.object());
        assertEquals("oenexa-neo-gpt-o1", resp.model());
        assertEquals(1, resp.choices().size());
        assertEquals("stop", resp.choices().get(0).finishReason());
        assertEquals("Success", resp.choices().get(0).message().content());
        assertEquals("fp_oenexa_neo_gpt_25", resp.systemFingerprint());
    }

    @Test
    @DisplayName("OpenAiUsage compute should handle null values safely")
    void testUsageNullSafety() {
        OpenAiUsage usage = OpenAiUsage.compute(null, null, null);
        assertEquals(0, usage.promptTokens());
        assertEquals(0, usage.completionTokens());
        assertEquals(0, usage.reasoningTokens());
        assertEquals(0, usage.totalTokens());
    }

    @Test
    @DisplayName("Models & Embeddings records should validate and default correctly")
    void testModelsAndEmbeddings() {
        OpenAiModel model = new OpenAiModel("oenexa-neo-gpt-o1", null, 1700000000L, null);
        assertEquals("oenexa-neo-gpt-o1", model.id());
        assertEquals("model", model.object());
        assertEquals("oenexa-system", model.ownedBy());

        OpenAiModelList modelList = new OpenAiModelList(null, List.of(model));
        assertEquals("list", modelList.object());
        assertEquals(1, modelList.data().size());

        assertThrows(NullPointerException.class, () -> new OpenAiEmbeddingRequest(null, "text-embedding-3-small", "u1"));
        assertThrows(IllegalArgumentException.class, () -> new OpenAiEmbeddingRequest(List.of(), "text-embedding-3-small", "u1"));

        OpenAiEmbeddingRequest embReq = new OpenAiEmbeddingRequest(List.of("text 1"), "text-embedding-3-small", "u1");
        assertEquals(1, embReq.input().size());

        OpenAiEmbeddingData embData = new OpenAiEmbeddingData(null, 0, List.of(0.1, 0.2));
        assertEquals("embedding", embData.object());
        assertEquals(2, embData.embedding().size());

        OpenAiEmbeddingResponse embResp = new OpenAiEmbeddingResponse(null, List.of(embData), "text-embedding-3-small", new OpenAiUsage(1, 0, 0, 1));
        assertEquals("list", embResp.object());
        assertEquals(1, embResp.data().size());
    }

    @Test
    @DisplayName("Tool Calling & Structured Output records should validate correctly")
    void testToolCallingRecords() {
        OpenAiToolDefinition tool = OpenAiToolDefinition.function("ast_analyzer", "Parse Java AST", Map.of("path", "String"));
        assertEquals("function", tool.type());
        assertEquals("ast_analyzer", tool.function().name());
        assertEquals("Parse Java AST", tool.function().description());
        assertEquals("String", tool.function().parameters().get("path"));

        OpenAiResponseFormat fmt = new OpenAiResponseFormat("json_schema", Map.of("strict", true));
        assertEquals("json_schema", fmt.type());
        assertEquals(true, fmt.jsonSchema().get("strict"));
    }

    @Test
    @DisplayName("Multimodal, Files, and Audio records should validate correctly")
    void testMultimodalAndAudioFiles() {
        OpenAiTranscriptionResponse audio = new OpenAiTranscriptionResponse("Transcribed directive", null, 4.5, null);
        assertEquals("Transcribed directive", audio.text());
        assertEquals("en", audio.language());
        assertEquals("transcribe", audio.task());
        assertEquals(4.5, audio.duration());

        OpenAiImageResponse imgResp = new OpenAiImageResponse(1700000000L, List.of(new OpenAiImageData("base64data", null, "revised prompt")));
        assertEquals(1, imgResp.data().size());
        assertEquals("base64data", imgResp.data().get(0).b64Json());

        OpenAiFileObject file = new OpenAiFileObject("file-1", 1024L, 1700000000L, "spec.json", null);
        assertEquals("file-1", file.id());
        assertEquals("assistants", file.purpose());

        OpenAiFileList fileList = new OpenAiFileList(null, List.of(file));
        assertEquals("list", fileList.object());
        assertEquals(1, fileList.data().size());
    }

    @Test
    @DisplayName("Deliberative Reasoning & 10-Persona Agent OS records should be verified")
    void testAgentOsRecords() {
        assertEquals(10, AgentPersona.values().length);
        for (AgentPersona persona : AgentPersona.values()) {
            assertNotNull(persona.getRoleTitle());
            assertNotNull(persona.getCoreMission());
        }

        DeliberativeReasoningTrace trace = new DeliberativeReasoningTrace(
                CognitiveStage.ANSWER_SYNTHESIS,
                3,
                true,
                0.99,
                "Synthesized hexagonal solution",
                List.of("Step 1", "Step 2")
        );
        assertEquals(CognitiveStage.ANSWER_SYNTHESIS, trace.stage());
        assertEquals(3, trace.hypothesesCount());
        assertTrue(trace.selfCritiquePassed());
        assertEquals(0.99, trace.confidenceScore());
        assertEquals(2, trace.thoughtSteps().size());

        AgentOsGoalRequest goalReq = new AgentOsGoalRequest("Build payment engine", null, null, true, null, null);
        assertEquals("repo-default", goalReq.repositoryId());
        assertEquals("engineer", goalReq.userId());
        assertTrue(goalReq.localFirstAirGapped());
        assertEquals("high", goalReq.reasoningEffort());
        assertEquals(10, goalReq.selectedPersonas().size());

        AgentOsTaskStep step = new AgentOsTaskStep("step-1", AgentPersona.ARCHITECT_AGENT, "Define boundaries", "Boundaries defined", null, 120L);
        assertEquals("step-1", step.stepId());
        assertEquals(AgentPersona.ARCHITECT_AGENT, step.persona());
        assertEquals("COMPLETED", step.status());

        AgentOsMetrics metrics = new AgentOsMetrics(1200L, 500, 45.2, true, true, 0.99);
        AgentOsGoalResponse goalResp = new AgentOsGoalResponse(
                "exec-001",
                "Build payment engine",
                null,
                trace,
                List.of(step),
                List.of(),
                metrics
        );
        assertEquals("exec-001", goalResp.executionId());
        assertEquals("COMPLETED", goalResp.status());
        assertEquals(1, goalResp.executedSteps().size());

        OpenAiResponsesRequest respReq = new OpenAiResponsesRequest(null, null, null, null);
        assertEquals("oenexa-neo-gpt-o1", respReq.model());
        assertEquals("", respReq.input());
        assertTrue(respReq.tools().isEmpty());
        assertEquals("high", respReq.reasoningEffort());

        OpenAiResponseOutputItem outputItem = new OpenAiResponseOutputItem(null, null, null);
        assertEquals("message", outputItem.type());
        assertEquals("assistant", outputItem.role());
        assertEquals("", outputItem.content());

        OpenAiResponsesResponse responsesResp = new OpenAiResponsesResponse(
                "resp-1", null, 1700000000L, null, List.of(outputItem), null, new OpenAiUsage(10, 10, 0, 20)
        );
        assertEquals("resp-1", responsesResp.id());
        assertEquals("response", responsesResp.object());
        assertEquals("completed", responsesResp.status());
        assertEquals(1, responsesResp.output().size());
    }
}
