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

import java.time.Instant;
import java.util.*;

/**
 * Enterprise Native Autonomous Cognitive Domain Records & Invariants for OENEXA Neo-GPT.
 * Models pure-own deliberative reasoning, multi-stage cognitive state, autonomous agent OS,
 * dense vector embeddings, and zero-mock software engineering invariants.
 */
public final class OenexaCognitiveDomain {

    private OenexaCognitiveDomain() {}

    // =========================================================================
    // 1. OENEXA Deliberative Cognitive Protocol
    // =========================================================================

    public record OenexaCognitiveRequest(
            String model,
            List<OenexaCognitiveMessage> messages,
            Double temperature,
            Double topP,
            Integer n,
            Boolean stream,
            List<String> stop,
            Integer maxTokens,
            Double presencePenalty,
            Double frequencyPenalty,
            Map<String, Integer> logitBias,
            String user,
            OenexaResponseFormat responseFormat,
            Integer seed,
            List<OenexaToolDefinition> tools,
            String toolChoice,
            String reasoningEffort
    ) {
        public OenexaCognitiveRequest {
            Objects.requireNonNull(model, "model must not be null");
            Objects.requireNonNull(messages, "messages must not be null");
            if (messages.isEmpty()) {
                throw new IllegalArgumentException("messages must not be empty");
            }
            messages = List.copyOf(messages);
            stop = (stop != null) ? List.copyOf(stop) : List.of();
            tools = (tools != null) ? List.copyOf(tools) : List.of();
            logitBias = (logitBias != null) ? Map.copyOf(logitBias) : Map.of();
            temperature = (temperature != null) ? temperature : 0.7;
            topP = (topP != null) ? topP : 1.0;
            n = (n != null) ? n : 1;
            stream = (stream != null) ? stream : false;
            reasoningEffort = (reasoningEffort != null && !reasoningEffort.isBlank()) ? reasoningEffort : "medium";
        }

        public static OenexaCognitiveRequest of(String model, String userMessage) {
            return new OenexaCognitiveRequest(
                    model,
                    List.of(OenexaCognitiveMessage.user(userMessage)),
                    0.7, 1.0, 1, false, List.of(), null, null, null, Map.of(), "default-user",
                    null, null, List.of(), "auto", "medium"
            );
        }
    }

    public record OenexaCognitiveMessage(
            String role,
            String content,
            String name,
            List<OenexaToolCall> toolCalls,
            String toolCallId,
            String reasoningContent,
            String refusal
    ) {
        public OenexaCognitiveMessage {
            Objects.requireNonNull(role, "role must not be null");
            toolCalls = (toolCalls != null) ? List.copyOf(toolCalls) : List.of();
        }

        public static OenexaCognitiveMessage system(String content) {
            return new OenexaCognitiveMessage("system", content, null, List.of(), null, null, null);
        }

        public static OenexaCognitiveMessage user(String content) {
            return new OenexaCognitiveMessage("user", content, null, List.of(), null, null, null);
        }

        public static OenexaCognitiveMessage assistant(String content) {
            return new OenexaCognitiveMessage("assistant", content, null, List.of(), null, null, null);
        }

        public static OenexaCognitiveMessage assistantWithReasoning(String content, String reasoningContent) {
            return new OenexaCognitiveMessage("assistant", content, null, List.of(), null, reasoningContent, null);
        }

        public static OenexaCognitiveMessage tool(String toolCallId, String content) {
            return new OenexaCognitiveMessage("tool", content, null, List.of(), toolCallId, null, null);
        }
    }

    public record OenexaToolDefinition(String type, OenexaFunctionDefinition function) {
        public OenexaToolDefinition {
            type = (type != null) ? type : "function";
            Objects.requireNonNull(function, "function must not be null");
        }

        public static OenexaToolDefinition function(String name, String description, Map<String, Object> parameters) {
            return new OenexaToolDefinition("function", new OenexaFunctionDefinition(name, description, parameters, true));
        }
    }

    public record OenexaFunctionDefinition(
            String name,
            String description,
            Map<String, Object> parameters,
            Boolean strict
    ) {
        public OenexaFunctionDefinition {
            Objects.requireNonNull(name, "name must not be null");
            parameters = (parameters != null) ? Map.copyOf(parameters) : Map.of();
        }
    }

    public record OenexaToolCall(String id, String type, OenexaFunctionCall function) {
        public OenexaToolCall {
            Objects.requireNonNull(id, "id must not be null");
            type = (type != null) ? type : "function";
            Objects.requireNonNull(function, "function must not be null");
        }
    }

