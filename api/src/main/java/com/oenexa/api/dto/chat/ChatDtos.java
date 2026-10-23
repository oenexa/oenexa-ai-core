package com.oenexa.api.dto.chat;

import java.time.Instant;
import java.util.List;

/**
 * Standardized DTO Layer for Chat & Conversation Domain.
 */
public class ChatDtos {

    public record SendMessageRequestDto(
            String message,
            String conversationId,
            String model,
            boolean includeThinking
    ) {
        public SendMessageRequestDto {
            message = (message != null) ? message : "";
            model = (model != null && !model.isBlank()) ? model : "gemini-2.5-flash";
        }
    }

    public record ChatTurnResponseDto(
            String reply,
            String conversationId,
            String thoughtProcess,
            String modelUsed,
            int tokensUsed,
            double confidenceScore,
            Instant timestamp
    ) {}

    public record ConversationSummaryDto(
            String conversationId,
            String title,
            String model,
            int messageCount,
            boolean pinned,
            Instant updatedAt
    ) {}
}
