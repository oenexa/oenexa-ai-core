package com.oenexa.infrastructure.mapper;

import com.oenexa.domain.model.chat.ChatDomain.Message;
import com.oenexa.domain.model.chat.ChatDomain.Role;
import com.oenexa.infrastructure.persistence.entity.ChatMessageEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Enterprise Entity Mapper Layer for Chat Messages.
 * Translates between JPA ChatMessageEntity persistence models and ChatDomain Message records.
 */
@Component
public class ChatMessageEntityMapper {

    public ChatMessageEntity toEntity(Message message, String sessionId) {
        if (message == null) {
            return null;
        }
        String id = UUID.randomUUID().toString();
        String session = (sessionId != null && !sessionId.isBlank()) ? sessionId : UUID.randomUUID().toString();
        String roleStr = message.role() != null ? message.role().name() : Role.USER.name();
        String content = message.content() != null ? message.content() : "";
        int tokens = content.length() / 4;
        String model = message.model() != null ? message.model() : "gemini-2.5-flash";

        return new ChatMessageEntity(
                id,
                session,
                roleStr,
                content,
                tokens,
                model,
                "GENERAL_AGENT",
                1.0
        );
    }

    public Message toDomain(ChatMessageEntity entity) {
        if (entity == null) {
            return null;
        }
        Role role;
        try {
            role = Role.valueOf(entity.getRole().toUpperCase());
        } catch (Exception e) {
            role = Role.USER;
        }
        return new Message(
                role,
                entity.getContent() != null ? entity.getContent() : "",
                entity.getCreatedAt() != null ? entity.getCreatedAt() : Instant.now(),
                null,
                entity.getModelUsed() != null ? entity.getModelUsed() : "gemini-2.5-flash",
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }
}

