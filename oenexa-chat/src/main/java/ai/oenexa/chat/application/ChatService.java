package ai.oenexa.chat.application;

import ai.oenexa.chat.domain.ChatMessage;
import ai.oenexa.chat.domain.CodeContextAttachment;
import ai.oenexa.chat.domain.ConversationMetadata;
import ai.oenexa.chat.domain.ConversationStore;
import ai.oenexa.chat.domain.LlmPort;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ChatService {

    public record ChatStreamEvent(String event, String data) {}

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
        return reply(tenantId, conversationId, userText, "gemini-2.5-flash", true, List.of());
    }

    public Flux<String> reply(
            UUID tenantId,
            UUID conversationId,
            String userText,
            String model,
            boolean includeThinking,
            List<CodeContextAttachment> attachments
    ) {
        ChatMessage user = ChatMessage.of(
                ChatMessage.Role.USER,
                userText,
                null,
                (model != null && !model.isBlank()) ? model : "gemini-2.5-flash",
                (attachments != null) ? attachments : List.of(),
                List.of(),
                List.of(),
                List.of()
        );

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
                                            ChatMessage.of(ChatMessage.Role.ASSISTANT, full.toString(), null, model))
                                    .then(Mono.empty())));
                });
    }

    /** Streams structured SSE events: session_start, chunks, and done. */
    public Flux<ChatStreamEvent> streamEvents(
            UUID tenantId,
            UUID conversationId,
            String userText,
            String model,
            boolean includeThinking,
            List<CodeContextAttachment> attachments
    ) {
        return Flux.concat(
                Flux.just(new ChatStreamEvent("session_start", conversationId.toString())),
                reply(tenantId, conversationId, userText, model, includeThinking, attachments)
                        .map(chunk -> new ChatStreamEvent("content", chunk)),
                Flux.just(new ChatStreamEvent("done", "SUCCESS"))
        );
    }

    /** Synchronous turn completion returning full reasoning turn and persisting conversation turns. */
    public Mono<GeminiReasoningEngine.TurnResult> complete(
            UUID tenantId,
            UUID conversationId,
            String userText,
            String model,
            boolean includeThinking,
            List<CodeContextAttachment> attachments,
            GeminiReasoningEngine engine
    ) {
        String activeModel = (model != null && !model.isBlank()) ? model : "gemini-2.5-flash";
        ChatMessage user = ChatMessage.of(
                ChatMessage.Role.USER,
                userText,
                null,
                activeModel,
                (attachments != null) ? attachments : List.of(),
                List.of(),
                List.of(),
                List.of()
        );

        GeminiReasoningEngine reasoningEngine = (engine != null) ? engine : new GeminiReasoningEngine();
        GeminiReasoningEngine.TurnResult turn = reasoningEngine.process(userText, activeModel, includeThinking, attachments);

        ChatMessage assistant = ChatMessage.of(
                ChatMessage.Role.ASSISTANT,
                turn.response(),
                turn.thought(),
                activeModel,
                List.of(),
                turn.toolCalls(),
                turn.toolResults(),
                turn.artifacts()
        );

        return store.append(tenantId, conversationId, user)
                .then(store.append(tenantId, conversationId, assistant))
                .thenReturn(turn);
    }

    // --- Conversation Management Delegates ---

    public Mono<List<ChatMessage>> history(UUID tenantId, UUID conversationId) {
        return store.history(tenantId, conversationId);
    }

    public Mono<List<ConversationMetadata>> listConversations(UUID tenantId) {
        return store.listConversations(tenantId);
    }

    public Mono<ConversationMetadata> getMetadata(UUID tenantId, UUID conversationId) {
        return store.getMetadata(tenantId, conversationId);
    }

    public Mono<ConversationMetadata> createConversation(UUID tenantId, UUID conversationId, String title, String model) {
        return store.createConversation(tenantId, conversationId, title, model);
    }

    public Mono<ConversationMetadata> updateTitle(UUID tenantId, UUID conversationId, String newTitle) {
        return store.updateTitle(tenantId, conversationId, newTitle);
    }

    public Mono<ConversationMetadata> setPinned(UUID tenantId, UUID conversationId, boolean pinned) {
        return store.setPinned(tenantId, conversationId, pinned);
    }

    public Mono<Void> deleteConversation(UUID tenantId, UUID conversationId) {
        return store.deleteConversation(tenantId, conversationId);
    }

    public Mono<ConversationMetadata> forkConversation(UUID tenantId, UUID sourceId, UUID newId, int upToMessageIndex) {
        return store.forkConversation(tenantId, sourceId, newId, upToMessageIndex);
    }

    public Mono<Void> clearHistory(UUID tenantId, UUID conversationId) {
        return store.clearHistory(tenantId, conversationId);
    }
}

