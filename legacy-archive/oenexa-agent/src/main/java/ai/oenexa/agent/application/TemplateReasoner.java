package ai.oenexa.agent.application;

import ai.oenexa.agent.domain.AgentReasoner;
import reactor.core.publisher.Mono;

/** Offline reasoner used until an LLM-backed reasoner is configured. Deterministic and side-effect free. */
public class TemplateReasoner implements AgentReasoner {

    @Override
    public Mono<String> think(String systemPrompt, String input) {
        String firstLine = input.lines().findFirst().orElse("");
        return Mono.just("Completed: " + systemPrompt.split("[.;]")[0] + " (" + firstLine + ")");
    }
}
