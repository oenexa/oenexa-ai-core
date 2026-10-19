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

    @Test
    void contextWindowUtilityConstructor() throws Exception {
        var constructor = ContextWindow.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        var instance = constructor.newInstance();
        assertThat(instance).isNotNull();
    }

    @Test
    void chatMessageEstimatedTokensAndCreation() {
        var msg = ChatMessage.of(Role.USER, "12345678");
        assertThat(msg.estimatedTokens()).isEqualTo(2);
        assertThat(msg.createdAt()).isNotNull();

        var shortMsg = ChatMessage.of(Role.SYSTEM, "");
        assertThat(shortMsg.estimatedTokens()).isEqualTo(1);
    }

    @Test
    void springAiLlmAdapterWithRealChatModel() {
        org.springframework.ai.chat.model.ChatModel stubModel = new org.springframework.ai.chat.model.ChatModel() {
            @Override
            public org.springframework.ai.chat.model.ChatResponse call(org.springframework.ai.chat.prompt.Prompt prompt) {
                return new org.springframework.ai.chat.model.ChatResponse(
                        List.of(new org.springframework.ai.chat.model.Generation(new org.springframework.ai.chat.messages.AssistantMessage("call-response"))));
            }

            @Override
            public reactor.core.publisher.Flux<org.springframework.ai.chat.model.ChatResponse> stream(org.springframework.ai.chat.prompt.Prompt prompt) {
                return reactor.core.publisher.Flux.just(
                        new org.springframework.ai.chat.model.ChatResponse(
                                List.of(new org.springframework.ai.chat.model.Generation(new org.springframework.ai.chat.messages.AssistantMessage("streaming-chunk")))));
            }
        };

        var builder = org.springframework.ai.chat.client.ChatClient.builder(stubModel);
        var adapter = new SpringAiLlmAdapter(builder);
        var messages = List.of(
                ChatMessage.of(Role.SYSTEM, "sys"),
                ChatMessage.of(Role.USER, "hello"),
                ChatMessage.of(Role.ASSISTANT, "hi")
        );

        StepVerifier.create(adapter.stream(messages))
                .assertNext(chunk -> assertThat(chunk).isEqualTo("streaming-chunk"))
                .verifyComplete();
    }

    @Test
    void chatModuleConfigurationAndBeanDefinitions() {
        var module = new ChatModule();
        assertThat(module.id()).isEqualTo("chat");
        assertThat(module.displayName()).isEqualTo("OENEXA Chat");
        assertThat(module.description()).contains("OENEXA");
        assertThat(module.capabilities()).contains("oenexa-reasoning-v1", "chain-of-thought", "streaming", "multi-turn", "tenant-isolated-history");
        assertThat(module.status()).isEqualTo(ai.oenexa.common.module.PlatformModule.ModuleStatus.ACTIVE);

        var store = module.conversationStore();
        assertThat(store).isInstanceOf(InMemoryConversationStore.class);

        var engine = module.oenexaReasoningEngine();
        assertThat(engine).isNotNull();

        org.springframework.beans.factory.ObjectProvider<org.springframework.ai.chat.client.ChatClient.Builder> emptyProvider =
                new org.springframework.beans.factory.ObjectProvider<>() {
                    @Override public org.springframework.ai.chat.client.ChatClient.Builder getObject(Object... args) { return null; }
                    @Override public org.springframework.ai.chat.client.ChatClient.Builder getIfAvailable() { return null; }
                    @Override public org.springframework.ai.chat.client.ChatClient.Builder getIfUnique() { return null; }
                    @Override public org.springframework.ai.chat.client.ChatClient.Builder getObject() { return null; }
                };

        var oenexaLlm = module.llmPort(emptyProvider, engine, "oenexa", "", "oenexa-reasoning-v1");
        assertThat(oenexaLlm).isInstanceOf(ai.oenexa.chat.infra.OenexaLocalLlmAdapter.class);

        var echoLlm = module.llmPort(emptyProvider, engine, "echo", "", "oenexa-reasoning-v1");
        assertThat(echoLlm).isInstanceOf(EchoLlmAdapter.class);

        var defaultFallbackLlm = module.llmPort(emptyProvider, engine, "spring-ai", "", "oenexa-reasoning-v1");
        assertThat(defaultFallbackLlm).isInstanceOf(ai.oenexa.chat.infra.OenexaLocalLlmAdapter.class);

        org.springframework.ai.chat.model.ChatModel stubModel = prompt -> null;
        var validBuilder = org.springframework.ai.chat.client.ChatClient.builder(stubModel);
        org.springframework.beans.factory.ObjectProvider<org.springframework.ai.chat.client.ChatClient.Builder> validProvider =
                new org.springframework.beans.factory.ObjectProvider<>() {
                    @Override public org.springframework.ai.chat.client.ChatClient.Builder getObject(Object... args) { return validBuilder; }
                    @Override public org.springframework.ai.chat.client.ChatClient.Builder getIfAvailable() { return validBuilder; }
                    @Override public org.springframework.ai.chat.client.ChatClient.Builder getIfUnique() { return validBuilder; }
                    @Override public org.springframework.ai.chat.client.ChatClient.Builder getObject() { return validBuilder; }
                };

        var springAiLlm = module.llmPort(validProvider, engine, "spring-ai", "", "oenexa-reasoning-v1");
        assertThat(springAiLlm).isInstanceOf(SpringAiLlmAdapter.class);

        var chatService = module.chatService(store, oenexaLlm, "Custom prompt", 32000);
        assertThat(chatService).isNotNull();
    }

    @Test
    void contextWindowCompactionSummarizesDroppedTurns() {
        List<ChatMessage> msgs = new ArrayList<>();
        msgs.add(ChatMessage.of(Role.SYSTEM, "sys".repeat(10)));
        for (int i = 0; i < 8; i++) {
            msgs.add(ChatMessage.of(Role.USER, ("turn" + i).repeat(20)));
        }
        List<ChatMessage> compacted = ContextWindow.fitWithCompaction(msgs, 40);
        assertThat(compacted.size()).isLessThan(msgs.size());
        assertThat(compacted.stream().anyMatch(m -> m.content().contains("OENEXA Long-Context Compactor"))).isTrue();
    }

    @Test
    void chatMessageAdvancedConstructorsAndTokenEstimation() {
        var legacyMsg = new ChatMessage(Role.USER, "legacy", java.time.Instant.now());
        assertThat(legacyMsg.thoughtProcess()).isNull();
        assertThat(legacyMsg.model()).isEqualTo("oenexa-reasoning-v1");

        var msgWithThought = ChatMessage.of(Role.ASSISTANT, "content", "thinking");
        assertThat(msgWithThought.thoughtProcess()).isEqualTo("thinking");
        assertThat(msgWithThought.model()).isEqualTo("oenexa-reasoning-v1");
        assertThat(msgWithThought.estimatedTokens()).isGreaterThan(msgWithThought.content().length() / 4);

        var fullMsg = ChatMessage.of(Role.ASSISTANT, "code", "thought", "oenexa-neo-gpt-o3-max");
        assertThat(fullMsg.model()).isEqualTo("oenexa-neo-gpt-o3-max");
    }
}
