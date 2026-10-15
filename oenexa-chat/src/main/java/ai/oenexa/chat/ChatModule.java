package ai.oenexa.chat;

import ai.oenexa.chat.application.ChatService;
import ai.oenexa.chat.domain.ConversationStore;
import ai.oenexa.chat.domain.LlmPort;
import ai.oenexa.chat.infra.EchoLlmAdapter;
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

    /** Spring AI when a provider starter is configured and enabled; otherwise the offline echo adapter. */
    @Bean
    @ConditionalOnMissingBean(LlmPort.class)
    LlmPort llmPort(ObjectProvider<ChatClient.Builder> builder,
                    @Value("${oenexa.chat.provider:echo}") String provider) {
        ChatClient.Builder b = builder.getIfAvailable();
        return "spring-ai".equals(provider) && b != null ? new SpringAiLlmAdapter(b) : new EchoLlmAdapter();
    }

    @Bean
    ChatService chatService(ConversationStore store, LlmPort llm,
                            @Value("${oenexa.chat.system-prompt:You are OENEXA AI, a precise engineering assistant.}")
                            String systemPrompt,
                            @Value("${oenexa.chat.token-budget:24000}") int tokenBudget) {
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
        return "Multi-turn streaming conversations with long-context windowing.";
    }

    @Override
    public List<String> capabilities() {
        return List.of("streaming", "multi-turn", "context-window", "tenant-isolated-history");
    }

    @Override
    public ModuleStatus status() {
        return ModuleStatus.ACTIVE;
    }
}
