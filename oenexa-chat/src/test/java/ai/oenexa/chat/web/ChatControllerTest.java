package ai.oenexa.chat.web;

import ai.oenexa.auth.domain.Role;
import ai.oenexa.auth.token.JwtService.TokenClaims;
import ai.oenexa.chat.application.ChatService;
import ai.oenexa.chat.domain.ChatMessage;
import ai.oenexa.chat.infra.EchoLlmAdapter;
import ai.oenexa.chat.infra.InMemoryConversationStore;
import ai.oenexa.common.error.OenexaException;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChatControllerTest {

    private ChatService service;
    private ChatController controller;
    private UUID tenantId;
    private UsernamePasswordAuthenticationToken auth;

    @BeforeEach
    void setUp() {
        service = new ChatService(new InMemoryConversationStore(), new EchoLlmAdapter(), "System prompt", 4000);
        controller = new ChatController(service);
        tenantId = UUID.randomUUID();
        var claims = new TokenClaims(UUID.randomUUID(), tenantId, Role.DEVELOPER);
        auth = new UsernamePasswordAuthenticationToken("user", "pass", List.of());
        auth.setDetails(claims);
    }

    @Test
    void sendAndHistoryFlow() {
        UUID conversationId = UUID.randomUUID();
        var body = new ChatController.SendMessage("Hello from test");

        StepVerifier.create(controller.send(auth, conversationId, body))
                .thenConsumeWhile(s -> true)
                .verifyComplete();

        StepVerifier.create(controller.history(auth, conversationId))
                .assertNext(messages -> {
                    assertThat(messages).hasSize(2);
                    assertThat(messages.get(0).content()).isEqualTo("Hello from test");
                    assertThat(messages.get(1).content()).isEqualTo("Echo: Hello from test");
                })
                .verifyComplete();
    }

    @Test
    void invalidPrincipalThrowsUnauthorized() {
        UUID conversationId = UUID.randomUUID();
        var body = new ChatController.SendMessage("Hello");
        Principal anonymous = () -> "anon";

        assertThatThrownBy(() -> controller.send(anonymous, conversationId, body))
                .isInstanceOf(OenexaException.class);

        assertThatThrownBy(() -> controller.send(null, conversationId, body))
                .isInstanceOf(OenexaException.class);

        assertThatThrownBy(() -> controller.history(anonymous, conversationId))
                .isInstanceOf(OenexaException.class);

        assertThatThrownBy(() -> controller.history(null, conversationId))
                .isInstanceOf(OenexaException.class);

        var badAuth = new UsernamePasswordAuthenticationToken("user", "pass", List.of());
        badAuth.setDetails("not-claims");
        assertThatThrownBy(() -> controller.send(badAuth, conversationId, body))
                .isInstanceOf(OenexaException.class);
    }
}
