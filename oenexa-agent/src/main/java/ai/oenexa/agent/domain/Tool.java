package ai.oenexa.agent.domain;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import reactor.core.publisher.Mono;

/** A capability agents can invoke (git clone, static analysis, kubectl read-only, ...). */
public interface Tool {

    String name();

    Mono<String> call(String input);

    final class Registry {
        private final Map<String, Tool> tools = new ConcurrentHashMap<>();

        public Registry register(Tool tool) {
            tools.put(tool.name(), tool);
            return this;
        }

        public Mono<String> call(String name, String input) {
            Tool tool = tools.get(name);
            return tool == null ? Mono.error(new IllegalArgumentException("Unknown tool: " + name)) : tool.call(input);
        }

        public Collection<String> names() {
            return tools.keySet();
        }
    }
}
