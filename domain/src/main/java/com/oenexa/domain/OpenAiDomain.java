package com.oenexa.domain;

import com.oenexa.domain.OpenAiToolProtocolDomain.OpenAiResponseFormat;
import com.oenexa.domain.OpenAiToolProtocolDomain.OpenAiToolCall;
import com.oenexa.domain.OpenAiToolProtocolDomain.OpenAiToolDefinition;

import java.time.Instant;
import java.util.*;

/**
 * Enterprise Domain Records & Invariants for OENEXA Neo-GPT Native OpenAI Engine.
 * Provides 100% wire-compatibility with OpenAI Chat Completion and Model/Embedding protocols.
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
}
