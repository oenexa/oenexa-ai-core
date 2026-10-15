package com.oenexa.domain;

import java.time.Instant;
import java.util.List;

/**
 * Domain contracts for Interactive Chat and Context Windows.
 */
public class ChatDomain {

    public enum Role {
        SYSTEM, USER, ASSISTANT
    }

    public record Message(Role role, String content, Instant timestamp) {}

    public record Conversation(String conversationId, String userId, List<Message> messages) {}

    public record ChatRequest(String message, String conversationId) {}

    public record ChatResponse(String reply, String conversationId, int contextTokensUsed) {}
}
