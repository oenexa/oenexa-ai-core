package ai.oenexa.chat;

import ai.oenexa.chat.application.ChatService;
import ai.oenexa.chat.application.GeminiReasoningEngine;
import ai.oenexa.chat.domain.ConversationStore;
import ai.oenexa.chat.domain.LlmPort;
import ai.oenexa.chat.infra.EchoLlmAdapter;
import ai.oenexa.chat.infra.GeminiLlmAdapter;
import ai.oenexa.chat.infra.InMemoryConversationStore;
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
    @ConditionalOnMissingBean(GeminiReasoningEngine.class)
    GeminiReasoningEngine geminiReasoningEngine() {
        return new GeminiReasoningEngine();
    }

    /**
     * Advanced LLM Port: Defaults to Gemini 2.5 provider with autonomous offline fallback.
     * Also supports spring-ai and echo providers.
     */
    @Bean
    @ConditionalOnMissingBean(LlmPort.class)
    LlmPort llmPort(ObjectProvider<ChatClient.Builder> builder,
                    GeminiReasoningEngine reasoningEngine,
                    @Value("${oenexa.chat.provider:gemini}") String provider,
                    @Value("${oenexa.chat.gemini.api-key:${GEMINI_API_KEY:}}") String apiKey,
                    @Value("${oenexa.chat.gemini.model:gemini-2.5-flash}") String model) {
        if ("spring-ai".equalsIgnoreCase(provider)) {
            ChatClient.Builder b = builder.getIfAvailable();
            if (b != null) {
                return new SpringAiLlmAdapter(b);
            }
        } else if ("echo".equalsIgnoreCase(provider)) {
            return new EchoLlmAdapter();
        }
        return new GeminiLlmAdapter(apiKey, model, reasoningEngine);
    }

    @Bean
    ChatService chatService(ConversationStore store, LlmPort llm,
                            @Value("${oenexa.chat.system-prompt:You are OENEXA AI, a precise Gemini-grade engineering assistant.}")
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
        return "Gemini-grade multi-turn streaming conversations with Chain-of-Thought reasoning and long-context compaction.";
    }

    @Override
    public List<String> capabilities() {
        return List.of("gemini-2.5-flash", "gemini-2.5-pro", "chain-of-thought", "streaming", "multi-turn", "context-compaction", "tenant-isolated-history");
    }

    @Override
    public ModuleStatus status() {
        return ModuleStatus.ACTIVE;
    }
}
