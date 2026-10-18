package ai.oenexa.chat.web;

import ai.oenexa.auth.token.JwtService.TokenClaims;
import ai.oenexa.chat.application.ChatService;
import ai.oenexa.chat.application.ChatService.ChatStreamEvent;
import ai.oenexa.chat.application.OenexaReasoningEngine;
import ai.oenexa.chat.domain.ChatMessage;
import ai.oenexa.chat.domain.ChatTool;
import ai.oenexa.chat.domain.CodeContextAttachment;
import ai.oenexa.chat.domain.ConversationMetadata;
import ai.oenexa.common.error.ErrorCode;
import ai.oenexa.common.error.OenexaException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    public record SendMessage(
            @NotBlank @Size(max = 64_000) String content,
            String model,
            Boolean includeThinking,
            List<CodeContextAttachment> attachments
    ) {
        public SendMessage(String content) {
            this(content, "oenexa-reasoning-v1", true, List.of());
        }

        public SendMessage(String content, String model, Boolean includeThinking) {
            this(content, model, includeThinking, List.of());
        }
    }

    public record CreateConversation(String title, String model) {}

    public record UpdateTitle(@NotBlank String title) {}

    public record ForkRequest(UUID newConversationId, Integer upToMessageIndex) {}

    private final ChatService service;
    private final OenexaReasoningEngine reasoningEngine;

    @Autowired
    public ChatController(ChatService service, OenexaReasoningEngine reasoningEngine) {
        this.service = service;
        this.reasoningEngine = (reasoningEngine != null) ? reasoningEngine : new OenexaReasoningEngine();
    }

    public ChatController(ChatService service) {
        this(service, new OenexaReasoningEngine());
    }

    /** Server-sent events: one event per streamed token chunk. */
    @PostMapping(value = "/{conversationId}/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> send(
            Principal auth,
            @PathVariable UUID conversationId,
            @Valid @RequestBody SendMessage body
    ) {
        return service.reply(
                tenantOf(auth),
                conversationId,
                body.content(),
                body.model(),
                body.includeThinking() == null || body.includeThinking(),
                body.attachments()
        );
    }

    /** Structured Server-Sent Events with session_start, content chunks, and done. */
    @PostMapping(value = "/{conversationId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatStreamEvent> streamEvents(
            Principal auth,
            @PathVariable UUID conversationId,
            @Valid @RequestBody SendMessage body
    ) {
        return service.streamEvents(
                tenantOf(auth),
                conversationId,
                body.content(),
                body.model(),
                body.includeThinking() == null || body.includeThinking(),
                body.attachments()
        );
    }

    /** Synchronous turn completion returning full reasoning turn, tool calls, and artifacts. */
    @PostMapping("/{conversationId}/completions")
    public Mono<OenexaReasoningEngine.TurnResult> complete(
            Principal auth,
            @PathVariable UUID conversationId,
            @Valid @RequestBody SendMessage body
    ) {
        return service.complete(
                tenantOf(auth),
                conversationId,
                body.content(),
                body.model(),
                body.includeThinking() == null || body.includeThinking(),
                body.attachments(),
                reasoningEngine
        );
    }

    @GetMapping("/{conversationId}/messages")
    public Mono<List<ChatMessage>> history(Principal auth, @PathVariable UUID conversationId) {
        return service.history(tenantOf(auth), conversationId);
    }

    @GetMapping("/conversations")
    public Mono<List<ConversationMetadata>> listConversations(Principal auth) {
        return service.listConversations(tenantOf(auth));
    }

    @PostMapping("/conversations")
    public Mono<ConversationMetadata> createConversation(
            Principal auth,
            @RequestBody(required = false) CreateConversation body
    ) {
        String title = (body != null) ? body.title() : "New Conversation";
        String model = (body != null) ? body.model() : "oenexa-reasoning-v1";
        return service.createConversation(tenantOf(auth), UUID.randomUUID(), title, model);
    }

    @GetMapping("/{conversationId}/metadata")
    public Mono<ConversationMetadata> getMetadata(Principal auth, @PathVariable UUID conversationId) {
        return service.getMetadata(tenantOf(auth), conversationId);
    }

    @PatchMapping("/{conversationId}/title")
    public Mono<ConversationMetadata> updateTitle(
            Principal auth,
            @PathVariable UUID conversationId,
            @Valid @RequestBody UpdateTitle body
    ) {
        return service.updateTitle(tenantOf(auth), conversationId, body.title());
    }

    @PatchMapping("/{conversationId}/pin")
    public Mono<ConversationMetadata> setPinned(
            Principal auth,
            @PathVariable UUID conversationId,
            @RequestParam(defaultValue = "true") boolean pinned
    ) {
        return service.setPinned(tenantOf(auth), conversationId, pinned);
    }

    @DeleteMapping("/{conversationId}")
    public Mono<Void> deleteConversation(Principal auth, @PathVariable UUID conversationId) {
        return service.deleteConversation(tenantOf(auth), conversationId);
    }

    @PostMapping("/{conversationId}/fork")
    public Mono<ConversationMetadata> forkConversation(
            Principal auth,
            @PathVariable UUID conversationId,
            @RequestBody(required = false) ForkRequest body
    ) {
        UUID newId = (body != null && body.newConversationId() != null) ? body.newConversationId() : UUID.randomUUID();
        int index = (body != null && body.upToMessageIndex() != null) ? body.upToMessageIndex() : -1;
        return service.forkConversation(tenantOf(auth), conversationId, newId, index);
    }

    @DeleteMapping("/{conversationId}/messages")
    public Mono<Void> clearHistory(Principal auth, @PathVariable UUID conversationId) {
        return service.clearHistory(tenantOf(auth), conversationId);
    }

    @GetMapping("/tools")
    public Mono<List<ChatTool>> listTools() {
        return Mono.just(reasoningEngine.toolRegistry().listTools());
    }

    private static UUID tenantOf(Principal principal) {
        if (principal instanceof Authentication auth && auth.getDetails() instanceof TokenClaims(var userId, var tenantId, var role)) {
            return tenantId;
        }
        throw new OenexaException(ErrorCode.UNAUTHORIZED, "Authentication required");
    }
}

