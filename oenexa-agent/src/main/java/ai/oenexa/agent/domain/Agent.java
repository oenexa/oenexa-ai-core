package ai.oenexa.agent.domain;

import java.util.Map;
import java.util.Optional;
import reactor.core.publisher.Mono;

public interface Agent {

    AgentType type();

    Mono<Result> execute(Task task, AgentContext context);

    record Task(AgentType agent, String objective) {
    }

    /** Outcome of one agent step; may hand follow-up work to another agent. */
    record Result(String summary, Map<String, String> artifacts, Optional<Delegation> delegation) {
        public static Result of(String summary) {
            return new Result(summary, Map.of(), Optional.empty());
        }
    }

    record Delegation(AgentType to, String objective) {
    }
}
