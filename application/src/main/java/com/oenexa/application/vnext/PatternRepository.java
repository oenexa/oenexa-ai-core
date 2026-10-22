package com.oenexa.application.vnext;

import com.oenexa.domain.OenexaVNextDomain.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Phase 3: PatternRepository
 * Thread-safe concurrent storage for all 5 VNext pattern categories:
 * REASONING, SOLUTION, FAILURE, PERFORMANCE, OPTIMIZATION.
 */
@Component
public class PatternRepository {

    private final Map<String, VNextPattern> store = new ConcurrentHashMap<>();

    public PatternRepository() {
        initDefaultPatterns();
    }

    private void initDefaultPatterns() {
        store.put("PAT-ZERO-MOCK", new VNextPattern("PAT-ZERO-MOCK", VNextPatternCategory.SOLUTION, "Zero-Mock Real Domain Testing", "Test synthesis for domain/app", "Instantiate real records with invariant assertions", 15, 0, 1.0));
        store.put("PAT-HEXAGONAL-ISOLATION", new VNextPattern("PAT-HEXAGONAL-ISOLATION", VNextPatternCategory.REASONING, "Hexagonal Inbound/Outbound Isolation", "Boundary decoupling", "Strictly isolate domain from Spring/JPA", 12, 0, 1.0));
        store.put("PAT-NPE-GUARD", new VNextPattern("PAT-NPE-GUARD", VNextPatternCategory.FAILURE, "Defensive Object Dereference Guard", "CWE-476 prevention", "Inject Objects.requireNonNull or Optional", 20, 0, 1.0));
        store.put("PAT-VIRTUAL-THREADS", new VNextPattern("PAT-VIRTUAL-THREADS", VNextPatternCategory.PERFORMANCE, "Java 25 Structured Concurrency", "High I/O swarm dispatch", "Dispatch per-task virtual threads with structured scope", 18, 0, 1.0));
        store.put("PAT-ZSTD-SSD-FIRST", new VNextPattern("PAT-ZSTD-SSD-FIRST", VNextPatternCategory.OPTIMIZATION, "ZSTD Memory Compaction", "L4/L5 memory exhaustion", "Compress cold context vectors with ZSTD", 10, 0, 1.0));
    }

    public void save(VNextPattern pattern) {
        Objects.requireNonNull(pattern, "pattern must not be null");
        store.put(pattern.patternId(), pattern);
    }

    public Optional<VNextPattern> findById(String patternId) {
        return Optional.ofNullable(store.get(patternId));
    }

    public List<VNextPattern> findByCategory(VNextPatternCategory category) {
        return store.values().stream()
                .filter(p -> p.category() == category)
                .toList();
    }

    public List<VNextPattern> findAll() {
        return List.copyOf(store.values());
    }

    public int count() {
        return store.size();
    }
}
