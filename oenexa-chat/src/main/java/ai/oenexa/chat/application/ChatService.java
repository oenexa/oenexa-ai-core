package ai.oenexa.chat.application;

import ai.oenexa.chat.domain.ChatMessage;
import ai.oenexa.chat.domain.ConversationStore;
import ai.oenexa.chat.domain.LlmPort;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ChatService {

    private final ConversationStore store;
    private final LlmPort llm;
    private final String systemPrompt;
    private final int tokenBudget;

    public ChatService(ConversationStore store, LlmPort llm, String systemPrompt, int tokenBudget) {
        this.store = store;
        this.llm = llm;
        this.systemPrompt = systemPrompt;
        this.tokenBudget = tokenBudget;
    }

    /** Streams the assistant reply; persists the user turn first and the full assistant turn on completion. */
    public Flux<String> reply(UUID tenantId, UUID conversationId, String userText) {
        ChatMessage user = ChatMessage.of(ChatMessage.Role.USER, userText);
        return store.append(tenantId, conversationId, user)
                .then(store.history(tenantId, conversationId))
                .flatMapMany(history -> {
                    List<ChatMessage> prompt = new ArrayList<>();
                    prompt.add(ChatMessage.of(ChatMessage.Role.SYSTEM, systemPrompt));
                    prompt.addAll(history);
                    StringBuilder full = new StringBuilder();
                    return llm.stream(ContextWindow.fit(prompt, tokenBudget))
                            .doOnNext(full::append)
                            .concatWith(Mono.defer(() -> store
                                    .append(tenantId, conversationId,
                                            ChatMessage.of(ChatMessage.Role.ASSISTANT, full.toString()))
                                    .then(Mono.empty())));
                });
    }

    public Mono<List<ChatMessage>> history(UUID tenantId, UUID conversationId) {
        return store.history(tenantId, conversationId);
    }
}