    public record OenexaFunctionCall(String name, String arguments) {
        public OenexaFunctionCall {
            Objects.requireNonNull(name, "name must not be null");
            arguments = (arguments != null) ? arguments : "{}";
        }
    }

    public record OenexaResponseFormat(String type, Map<String, Object> jsonSchema) {
        public OenexaResponseFormat {
            type = (type != null) ? type : "text";
            jsonSchema = (jsonSchema != null) ? Map.copyOf(jsonSchema) : Map.of();
        }
    }

    public record OenexaCognitiveChoice(
            int index,
            OenexaCognitiveMessage message,
            String finishReason,
            OenexaCognitiveMessage delta
    ) {
        public static OenexaCognitiveChoice completed(int index, OenexaCognitiveMessage message) {
            return new OenexaCognitiveChoice(index, message, "stop", null);
        }

        public static OenexaCognitiveChoice streaming(int index, OenexaCognitiveMessage delta) {
            return new OenexaCognitiveChoice(index, null, null, delta);
        }
    }

    public record OenexaCognitiveUsage(
            int promptTokens,
            int completionTokens,
            int reasoningTokens,
            int totalTokens
    ) {
        public static OenexaCognitiveUsage compute(String prompt, String completion, String reasoning) {
            int p = (prompt != null) ? Math.max(1, prompt.length() / 4) : 0;
            int c = (completion != null) ? Math.max(1, completion.length() / 4) : 0;
            int r = (reasoning != null) ? Math.max(0, reasoning.length() / 4) : 0;
            return new OenexaCognitiveUsage(p, c + r, r, p + c + r);
        }
    }

    public record OenexaCognitiveResponse(
            String id,
            String object,
            long created,
            String model,
            List<OenexaCognitiveChoice> choices,
            OenexaCognitiveUsage usage,
            String systemFingerprint
    ) {
        public OenexaCognitiveResponse {
            Objects.requireNonNull(id, "id must not be null");
            object = (object != null) ? object : "chat.completion";
            choices = (choices != null) ? List.copyOf(choices) : List.of();
            usage = (usage != null) ? usage : new OenexaCognitiveUsage(0, 0, 0, 0);
            systemFingerprint = (systemFingerprint != null) ? systemFingerprint : "oenexa-neo-gpt-v3";
        }

        public static OenexaCognitiveResponse single(String model, OenexaCognitiveMessage message, OenexaCognitiveUsage usage) {
            return new OenexaCognitiveResponse(
                    "oenexa-cog-" + UUID.randomUUID(),
                    "chat.completion",
                    Instant.now().getEpochSecond(),
                    model,
                    List.of(OenexaCognitiveChoice.completed(0, message)),
                    usage,
                    "oenexa-neo-gpt-v3"
            );
        }
    }

    // =========================================================================
    // 2. OENEXA Model Catalog
    // =========================================================================

    public record OenexaModelDescriptor(
            String id,
            String object,
            long created,
            String ownedBy,
            String description,
            int contextWindowTokens,
            boolean sovereignInProcess
    ) {
        public OenexaModelDescriptor {
            Objects.requireNonNull(id, "id must not be null");
            object = (object != null) ? object : "model";
            ownedBy = (ownedBy != null) ? ownedBy : "oenexa-sovereign-ai";
        }

        public static List<OenexaModelDescriptor> nativeCatalog() {
            return List.of(
                    new OenexaModelDescriptor("oenexa-neo-gpt-o3-max", "model", 1700000000L, "oenexa-sovereign-ai", "OENEXA Neo-GPT o3 Max Deliberative Reasoning Engine (Native In-Process)", 128000, true),
                    new OenexaModelDescriptor("oenexa-neo-gpt-coder-32b", "model", 1700000000L, "oenexa-sovereign-ai", "OENEXA Neo-GPT Autonomous Code Synthesis & Refactoring", 65536, true),
                    new OenexaModelDescriptor("oenexa-neo-gpt-architect-70b", "model", 1700000000L, "oenexa-sovereign-ai", "OENEXA Neo-GPT Deep Architectural Invariants & C4 Modeling", 131072, true),
                    new OenexaModelDescriptor("oenexa-neo-gpt-reasoning-14b", "model", 1700000000L, "oenexa-sovereign-ai", "OENEXA Neo-GPT High-Speed Tree-of-Thought Explorer", 32768, true)
            );
        }
    }

    // =========================================================================
    // 3. Dense Embeddings & Vector Operations
    // =========================================================================

