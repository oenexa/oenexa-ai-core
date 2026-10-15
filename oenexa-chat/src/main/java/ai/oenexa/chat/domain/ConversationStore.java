package ai.oenexa.chat.domain;

import java.util.List;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface ConversationStore {

    Mono<List<ChatMessage>> history(UUID tenantId, UUID conversationId);

    Mono<Void> append(UUID tenantId, UUID conversationId, ChatMessage message);
}
