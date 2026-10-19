package ai.oenexa.chat.domain;

import java.util.List;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface ConversationStore {

    Mono<List<ChatMessage>> history(UUID tenantId, UUID conversationId);

    Mono<Void> append(UUID tenantId, UUID conversationId, ChatMessage message);

    Mono<List<ConversationMetadata>> listConversations(UUID tenantId);

    Mono<ConversationMetadata> getMetadata(UUID tenantId, UUID conversationId);

    Mono<ConversationMetadata> createConversation(UUID tenantId, UUID conversationId, String title, String model);

    Mono<ConversationMetadata> updateTitle(UUID tenantId, UUID conversationId, String newTitle);

    Mono<ConversationMetadata> setPinned(UUID tenantId, UUID conversationId, boolean pinned);

    Mono<Void> deleteConversation(UUID tenantId, UUID conversationId);

    Mono<ConversationMetadata> forkConversation(UUID tenantId, UUID sourceConversationId, UUID newConversationId, int upToMessageIndex);

    Mono<Void> clearHistory(UUID tenantId, UUID conversationId);
}

