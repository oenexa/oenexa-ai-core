package com.oenexa.application.mapper;

import com.oenexa.domain.ChatDomain.ChatResponse;
import com.oenexa.domain.ChatDomain.Message;
import com.oenexa.domain.ChatDomain.Role;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Enterprise Mapper Layer for Chat & Conversation Domain.
 * Maps between internal conversation messages and external API chat response envelopes.
 */
@Component
public class ChatDtoMapper {

    public ChatResponse toResponse(Message message, String conversationId, int tokenUsage) {
        if (message == null) {
            return null;
        }
        return new ChatResponse(
                message.content(),
                conversationId != null ? conversationId : "default-conversation",
                tokenUsage,
                message.thoughtProcess(),
                message.model() != null ? message.model() : "gemini-2.5-flash",
                "ASSISTANT_REPLY",
                List.of(),
                message.toolCalls(),
                message.artifacts(),
                0.98
        );
    }

    public Message toAssistantMessage(String reply, String thoughtProcess, String model) {
        return new Message(
                Role.ASSISTANT,
                reply != null ? reply : "",
                Instant.now(),
                thoughtProcess,
                model != null ? model : "gemini-2.5-flash",
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }
}
