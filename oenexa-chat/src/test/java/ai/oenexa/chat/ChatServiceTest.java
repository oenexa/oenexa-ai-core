package ai.oenexa.chat;

import ai.oenexa.chat.application.ChatService;
import ai.oenexa.chat.application.ContextWindow;
import ai.oenexa.chat.domain.ChatMessage;
import ai.oenexa.chat.domain.ChatMessage.Role;
import ai.oenexa.chat.infra.EchoLlmAdapter;
import ai.oenexa.chat.infra.InMemoryConversationStore;
import ai.oenexa.chat.infra.SpringAiLlmAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class ChatServiceTest {

    private final UUID tenant = UUID.randomUUID();
    private final UUID conversation = UUID.randomUUID();
    private final InMemoryConversationStore store = new InMemoryConversationStore();
    private final ChatService service = new ChatService(store, new EchoLlmAdapter(), "sys", 1000);

    @Test
    void streamsReplyAndPersistsBothTurns() {
        StepVerifier.create(service.reply(tenant, conversation, "hello world").collectList())
                .assertNext(chunks -> assertThat(String.join("", chunks)).isEqualTo("Echo: hello world"))
                .verifyComplete();

        StepVerifier.create(service.history(tenant, conversation))
                .assertNext(h -> {
                    assertThat(h).extracting(ChatMessage::role).containsExactly(Role.USER, Role.ASSISTANT);
                    assertThat(h.get(1).content()).isEqualTo("Echo: hello world");
                })
                .verifyComplete();
    }

    @Test
    void historyIsIsolatedPerTenant() {
        service.reply(tenant, conversation, "secret").blockLast();
        StepVerifier.create(service.history(UUID.randomUUID(), conversation))
                .assertNext(h -> assertThat(h).isEmpty())
                .verifyComplete();
    }

    @Test
    void contextWindowKeepsSystemPromptAndNewestTurns() {
        List<ChatMessage> msgs = new ArrayList<>();
        msgs.add(ChatMessage.of(Role.SYSTEM, "s".repeat(40)));
        for (int i = 0; i < 10; i++) {
            msgs.add(ChatMessage.of(Role.USER, ("m" + i).repeat(20)));
        }
        List<ChatMessage> fitted = ContextWindow.fit(msgs, 40);
        assertThat(fitted.get(0).role()).isEqualTo(Role.SYSTEM);
        assertThat(fitted.get(fitted.size() - 1).content()).startsWith("m9");
        assertThat(fitted.size()).isLessThan(msgs.size());
    }

    @Test
    void contextWindowAlwaysKeepsNewestMessageEvenIfOversized() {
        List<ChatMessage> msgs = List.of(ChatMessage.of(Role.USER, "x".repeat(4000)));
        assertThat(ContextWindow.fit(msgs, 10)).hasSize(1);
    }

    @Test
    void mapsDomainRolesToSpringAiMessages() {
        assertThat(SpringAiLlmAdapter.toSpringAi(ChatMessage.of(Role.SYSTEM, "a")).getMessageType().getValue())
                .isEqualTo("system");
        assertThat(SpringAiLlmAdapter.toSpringAi(ChatMessage.of(Role.USER, "a")).getMessageType().getValue())
                .isEqualTo("user");
        assertThat(SpringAiLlmAdapter.toSpringAi(ChatMessage.of(Role.ASSISTANT, "a")).getMessageType().getValue())
                .isEqualTo("assistant");
    }

    @Test
    void echoAdapterHandlesEmptyPrompt() {
        StepVerifier.create(new EchoLlmAdapter().stream(List.of()).collectList())
                .assertNext(c -> assertThat(String.join("", c)).isEqualTo("Echo: "))
                .verifyComplete();
    }
}
