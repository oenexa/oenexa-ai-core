package ai.oenexa.chat.domain;

import java.time.Instant;
import java.util.List;

public record ChatMessage(
        Role role,
        String content,
        Instant createdAt,
        String thoughtProcess,
        String model,
        List<CodeContextAttachment> attachments,
        List<ToolCall> toolCalls,
        List<ToolResult> toolResults,
        List<CodeArtifact> artifacts
) {

    public enum Role { SYSTEM, USER, ASSISTANT, TOOL }

    public ChatMessage(Role role, String content, Instant createdAt, String thoughtProcess, String model) {
        this(role, content, createdAt, thoughtProcess, model, List.of(), List.of(), List.of(), List.of());
    }

    public ChatMessage(Role role, String content, Instant createdAt) {
        this(role, content, createdAt, null, "oenexa-reasoning-v1", List.of(), List.of(), List.of(), List.of());
    }

    public static ChatMessage of(Role role, String content) {
        return new ChatMessage(role, content, Instant.now(), null, "oenexa-reasoning-v1", List.of(), List.of(), List.of(), List.of());
    }

    public static ChatMessage of(Role role, String content, String thoughtProcess) {
        return new ChatMessage(role, content, Instant.now(), thoughtProcess, "oenexa-reasoning-v1", List.of(), List.of(), List.of(), List.of());
    }

    public static ChatMessage of(Role role, String content, String thoughtProcess, String model) {
        return new ChatMessage(role, content, Instant.now(), thoughtProcess, model, List.of(), List.of(), List.of(), List.of());
    }

    public static ChatMessage of(
            Role role,
            String content,
            String thoughtProcess,
            String model,
            List<CodeContextAttachment> attachments,
            List<ToolCall> toolCalls,
            List<ToolResult> toolResults,
            List<CodeArtifact> artifacts
    ) {
        return new ChatMessage(role, content, Instant.now(), thoughtProcess, model, attachments, toolCalls, toolResults, artifacts);
    }

    /** Rough token estimate (~4 chars/token); good enough for budgeting, not billing. */
    public int estimatedTokens() {
        int contentTokens = Math.max(1, content.length() / 4);
        int thoughtTokens = (thoughtProcess != null && !thoughtProcess.isBlank()) ? thoughtProcess.length() / 4 : 0;
        int attachmentTokens = 0;
        if (attachments != null) {
            for (CodeContextAttachment att : attachments) {
                if (att != null && att.content() != null) {
                    attachmentTokens += Math.max(1, att.content().length() / 4);
                }
            }
        }
        return contentTokens + thoughtTokens + attachmentTokens;
    }
}


