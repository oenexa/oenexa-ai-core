package ai.oenexa.chat.domain;

import java.time.Instant;
import java.util.UUID;

public record ConversationMetadata(
        UUID conversationId,
        UUID tenantId,
        String title,
        String model,
        Instant createdAt,
        Instant updatedAt,
        int messageCount,
        boolean pinned
) {
    public ConversationMetadata(UUID conversationId, UUID tenantId, String title) {
        this(conversationId, tenantId, title, "oenexa-reasoning-v1", Instant.now(), Instant.now(), 0, false);
    }

    public ConversationMetadata withIncrementedCount() {
        return new ConversationMetadata(
                conversationId,
                tenantId,
                title,
                model,
                createdAt,
                Instant.now(),
                messageCount + 1,
                pinned
        );
    }

    public ConversationMetadata withTitle(String newTitle) {
        return new ConversationMetadata(
                conversationId,
                tenantId,
                newTitle,
                model,
                createdAt,
                Instant.now(),
                messageCount,
                pinned
        );
    }

    public ConversationMetadata withPinned(boolean newPinned) {
        return new ConversationMetadata(
                conversationId,
                tenantId,
                title,
                model,
                createdAt,
                updatedAt,
                messageCount,
                newPinned
        );
    }
}
