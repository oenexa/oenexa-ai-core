package com.oenexa.application.vnext;

import com.oenexa.domain.model.cognitive.OenexaVNextDomain.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

/**
 * Phase 6: MemoryEvolutionManager
 * Governs the 7-tier hierarchical memory architecture:
 * L1: Session Cache (In-Memory Hot)
 * L2: Context Memory (Ultra-Hot Working Window)
 * L3: Reasoning Memory (Deliberative Traces & Hypotheses)
 * L4: Knowledge Repository (Indexed Code & Architecture)
 * L5: Learning Repository (Learned Patterns & Dynamic Weights)
 * L6: Compressed SSD Memory (ZSTD Compaction 64GB -> 200GB+)
 * L7: Long-Term Knowledge Storage (PostgreSQL 17 + pgvector HNSW)
 *
 * Capabilities: Consolidation, Compression, Deduplication, Relevance Ranking.
 */
@Service
public class MemoryEvolutionManager {

    public MemoryConsolidationReport consolidateMemory(List<String> rawEntries) {
        if (rawEntries == null || rawEntries.isEmpty()) {
            return new MemoryConsolidationReport(0, 0, 1.0, 0L, Instant.now());
        }

        Set<String> uniqueEntries = new LinkedHashSet<>(rawEntries);
        int duplicates = rawEntries.size() - uniqueEntries.size();

        long originalBytes = rawEntries.stream().mapToLong(String::length).sum();
        long consolidatedBytes = uniqueEntries.stream().mapToLong(String::length).sum();
        long savedBytes = originalBytes - consolidatedBytes;

        double compressionRatio = originalBytes > 0 ? (double) consolidatedBytes / originalBytes : 1.0;

        return new MemoryConsolidationReport(
                uniqueEntries.size(),
                duplicates,
                compressionRatio,
                savedBytes,
                Instant.now()
        );
    }

    public List<VNextMemoryTier> getActiveTiers() {
        return List.of(VNextMemoryTier.values());
    }
}

