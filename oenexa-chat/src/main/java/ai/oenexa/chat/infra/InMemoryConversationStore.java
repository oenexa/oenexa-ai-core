package ai.oenexa.chat.infra;

import ai.oenexa.chat.domain.ChatMessage;
import ai.oenexa.chat.domain.ConversationStore;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import reactor.core.publisher.Mono;

/** Keys on (tenant, conversation) so one tenant can never read another's history. */
public class InMemoryConversationStore implements ConversationStore {

    private record Key(UUID tenant, UUID conversation) {
    }

    private final Map<Key, List<ChatMessage>> data = new ConcurrentHashMap<>();

    @Override
    public Mono<List<ChatMessage>> history(UUID tenantId, UUID conversationId) {
        return Mono.fromSupplier(() -> List.copyOf(data.getOrDefault(new Key(tenantId, conversationId), List.of())));
    }

    @Override
    public Mono<Void> append(UUID tenantId, UUID conversationId, ChatMessage message) {
        return Mono.fromRunnable(() -> data
                .computeIfAbsent(new Key(tenantId, conversationId), k -> new CopyOnWriteArrayList<>())
                .add(message));
    }
}
