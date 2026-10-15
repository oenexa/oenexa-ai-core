package ai.oenexa.agent.domain;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/** Shared blackboard for one workflow run: every agent reads what earlier agents produced. */
public class AgentContext {

    private final String objective;
    private final Map<String, String> shared = new ConcurrentHashMap<>();
    private final List<String> transcript = new CopyOnWriteArrayList<>();

    public AgentContext(String objective) {
        this.objective = objective;
    }

    public String objective() {
        return objective;
    }

    public void put(String key, String value) {
        shared.put(key, value);
    }

    public Map<String, String> shared() {
        return Map.copyOf(shared);
    }

    public void log(String line) {
        transcript.add(line);
    }

    public List<String> transcript() {
        return List.copyOf(transcript);
    }
}
