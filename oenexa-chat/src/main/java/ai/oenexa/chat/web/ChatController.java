package ai.oenexa.chat.web;

import ai.oenexa.auth.token.JwtService.TokenClaims;
import ai.oenexa.chat.application.ChatService;
import ai.oenexa.chat.domain.ChatMessage;
import ai.oenexa.common.error.ErrorCode;
import ai.oenexa.common.error.OenexaException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import java.security.Principal;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    public record SendMessage(@NotBlank @Size(max = 32_000) String content) {
    }

    private final ChatService service;

    public ChatController(ChatService service) {
        this.service = service;
    }

    /** Server-sent events: one event per streamed token chunk. */
    @PostMapping(value = "/{conversationId}/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> send(Principal auth, @PathVariable UUID conversationId,
                             @Valid @RequestBody SendMessage body) {
        return service.reply(tenantOf(auth), conversationId, body.content());
    }

    @GetMapping("/{conversationId}/messages")
    public Mono<List<ChatMessage>> history(Principal auth, @PathVariable UUID conversationId) {
        return service.history(tenantOf(auth), conversationId);
    }

    private static UUID tenantOf(Principal principal) {
        if (principal instanceof Authentication auth && auth.getDetails() instanceof TokenClaims(var userId, var tenantId, var role)) {
            return tenantId;
        }
        throw new OenexaException(ErrorCode.UNAUTHORIZED, "Authentication required");
    }
}
