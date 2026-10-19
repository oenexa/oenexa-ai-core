package ai.oenexa.agent.domain;

import reactor.core.publisher.Mono;

/** Port to the LLM used by agents. Kept separate from chat so agents can use cheaper/specialised models. */
public interface AgentReasoner {

    Mono<String> think(String systemPrompt, String input);
}
