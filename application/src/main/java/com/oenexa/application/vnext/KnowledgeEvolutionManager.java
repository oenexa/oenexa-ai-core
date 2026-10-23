package com.oenexa.application.vnext;

import com.oenexa.domain.model.cognitive.OenexaVNextDomain.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Phase 3: KnowledgeEvolutionManager
 * Updates the knowledge structure autonomously as new patterns, user feedback,
 * code reviews, and architectural decisions occur.
 */
@Component
public class KnowledgeEvolutionManager {

    private final List<String> evolutionAuditLog = new CopyOnWriteArrayList<>();

    public void recordEvolution(String triggerSource, String knowledgeUpdateSummary) {
        Objects.requireNonNull(triggerSource, "triggerSource must not be null");
        Objects.requireNonNull(knowledgeUpdateSummary, "knowledgeUpdateSummary must not be null");
        String entry = String.format("[%s] Source: %s | Update: %s", Instant.now(), triggerSource, knowledgeUpdateSummary);
        evolutionAuditLog.add(entry);
    }

    public List<String> getEvolutionHistory() {
        return List.copyOf(evolutionAuditLog);
    }

    public int getEvolutionCount() {
        return evolutionAuditLog.size();
    }
}

