package com.oenexa.domain;

import java.time.Instant;
import java.util.List;

/**
 * Domain contracts for Interactive Chat, Gemini Reasoning, and Context Windows.
 */
public class ChatDomain {

    public enum Role {
        SYSTEM, USER, ASSISTANT
    }

    public record Message(
            Role role,
            String content,
            Instant timestamp,
            String thoughtProcess,
            String model
    ) {
        public Message(Role role, String content, Instant timestamp) {
            this(role, content, timestamp, null, "gemini-2.5-flash");
        }
    }

    public record Conversation(String conversationId, String userId, List<Message> messages) {}

    public record ChatRequest(
            String message,
            String conversationId,
            String model,
            boolean includeThinking
    ) {
        public ChatRequest(String message, String conversationId) {
            this(message, conversationId, "gemini-2.5-flash", true);
        }
    }

    public record ChatResponse(
            String reply,
            String conversationId,
            int contextTokensUsed,
            String thoughtProcess,
            String model,
            String intent,
            List<String> suggestedFollowUps
    ) {
        public ChatResponse(String reply, String conversationId, int contextTokensUsed) {
            this(reply, conversationId, contextTokensUsed, null, "gemini-2.5-flash", "GENERAL_ASSISTANT", List.of());
        }
    }
}
