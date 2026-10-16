package ai.oenexa.chat.domain;

import java.time.Instant;

public record ChatMessage(
        Role role,
        String content,
        Instant createdAt,
        String thoughtProcess,
        String model
) {

    public enum Role { SYSTEM, USER, ASSISTANT }

    public ChatMessage(Role role, String content, Instant createdAt) {
        this(role, content, createdAt, null, "gemini-2.5-flash");
    }

    public static ChatMessage of(Role role, String content) {
        return new ChatMessage(role, content, Instant.now(), null, "gemini-2.5-flash");
    }

    public static ChatMessage of(Role role, String content, String thoughtProcess) {
        return new ChatMessage(role, content, Instant.now(), thoughtProcess, "gemini-2.5-flash");
    }

    public static ChatMessage of(Role role, String content, String thoughtProcess, String model) {
        return new ChatMessage(role, content, Instant.now(), thoughtProcess, model);
    }

    /** Rough token estimate (~4 chars/token); good enough for budgeting, not billing. */
    public int estimatedTokens() {
        int contentTokens = Math.max(1, content.length() / 4);
        int thoughtTokens = (thoughtProcess != null && !thoughtProcess.isBlank()) ? thoughtProcess.length() / 4 : 0;
        return contentTokens + thoughtTokens;
    }
}

