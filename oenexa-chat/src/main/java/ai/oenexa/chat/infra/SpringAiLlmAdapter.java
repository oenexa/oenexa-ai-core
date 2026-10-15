package ai.oenexa.chat.infra;

import ai.oenexa.chat.domain.ChatMessage;
import ai.oenexa.chat.domain.LlmPort;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import reactor.core.publisher.Flux;

/** Production adapter over Spring AI's ChatClient (provider chosen by Spring AI starter on the classpath). */
public class SpringAiLlmAdapter implements LlmPort {

    private final ChatClient client;

    public SpringAiLlmAdapter(ChatClient.Builder builder) {
        this.client = builder.build();
    }

    @Override
    public Flux<String> stream(List<ChatMessage> prompt) {
        List<Message> messages = prompt.stream().map(SpringAiLlmAdapter::toSpringAi).toList();
        return client.prompt().messages(messages).stream().content();
    }

    public static Message toSpringAi(ChatMessage m) {
        return switch (m.role()) {
            case SYSTEM -> new SystemMessage(m.content());
            case USER -> new UserMessage(m.content());
            case ASSISTANT -> new AssistantMessage(m.content());
        };
    }
}
