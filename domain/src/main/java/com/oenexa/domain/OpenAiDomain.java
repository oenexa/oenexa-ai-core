package com.oenexa.domain;

import java.time.Instant;
import java.util.*;

/**
 * Enterprise Domain Records & Invariants for OENEXA Neo-GPT Native OpenAI Engine.
 * Provides 100% wire-compatibility with OpenAI REST specification, o1/o3 deliberative reasoning,
 * and the 10-persona Autonomous Agent Operating System.
 */
public final class OpenAiDomain {

    private OpenAiDomain() {}

    // =========================================================================
    // 1. OpenAI Chat Completion Protocol
    // =========================================================================

    public record OpenAiChatCompletionRequest(
            String model,
            List<OpenAiChatMessage> messages,
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
            OpenAiResponseFormat responseFormat,
            Integer seed,
            List<OpenAiToolDefinition> tools,
            String toolChoice,
            String reasoningEffort
    ) {
        public OpenAiChatCompletionRequest {
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

        public static OpenAiChatCompletionRequest of(String model, String userMessage) {
            return new OpenAiChatCompletionRequest(
                    model,
                    List.of(OpenAiChatMessage.user(userMessage)),
                    0.7, 1.0, 1, false, List.of(), null, null, null, Map.of(), "default-user",
                    null, null, List.of(), "auto", "medium"
            );
        }
    }

    public record OpenAiChatMessage(
            String role,
            String content,
            String name,
            List<OpenAiToolCall> toolCalls,
            String toolCallId,
            String reasoningContent,
            String refusal
    ) {
        public OpenAiChatMessage {
            Objects.requireNonNull(role, "role must not be null");
            toolCalls = (toolCalls != null) ? List.copyOf(toolCalls) : List.of();
        }

        public static OpenAiChatMessage system(String content) {
            return new OpenAiChatMessage("system", content, null, List.of(), null, null, null);
        }

        public static OpenAiChatMessage user(String content) {
            return new OpenAiChatMessage("user", content, null, List.of(), null, null, null);
        }

        public static OpenAiChatMessage assistant(String content) {
            return new OpenAiChatMessage("assistant", content, null, List.of(), null, null, null);
        }

        public static OpenAiChatMessage assistantWithReasoning(String content, String reasoningContent) {
            return new OpenAiChatMessage("assistant", content, null, List.of(), null, reasoningContent, null);
        }

        public static OpenAiChatMessage assistantWithTools(String content, List<OpenAiToolCall> toolCalls) {
            return new OpenAiChatMessage("assistant", content, null, toolCalls, null, null, null);
        }

        public static OpenAiChatMessage tool(String toolCallId, String content) {
            return new OpenAiChatMessage("tool", content, null, List.of(), toolCallId, null, null);
        }
    }

    public record OpenAiChatCompletionResponse(
            String id,
            String object,
            long created,
            String model,
            List<OpenAiChoice> choices,
            OpenAiUsage usage,
            String systemFingerprint
    ) {
        public OpenAiChatCompletionResponse {
            Objects.requireNonNull(id, "id must not be null");
            Objects.requireNonNull(object, "object must not be null");
            Objects.requireNonNull(model, "model must not be null");
            Objects.requireNonNull(choices, "choices must not be null");
            Objects.requireNonNull(usage, "usage must not be null");
            choices = List.copyOf(choices);
        }

        public static OpenAiChatCompletionResponse single(
                String id,
                String model,
                OpenAiChatMessage message,
                String finishReason,
                OpenAiUsage usage
        ) {
            return new OpenAiChatCompletionResponse(
                    id,
                    "chat.completion",
                    Instant.now().getEpochSecond(),
                    model,
                    List.of(new OpenAiChoice(0, message, null, finishReason != null ? finishReason : "stop")),
                    usage,
                    "fp_oenexa_neo_gpt_25"
            );
        }
    }

    public record OpenAiChoice(
            int index,
            OpenAiChatMessage message,
            OpenAiChatMessage delta,
            String finishReason
    ) {}

    public record OpenAiUsage(
            int promptTokens,
            int completionTokens,
            int reasoningTokens,
            int totalTokens
    ) {
        public static OpenAiUsage compute(String prompt, String reply, String reasoning) {
            int p = prompt != null ? Math.max(1, prompt.length() / 4) : 0;
            int c = reply != null ? Math.max(1, reply.length() / 4) : 0;
            int r = reasoning != null ? Math.max(0, reasoning.length() / 4) : 0;
            return new OpenAiUsage(p, c, r, p + c + r);
        }
    }

    // =========================================================================
    // 2. Models & Embeddings Specification
    // =========================================================================

    public record OpenAiModel(
            String id,
            String object,
            long created,
            String ownedBy
    ) {
        public OpenAiModel {
            Objects.requireNonNull(id, "id must not be null");
            object = (object != null) ? object : "model";
            ownedBy = (ownedBy != null) ? ownedBy : "oenexa-system";
        }
    }

    public record OpenAiModelList(
            String object,
            List<OpenAiModel> data
    ) {
        public OpenAiModelList {
            object = (object != null) ? object : "list";
            data = (data != null) ? List.copyOf(data) : List.of();
        }
    }

    public record OpenAiEmbeddingRequest(
            List<String> input,
            String model,
            String user
    ) {
        public OpenAiEmbeddingRequest {
            Objects.requireNonNull(input, "input must not be null");
            if (input.isEmpty()) throw new IllegalArgumentException("input must not be empty");
            Objects.requireNonNull(model, "model must not be null");
            input = List.copyOf(input);
        }
    }

    public record OpenAiEmbeddingData(
            String object,
            int index,
            List<Double> embedding
    ) {
        public OpenAiEmbeddingData {
            object = (object != null) ? object : "embedding";
            embedding = (embedding != null) ? List.copyOf(embedding) : List.of();
        }
    }

    public record OpenAiEmbeddingResponse(
            String object,
            List<OpenAiEmbeddingData> data,
            String model,
            OpenAiUsage usage
    ) {
        public OpenAiEmbeddingResponse {
            object = (object != null) ? object : "list";
            data = (data != null) ? List.copyOf(data) : List.of();
            Objects.requireNonNull(model, "model must not be null");
            Objects.requireNonNull(usage, "usage must not be null");
        }
    }

    // =========================================================================
    // 3. Tool Calling & Structured Output Specification
    // =========================================================================

    public record OpenAiToolDefinition(
            String type,
            OpenAiFunctionDefinition function
    ) {
        public OpenAiToolDefinition {
            type = (type != null) ? type : "function";
            Objects.requireNonNull(function, "function must not be null");
        }

        public static OpenAiToolDefinition function(String name, String description, Map<String, Object> parameters) {
            return new OpenAiToolDefinition("function", new OpenAiFunctionDefinition(name, description, parameters));
        }
    }

    public record OpenAiFunctionDefinition(
            String name,
            String description,
            Map<String, Object> parameters
    ) {
        public OpenAiFunctionDefinition {
            Objects.requireNonNull(name, "name must not be null");
            description = (description != null) ? description : "";
            parameters = (parameters != null) ? Map.copyOf(parameters) : Map.of();
        }
    }

    public record OpenAiToolCall(
            String id,
            String type,
            OpenAiFunctionCall function
    ) {
        public OpenAiToolCall {
            Objects.requireNonNull(id, "id must not be null");
            type = (type != null) ? type : "function";
            Objects.requireNonNull(function, "function must not be null");
        }
    }

    public record OpenAiFunctionCall(
            String name,
            String arguments
    ) {
        public OpenAiFunctionCall {
            Objects.requireNonNull(name, "name must not be null");
            arguments = (arguments != null) ? arguments : "{}";
        }
    }

    public record OpenAiResponseFormat(
            String type,
            Map<String, Object> jsonSchema
    ) {
        public OpenAiResponseFormat {
            type = (type != null) ? type : "text";
            jsonSchema = (jsonSchema != null) ? Map.copyOf(jsonSchema) : Map.of();
        }
    }

    public record OpenAiResponsesRequest(
            String model,
            String input,
            List<OpenAiToolDefinition> tools,
            String reasoningEffort
    ) {
        public OpenAiResponsesRequest {
            model = (model != null && !model.isBlank()) ? model : "oenexa-neo-gpt-o1";
            input = (input != null) ? input : "";
            tools = (tools != null) ? List.copyOf(tools) : List.of();
            reasoningEffort = (reasoningEffort != null && !reasoningEffort.isBlank()) ? reasoningEffort : "high";
        }
    }

    public record OpenAiResponseOutputItem(
            String type,
            String role,
            String content
    ) {
        public OpenAiResponseOutputItem {
            type = (type != null) ? type : "message";
            role = (role != null) ? role : "assistant";
            content = (content != null) ? content : "";
        }
    }

    public record OpenAiResponsesResponse(
            String id,
            String object,
            long createdAt,
            String model,
            List<OpenAiResponseOutputItem> output,
            String status,
            OpenAiUsage usage
    ) {
        public OpenAiResponsesResponse {
            Objects.requireNonNull(id, "id must not be null");
            object = (object != null) ? object : "response";
            model = (model != null) ? model : "oenexa-neo-gpt-o1";
            output = (output != null) ? List.copyOf(output) : List.of();
            status = (status != null) ? status : "completed";
            Objects.requireNonNull(usage, "usage must not be null");
        }
    }

    // =========================================================================
    // 4. Multimodal & Audio/Files Specification
    // =========================================================================

    public record OpenAiTranscriptionResponse(
            String text,
            String language,
            double duration,
            String task
    ) {
        public OpenAiTranscriptionResponse {
            Objects.requireNonNull(text, "text must not be null");
            language = (language != null) ? language : "en";
            task = (task != null) ? task : "transcribe";
        }
    }

    public record OpenAiImageData(
            String b64Json,
            String url,
            String revisedPrompt
    ) {}

    public record OpenAiImageResponse(
            long created,
            List<OpenAiImageData> data
    ) {
        public OpenAiImageResponse {
            data = (data != null) ? List.copyOf(data) : List.of();
        }
    }

    public record OpenAiFileObject(
            String id,
            long bytes,
            long createdAt,
            String filename,
            String purpose
    ) {
        public OpenAiFileObject {
            Objects.requireNonNull(id, "id must not be null");
            Objects.requireNonNull(filename, "filename must not be null");
            purpose = (purpose != null) ? purpose : "assistants";
        }
    }

    public record OpenAiFileList(
            String object,
            List<OpenAiFileObject> data
    ) {
        public OpenAiFileList {
            object = (object != null) ? object : "list";
            data = (data != null) ? List.copyOf(data) : List.of();
        }
    }

    // =========================================================================
    // 5. o3 Deliberative Reasoning & Cognitive Architecture
    // =========================================================================

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

    // =========================================================================
    // 6. 10-Persona Autonomous Agent Operating System
    // =========================================================================

    public enum AgentPersona {
        ARCHITECT_AGENT("Software Architect", "Hexagonal boundaries, sealed records, C4 contracts"),
        BACKEND_AGENT("Backend Engineer", "Reactive WebFlux, Java 25 virtual threads, REST APIs"),
        FRONTEND_AGENT("Frontend Engineer", "React 18, Vite, Material UI, Monaco editor integration"),
        SECURITY_AGENT("AppSec Specialist", "OWASP Top 10, secret detection, SAST vulnerability scanner"),
        QA_AGENT("Verification Engineer", "Strictly zero-mock JUnit 5 test synthesis, 100% JaCoCo"),
        DEVOPS_AGENT("DevOps & SRE", "Multi-stage Docker packaging, Kubernetes manifests, Helm charts"),
        DATABASE_AGENT("Database Engineer", "Flyway schema migrations, PostgreSQL, pgvector indexing"),
        DOCUMENTATION_AGENT("Technical Writer", "OpenAPI 3.0 specs, Mermaid architecture diagrams"),
        REFACTORING_AGENT("Staff Refactoring Engineer", "AST complexity reduction, code smell eradication"),
        PERFORMANCE_AGENT("Performance Engineer", "Generational ZGC tuning, memory leak prevention, async I/O");

        private final String roleTitle;
        private final String coreMission;

        AgentPersona(String roleTitle, String coreMission) {
            this.roleTitle = roleTitle;
            this.coreMission = coreMission;
        }

        public String getRoleTitle() {
            return roleTitle;
        }

        public String getCoreMission() {
            return coreMission;
        }
    }

    public record AgentOsGoalRequest(
            String goal,
            String repositoryId,
            String userId,
            boolean localFirstAirGapped,
            String reasoningEffort,
            List<AgentPersona> selectedPersonas
    ) {
        public AgentOsGoalRequest {
            Objects.requireNonNull(goal, "goal must not be null");
            repositoryId = (repositoryId != null && !repositoryId.isBlank()) ? repositoryId : "repo-default";
            userId = (userId != null && !userId.isBlank()) ? userId : "engineer";
            reasoningEffort = (reasoningEffort != null && !reasoningEffort.isBlank()) ? reasoningEffort : "high";
            selectedPersonas = (selectedPersonas != null && !selectedPersonas.isEmpty())
                    ? List.copyOf(selectedPersonas)
                    : List.of(AgentPersona.values());
        }
    }

    public record AgentOsTaskStep(
            String stepId,
            AgentPersona persona,
            String action,
            String outputSnippet,
            String status,
            long durationMs
    ) {
        public AgentOsTaskStep {
            Objects.requireNonNull(stepId, "stepId must not be null");
            Objects.requireNonNull(persona, "persona must not be null");
            Objects.requireNonNull(action, "action must not be null");
            outputSnippet = (outputSnippet != null) ? outputSnippet : "";
            status = (status != null) ? status : "COMPLETED";
        }
    }

    public record AgentOsMetrics(
            long totalDurationMs,
            int tokensGenerated,
            double tokensPerSecond,
            boolean airGappedVerified,
            boolean zeroMockTestVerified,
            double confidenceScore
    ) {}

    public record AgentOsGoalResponse(
            String executionId,
            String goal,
            String status,
            DeliberativeReasoningTrace deliberativeTrace,
            List<AgentOsTaskStep> executedSteps,
            List<ChatDomain.CodeArtifact> synthesizedArtifacts,
            AgentOsMetrics metrics
    ) {
        public AgentOsGoalResponse {
            Objects.requireNonNull(executionId, "executionId must not be null");
            Objects.requireNonNull(goal, "goal must not be null");
            status = (status != null) ? status : "COMPLETED";
            Objects.requireNonNull(deliberativeTrace, "deliberativeTrace must not be null");
            executedSteps = (executedSteps != null) ? List.copyOf(executedSteps) : List.of();
            synthesizedArtifacts = (synthesizedArtifacts != null) ? List.copyOf(synthesizedArtifacts) : List.of();
            Objects.requireNonNull(metrics, "metrics must not be null");
        }
    }
}
