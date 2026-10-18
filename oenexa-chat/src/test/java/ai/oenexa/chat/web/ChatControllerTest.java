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

        var customBody = new ChatController.SendMessage("Custom message", "oenexa-neo-gpt-o3-max", false);
        assertThat(customBody.content()).isEqualTo("Custom message");
        assertThat(customBody.model()).isEqualTo("oenexa-neo-gpt-o3-max");
        assertThat(customBody.includeThinking()).isFalse();
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

    @Test
    void streamEventsAndCompletionsFlow() {
        UUID conversationId = UUID.randomUUID();
        var attachment = new ai.oenexa.chat.domain.CodeContextAttachment("src/Main.java", "public class Main {}");
        var body = new ChatController.SendMessage("Perform security audit", "oenexa-neo-gpt-o3-max", true, List.of(attachment));

        StepVerifier.create(controller.streamEvents(auth, conversationId, body))
                .assertNext(event -> {
                    assertThat(event.event()).isEqualTo("session_start");
                    assertThat(event.data()).isEqualTo(conversationId.toString());
                })
                .thenConsumeWhile(event -> !event.event().equals("done"))
                .assertNext(event -> {
                    assertThat(event.event()).isEqualTo("done");
                    assertThat(event.data()).isEqualTo("SUCCESS");
                })
                .verifyComplete();

        StepVerifier.create(controller.complete(auth, conversationId, body))
                .assertNext(turn -> {
                    assertThat(turn.intent()).isEqualTo("SECURITY_AUDIT");
                    assertThat(turn.thought()).contains("Attached Context Evaluated");
                    assertThat(turn.thought()).contains("src/Main.java");
                    assertThat(turn.toolCalls()).isNotEmpty();
                    assertThat(turn.toolResults()).isNotEmpty();
                    assertThat(turn.confidenceScore()).isEqualTo(0.98);
                })
                .verifyComplete();
    }

    @Test
    void conversationManagementEndpoints() {
        // Create conversation
        StepVerifier.create(controller.createConversation(auth, new ChatController.CreateConversation("Sprint Planning", "oenexa-neo-gpt-o3-max")))
                .assertNext(meta -> {
                    assertThat(meta.title()).isEqualTo("Sprint Planning");
                    assertThat(meta.model()).isEqualTo("oenexa-neo-gpt-o3-max");
                })
                .verifyComplete();

        // Default create
        StepVerifier.create(controller.createConversation(auth, null))
                .assertNext(meta -> assertThat(meta.title()).isEqualTo("New Conversation"))
                .verifyComplete();

        // List conversations
        StepVerifier.create(controller.listConversations(auth))
                .assertNext(list -> assertThat(list).hasSize(2))
                .verifyComplete();

        UUID convId = UUID.randomUUID();

        // Get metadata
        StepVerifier.create(controller.getMetadata(auth, convId))
                .assertNext(meta -> assertThat(meta.conversationId()).isEqualTo(convId))
                .verifyComplete();

        // Update title
        StepVerifier.create(controller.updateTitle(auth, convId, new ChatController.UpdateTitle("Architecture Refactor")))
                .assertNext(meta -> assertThat(meta.title()).isEqualTo("Architecture Refactor"))
                .verifyComplete();

        // Pin conversation
        StepVerifier.create(controller.setPinned(auth, convId, true))
                .assertNext(meta -> assertThat(meta.pinned()).isTrue())
                .verifyComplete();

        // Fork conversation
        UUID forkedId = UUID.randomUUID();
        StepVerifier.create(controller.forkConversation(auth, convId, new ChatController.ForkRequest(forkedId, 0)))
                .assertNext(meta -> assertThat(meta.conversationId()).isEqualTo(forkedId))
                .verifyComplete();

        // Fork with null request
        StepVerifier.create(controller.forkConversation(auth, convId, null))
                .assertNext(meta -> assertThat(meta.conversationId()).isNotNull())
                .verifyComplete();

        // Clear history
        StepVerifier.create(controller.clearHistory(auth, convId))
                .verifyComplete();

        // Delete conversation
        StepVerifier.create(controller.deleteConversation(auth, convId))
                .verifyComplete();

        // List tools (public, no auth required)
        StepVerifier.create(controller.listTools())
                .assertNext(tools -> assertThat(tools).isNotEmpty())
                .verifyComplete();
    }

    @Test
    void unauthorizedPrincipalAcrossNewEndpoints() {
        UUID conversationId = UUID.randomUUID();
        var body = new ChatController.SendMessage("test");
        Principal anonymous = () -> "anon";

        assertThatThrownBy(() -> controller.streamEvents(anonymous, conversationId, body))
                .isInstanceOf(OenexaException.class);
        assertThatThrownBy(() -> controller.complete(anonymous, conversationId, body))
                .isInstanceOf(OenexaException.class);
        assertThatThrownBy(() -> controller.listConversations(anonymous))
                .isInstanceOf(OenexaException.class);
        assertThatThrownBy(() -> controller.createConversation(anonymous, null))
                .isInstanceOf(OenexaException.class);
        assertThatThrownBy(() -> controller.getMetadata(anonymous, conversationId))
                .isInstanceOf(OenexaException.class);
        assertThatThrownBy(() -> controller.updateTitle(anonymous, conversationId, new ChatController.UpdateTitle("title")))
                .isInstanceOf(OenexaException.class);
        assertThatThrownBy(() -> controller.setPinned(anonymous, conversationId, true))
                .isInstanceOf(OenexaException.class);
        assertThatThrownBy(() -> controller.deleteConversation(anonymous, conversationId))
                .isInstanceOf(OenexaException.class);
        assertThatThrownBy(() -> controller.forkConversation(anonymous, conversationId, null))
                .isInstanceOf(OenexaException.class);
        assertThatThrownBy(() -> controller.clearHistory(anonymous, conversationId))
                .isInstanceOf(OenexaException.class);
    }
}

