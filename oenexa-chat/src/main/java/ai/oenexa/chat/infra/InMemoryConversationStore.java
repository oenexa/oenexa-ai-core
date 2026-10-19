package ai.oenexa.chat.infra;

import ai.oenexa.chat.domain.ChatMessage;
import ai.oenexa.chat.domain.ConversationMetadata;
import ai.oenexa.chat.domain.ConversationStore;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
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
    private final Map<Key, ConversationMetadata> metadataMap = new ConcurrentHashMap<>();

    @Override
    public Mono<List<ChatMessage>> history(UUID tenantId, UUID conversationId) {
        return Mono.fromSupplier(() -> List.copyOf(data.getOrDefault(new Key(tenantId, conversationId), List.of())));
    }

    @Override
    public Mono<Void> append(UUID tenantId, UUID conversationId, ChatMessage message) {
        return Mono.fromRunnable(() -> {
            Key key = new Key(tenantId, conversationId);
            data.computeIfAbsent(key, _ -> new CopyOnWriteArrayList<>()).add(message);
            metadataMap.compute(key, (_, existing) -> {
                if (existing == null) {
                    String title = (message.content() != null && !message.content().isBlank())
                            ? (message.content().length() > 40 ? message.content().substring(0, 40) + "..." : message.content())
                            : "New Conversation";
                    return new ConversationMetadata(conversationId, tenantId, title, message.model(), Instant.now(), Instant.now(), 1, false);
                }
                return existing.withIncrementedCount();
            });
        });
    }

    @Override
    public Mono<List<ConversationMetadata>> listConversations(UUID tenantId) {
        return Mono.fromSupplier(() -> {
            List<ConversationMetadata> result = new ArrayList<>();
            for (Map.Entry<Key, ConversationMetadata> entry : metadataMap.entrySet()) {
                if (entry.getKey().tenant().equals(tenantId)) {
                    result.add(entry.getValue());
                }
            }
            result.sort(Comparator
                    .comparing(ConversationMetadata::pinned).reversed()
                    .thenComparing(ConversationMetadata::updatedAt, Comparator.reverseOrder()));
            return List.copyOf(result);
        });
    }

    @Override
    public Mono<ConversationMetadata> getMetadata(UUID tenantId, UUID conversationId) {
        return Mono.fromSupplier(() -> {
            Key key = new Key(tenantId, conversationId);
            return metadataMap.computeIfAbsent(key, _ ->
                    new ConversationMetadata(conversationId, tenantId, "New Conversation", "oenexa-reasoning-v1", Instant.now(), Instant.now(), 0, false)
            );
        });
    }

    @Override
    public Mono<ConversationMetadata> createConversation(UUID tenantId, UUID conversationId, String title, String model) {
        return Mono.fromSupplier(() -> {
            Key key = new Key(tenantId, conversationId);
            String safeTitle = (title != null && !title.isBlank()) ? title.trim() : "New Conversation";
            String safeModel = (model != null && !model.isBlank()) ? model.trim() : "oenexa-reasoning-v1";
            ConversationMetadata meta = new ConversationMetadata(conversationId, tenantId, safeTitle, safeModel, Instant.now(), Instant.now(), 0, false);
            metadataMap.put(key, meta);
            data.put(key, new CopyOnWriteArrayList<>());
            return meta;
        });
    }

    @Override
    public Mono<ConversationMetadata> updateTitle(UUID tenantId, UUID conversationId, String newTitle) {
        return Mono.fromSupplier(() -> {
            Key key = new Key(tenantId, conversationId);
            ConversationMetadata meta = metadataMap.get(key);
            if (meta == null) {
                meta = new ConversationMetadata(conversationId, tenantId, newTitle, "oenexa-reasoning-v1", Instant.now(), Instant.now(), 0, false);
            } else {
                meta = meta.withTitle(newTitle);
            }
            metadataMap.put(key, meta);
            return meta;
        });
    }

    @Override
    public Mono<ConversationMetadata> setPinned(UUID tenantId, UUID conversationId, boolean pinned) {
        return Mono.fromSupplier(() -> {
            Key key = new Key(tenantId, conversationId);
            ConversationMetadata meta = metadataMap.get(key);
            if (meta == null) {
                meta = new ConversationMetadata(conversationId, tenantId, "New Conversation", "oenexa-reasoning-v1", Instant.now(), Instant.now(), 0, pinned);
            } else {
                meta = meta.withPinned(pinned);
            }
            metadataMap.put(key, meta);
            return meta;
        });
    }

    @Override
    public Mono<Void> deleteConversation(UUID tenantId, UUID conversationId) {
        return Mono.fromRunnable(() -> {
            Key key = new Key(tenantId, conversationId);
            data.remove(key);
            metadataMap.remove(key);
        });
    }

    @Override
    public Mono<ConversationMetadata> forkConversation(UUID tenantId, UUID sourceConversationId, UUID newConversationId, int upToMessageIndex) {
        return Mono.fromSupplier(() -> {
            Key sourceKey = new Key(tenantId, sourceConversationId);
            Key newKey = new Key(tenantId, newConversationId);

            List<ChatMessage> sourceMsgs = data.getOrDefault(sourceKey, List.of());
            List<ChatMessage> forkedMsgs = new CopyOnWriteArrayList<>();
            int limit = (upToMessageIndex >= 0 && upToMessageIndex < sourceMsgs.size()) ? upToMessageIndex + 1 : sourceMsgs.size();
            for (int i = 0; i < limit; i++) {
                forkedMsgs.add(sourceMsgs.get(i));
            }
            data.put(newKey, forkedMsgs);

            ConversationMetadata sourceMeta = metadataMap.get(sourceKey);
            String title = (sourceMeta != null) ? "Fork of " + sourceMeta.title() : "Forked Conversation";
            String model = (sourceMeta != null) ? sourceMeta.model() : "oenexa-reasoning-v1";
            ConversationMetadata newMeta = new ConversationMetadata(
                    newConversationId,
                    tenantId,
                    title,
                    model,
                    Instant.now(),
                    Instant.now(),
                    forkedMsgs.size(),
                    false
            );
            metadataMap.put(newKey, newMeta);
            return newMeta;
        });
    }

    @Override
    public Mono<Void> clearHistory(UUID tenantId, UUID conversationId) {
        return Mono.fromRunnable(() -> {
            Key key = new Key(tenantId, conversationId);
            List<ChatMessage> msgs = data.get(key);
            if (msgs != null) {
                msgs.clear();
            }
            metadataMap.computeIfPresent(key, (_, meta) ->
                    new ConversationMetadata(meta.conversationId(), meta.tenantId(), meta.title(), meta.model(), meta.createdAt(), Instant.now(), 0, meta.pinned())
            );
        });
    }
}

