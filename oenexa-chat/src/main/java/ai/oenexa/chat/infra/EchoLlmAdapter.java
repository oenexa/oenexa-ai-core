package ai.oenexa.chat.infra;

import ai.oenexa.chat.domain.ChatMessage;
import ai.oenexa.chat.domain.LlmPort;
import java.util.Arrays;
import java.util.List;
import reactor.core.publisher.Flux;

/** Deterministic offline adapter so the platform runs end-to-end without provider keys. */
public class EchoLlmAdapter implements LlmPort {

    @Override
    public Flux<String> stream(List<ChatMessage> prompt) {
        String last = prompt.isEmpty() ? "" : prompt.getLast().content();
        return Flux.fromStream(Arrays.stream(("Echo: " + last).split("(?<= )")));
    }
}
