package ai.oenexa.chat.domain;

import java.time.Instant;

public record ChatMessage(Role role, String content, Instant createdAt) {

    public enum Role { SYSTEM, USER, ASSISTANT }

    public static ChatMessage of(Role role, String content) {
        return new ChatMessage(role, content, Instant.now());
    }

    /** Rough token estimate (~4 chars/token); good enough for budgeting, not billing. */
    public int estimatedTokens() {
        return Math.max(1, content.length() / 4);
    }
}
