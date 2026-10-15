package ai.oenexa.agent.domain;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/** Bounded per-agent memory; oldest entries are evicted first. */
public class AgentMemory {

    private final int capacity;
    private final Deque<String> entries = new ArrayDeque<>();

    public AgentMemory(int capacity) {
        this.capacity = capacity;
    }

    public synchronized void remember(String entry) {
        if (entries.size() == capacity) {
            entries.removeFirst();
        }
        entries.addLast(entry);
    }

    public synchronized List<String> recall() {
        return List.copyOf(entries);
    }
}
