package ai.oenexa.chat.domain;

import java.util.List;
import reactor.core.publisher.Flux;

/** Outbound port to any LLM provider. Adapters: Spring AI (production), echo (local/dev). */
public interface LlmPort {

    Flux<String> stream(List<ChatMessage> prompt);
}
