package com.oenexa.domain;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Domain contracts for Interactive Chat, Gemini Reasoning, Tool Calling, and Context Windows.
 */
public class ChatDomain {

    public enum Role {
        SYSTEM, USER, ASSISTANT, TOOL
    }

    public record ToolCall(String callId, String toolName, Map<String, Object> arguments) {
        public ToolCall(String callId, String toolName) {
            this(callId, toolName, Map.of());
        }
    }

    public record ToolResult(String callId, String toolName, String output, boolean isError) {
        public ToolResult(String callId, String toolName, String output) {
            this(callId, toolName, output, false);
        }
    }

    public record CodeContextAttachment(
            String filePath,
            String language,
            int startLine,
            int endLine,
            String content
    ) {
        public CodeContextAttachment(String filePath, String content) {
            this(filePath, "plaintext", 1, 1, content);
        }
    }

    public record CodeArtifact(
            String artifactId,
            String title,
            String filePath,
            String language,
            String codeContent,
            String action
    ) {
        public CodeArtifact(String title, String filePath, String codeContent) {
            this(java.util.UUID.randomUUID().toString(), title, filePath, "java", codeContent, "CREATE");
        }
    }

    public record ConversationMetadata(
            String conversationId,
            String tenantId,
            String userId,
            String title,
            String model,
            Instant createdAt,
            Instant updatedAt,
            int messageCount,
            boolean pinned
    ) {
        public ConversationMetadata(String conversationId, String title) {
            this(conversationId, "default-tenant", "default-user", title, "gemini-2.5-flash", Instant.now(), Instant.now(), 0, false);
        }
    }

    public record Message(
            Role role,
            String content,
            Instant timestamp,
            String thoughtProcess,
            String model,
            List<CodeContextAttachment> attachments,
            List<ToolCall> toolCalls,
            List<ToolResult> toolResults,
            List<CodeArtifact> artifacts
    ) {
        public Message(Role role, String content, Instant timestamp) {
            this(role, content, timestamp, null, "gemini-2.5-flash", List.of(), List.of(), List.of(), List.of());
        }

        public Message(Role role, String content, Instant timestamp, String thoughtProcess, String model) {
            this(role, content, timestamp, thoughtProcess, model, List.of(), List.of(), List.of(), List.of());
        }
    }

    public record Conversation(String conversationId, String userId, List<Message> messages) {}

    public record ChatRequest(
            String message,
            String conversationId,
            String model,
            boolean includeThinking,
            List<CodeContextAttachment> attachments
    ) {
        public ChatRequest(String message, String conversationId) {
            this(message, conversationId, "gemini-2.5-flash", true, List.of());
        }

        public ChatRequest(String message, String conversationId, String model, boolean includeThinking) {
            this(message, conversationId, model, includeThinking, List.of());
        }
    }

    public record ChatResponse(
            String reply,
            String conversationId,
            int contextTokensUsed,
            String thoughtProcess,
            String model,
            String intent,
            List<String> suggestedFollowUps,
            List<ToolCall> toolCalls,
            List<CodeArtifact> artifacts,
            double confidenceScore
    ) {
        public ChatResponse(String reply, String conversationId, int contextTokensUsed) {
            this(reply, conversationId, contextTokensUsed, null, "gemini-2.5-flash", "GENERAL_ASSISTANT", List.of(), List.of(), List.of(), 0.95);
        }

        public ChatResponse(
                String reply,
                String conversationId,
                int contextTokensUsed,
                String thoughtProcess,
                String model,
                String intent,
                List<String> suggestedFollowUps
        ) {
            this(reply, conversationId, contextTokensUsed, thoughtProcess, model, intent, suggestedFollowUps, List.of(), List.of(), 0.95);
        }
    }
}