    public record OenexaEmbeddingRequest(List<String> input, String model, String user) {
        public OenexaEmbeddingRequest {
            Objects.requireNonNull(input, "input must not be null");
            if (input.isEmpty()) {
                throw new IllegalArgumentException("input must not be empty");
            }
            input = List.copyOf(input);
            model = (model != null && !model.isBlank()) ? model : "oenexa-embedding-v3";
        }
    }

    public record OenexaEmbeddingData(String object, int index, List<Double> embedding) {
        public OenexaEmbeddingData {
            object = (object != null) ? object : "embedding";
            embedding = (embedding != null) ? List.copyOf(embedding) : List.of();
        }
    }

    public record OenexaEmbeddingResponse(
            String object,
            List<OenexaEmbeddingData> data,
            String model,
            OenexaCognitiveUsage usage
    ) {
        public OenexaEmbeddingResponse {
            object = (object != null) ? object : "list";
            data = (data != null) ? List.copyOf(data) : List.of();
            usage = (usage != null) ? usage : new OenexaCognitiveUsage(0, 0, 0, 0);
        }
    }

    // =========================================================================
    // 4. Structured Cognitive Responses API
    // =========================================================================

    public record OenexaResponsesRequest(
            String model,
            String input,
            List<OenexaCognitiveMessage> instructions,
            String reasoningEffort
    ) {
        public OenexaResponsesRequest {
            model = (model != null && !model.isBlank()) ? model : "oenexa-neo-gpt-o3-max";
            input = (input != null) ? input : "";
            instructions = (instructions != null) ? List.copyOf(instructions) : List.of();
            reasoningEffort = (reasoningEffort != null && !reasoningEffort.isBlank()) ? reasoningEffort : "high";
        }
    }

    public record OenexaResponseOutputItem(String type, String role, String content) {
        public OenexaResponseOutputItem {
            type = (type != null) ? type : "message";
            role = (role != null) ? role : "assistant";
            content = (content != null) ? content : "";
        }
    }

    public record OenexaResponsesResponse(
            String id,
            String object,
            long createdAt,
            String model,
            List<OenexaResponseOutputItem> output,
            String status,
            OenexaCognitiveUsage usage
    ) {
        public OenexaResponsesResponse {
            Objects.requireNonNull(id, "id must not be null");
            object = (object != null) ? object : "response";
            output = (output != null) ? List.copyOf(output) : List.of();
            status = (status != null) ? status : "completed";
            usage = (usage != null) ? usage : new OenexaCognitiveUsage(0, 0, 0, 0);
        }
    }

    // =========================================================================
    // 5. Native Multimodal & Sandbox Artifacts
    // =========================================================================

    public record OenexaTranscriptionResponse(String text, String language, double durationSeconds, String task) {
        public OenexaTranscriptionResponse {
            Objects.requireNonNull(text, "text must not be null");
            language = (language != null) ? language : "en";
            task = (task != null) ? task : "transcribe";
        }
    }

    public record OenexaDiagramData(String b64Json, String url, String revisedPrompt) {}

    public record OenexaDiagramResponse(long created, List<OenexaDiagramData> data) {
        public OenexaDiagramResponse {
            data = (data != null) ? List.copyOf(data) : List.of();
        }
    }

    public record OenexaFileDescriptor(String id, long bytes, long createdAt, String filename, String purpose) {
        public OenexaFileDescriptor {
            Objects.requireNonNull(id, "id must not be null");
            Objects.requireNonNull(filename, "filename must not be null");
            purpose = (purpose != null) ? purpose : "engineering";
        }
    }

    public record OenexaFileListResponse(String object, List<OenexaFileDescriptor> data) {
        public OenexaFileListResponse {
            object = (object != null) ? object : "list";
            data = (data != null) ? List.copyOf(data) : List.of();
        }
    }

    // =========================================================================
    // 6. o3 Deliberative Multi-Stage Cognitive Architecture
    // =========================================================================

    public enum OenexaCognitiveStage {
        INTENT_UNDERSTANDING,
        MULTI_HYPOTHESIS_EXPLORATION,
        DEEP_ANALYTICAL_REASONING,
        SELF_CRITIQUE_ENGINE,
        ANSWER_SYNTHESIS
    }

    public record OenexaDeliberativeTrace(
            OenexaCognitiveStage stage,
            int hypothesesCount,
            boolean selfCritiquePassed,
            double confidenceScore,
            String reasoningSummary,
            List<String> thoughtSteps
    ) {
        public OenexaDeliberativeTrace {
            Objects.requireNonNull(stage, "stage must not be null");
            reasoningSummary = (reasoningSummary != null) ? reasoningSummary : "";
            thoughtSteps = (thoughtSteps != null) ? List.copyOf(thoughtSteps) : List.of();
        }
    }
}

