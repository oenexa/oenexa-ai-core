package ai.oenexa.chat;

import ai.oenexa.chat.application.ChatService;
import ai.oenexa.chat.application.OenexaReasoningEngine;
import ai.oenexa.chat.domain.ConversationStore;
import ai.oenexa.chat.domain.LlmPort;
import ai.oenexa.chat.infra.EchoLlmAdapter;
import ai.oenexa.chat.infra.InMemoryConversationStore;
import ai.oenexa.chat.infra.OenexaLocalLlmAdapter;
import ai.oenexa.chat.infra.SpringAiLlmAdapter;
import ai.oenexa.chat.web.ChatController;
import ai.oenexa.common.module.PlatformModule;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(ChatController.class)
public class ChatModule implements PlatformModule {

    @Bean
    @ConditionalOnMissingBean(ConversationStore.class)
    ConversationStore conversationStore() {
        return new InMemoryConversationStore();
    }

    @Bean
    @ConditionalOnMissingBean(OenexaReasoningEngine.class)
    OenexaReasoningEngine oenexaReasoningEngine() {
        return new OenexaReasoningEngine();
    }

    /**
     * Advanced LLM Port: Defaults to OENEXA local-first provider with autonomous offline reasoning fallback.
     * Also supports spring-ai and echo providers.
     */
    @Bean
    @ConditionalOnMissingBean(LlmPort.class)
    LlmPort llmPort(ObjectProvider<ChatClient.Builder> builder,
                    OenexaReasoningEngine reasoningEngine,
                    @Value("${oenexa.chat.provider:oenexa}") String provider,
                    @Value("${oenexa.chat.local.api-key:${OENEXA_API_KEY:}}") String apiKey,
                    @Value("${oenexa.chat.local.model:oenexa-reasoning-v1}") String model) {
        if ("spring-ai".equalsIgnoreCase(provider)) {
            ChatClient.Builder b = builder.getIfAvailable();
            if (b != null) {
                return new SpringAiLlmAdapter(b);
            }
        } else if ("echo".equalsIgnoreCase(provider)) {
            return new EchoLlmAdapter();
        }
        return new OenexaLocalLlmAdapter(apiKey, model, reasoningEngine);
    }

    @Bean
    ChatService chatService(ConversationStore store, LlmPort llm,
                            @Value("${oenexa.chat.system-prompt:You are OENEXA AI, an autonomous software engineering assistant.}")
                            String systemPrompt,
                            @Value("${oenexa.chat.token-budget:32000}") int tokenBudget) {
        return new ChatService(store, llm, systemPrompt, tokenBudget);
    }

    @Override
    public String id() {
        return "chat";
    }

    @Override
    public String displayName() {
        return "OENEXA Chat";
    }

    @Override
    public String description() {
        return "OENEXA multi-turn streaming conversations with Chain-of-Thought deliberative reasoning and long-context compaction.";
    }

    @Override
    public List<String> capabilities() {
        return List.of("oenexa-reasoning-v1", "oenexa-neo-gpt-o3-max", "chain-of-thought", "streaming", "multi-turn", "context-compaction", "tenant-isolated-history");
    }

    @Override
    public ModuleStatus status() {
        return ModuleStatus.ACTIVE;
    }
}
