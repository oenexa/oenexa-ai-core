package com.oenexa.api.mapper;

import com.oenexa.api.dto.chat.ChatDtos.ChatTurnResponseDto;
import com.oenexa.api.dto.chat.ChatDtos.ConversationSummaryDto;
import com.oenexa.api.dto.chat.ChatDtos.SendMessageRequestDto;
import com.oenexa.domain.model.chat.ChatDomain.ChatRequest;
import com.oenexa.domain.model.chat.ChatDomain.ChatResponse;
import com.oenexa.domain.model.chat.ChatDomain.ConversationMetadata;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Enterprise API Mapper for Chat & Conversation DTOs.
 */
@Component
public class ChatApiMapper {

    public ChatRequest toDomainRequest(SendMessageRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return new ChatRequest(dto.message(), dto.conversationId(), dto.model(), dto.includeThinking(), List.of());
    }

    public ChatTurnResponseDto toTurnResponseDto(ChatResponse response) {
        if (response == null) {
            return null;
        }
        return new ChatTurnResponseDto(
                response.reply(),
                response.conversationId(),
                response.thoughtProcess(),
                response.model(),
                response.contextTokensUsed(),
                response.confidenceScore(),
                Instant.now()
        );
    }

    public ConversationSummaryDto toSummaryDto(ConversationMetadata metadata) {
        if (metadata == null) {
            return null;
        }
        return new ConversationSummaryDto(
                metadata.conversationId(),
                metadata.title(),
                metadata.model(),
                metadata.messageCount(),
                metadata.pinned(),
                metadata.updatedAt()
        );
    }
}

