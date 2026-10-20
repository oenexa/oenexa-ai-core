package com.oenexa.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Domain specifications for OENEXA Memory Engine V2: SSD-First AI Memory Architecture.
 * Models the 6-layer human brain analogy hierarchy, deterministic data movement steps,
 * smart eviction prioritization, intelligent prefetching, context compression,
 * and modular repository memory graphs.
 */
public class OenexaMemoryEngineV2Domain {

    public enum BrainMemoryTier {
        L1_HOT_EXECUTION(1, "Current Thought", "Nanoseconds", "Current token, active matrix calculation, running operator"),
        L2_ACTIVE_THOUGHT(50, "Active Thought", "Sub-microsecond", "Current context window, hot embeddings, active agent plans"),
        L3_RAM_WORKING_MEMORY(100, "Working Memory", "Fast (<100ns)", "Active repository files, agent contexts, active vector chunks"),
        L4_COMPRESSED_SHORT_TERM(5_000, "Short-Term Memory", "Extended RAM (<5μs)", "Semi-active context, ZSTD/dictionary compressed"),
        L5_NVME_SSD_LONG_TERM(25_000, "Long-Term Memory", "Virtual AI (<25μs)", "Model weights, historical chats, zero-copy mmap snapshots"),
        L6_KNOWLEDGE_PERMANENT(5_000_000, "Permanent Knowledge", "Durable (1-15ms)", "PostgreSQL, Qdrant vectors, MinIO object storage");

        private final long typicalLatencyNanos;
        private final String cognitiveRole;
        private final String latencyDisplay;
        private final String description;

        BrainMemoryTier(long typicalLatencyNanos, String cognitiveRole, String latencyDisplay, String description) {
            this.typicalLatencyNanos = typicalLatencyNanos;
            this.cognitiveRole = cognitiveRole;
            this.latencyDisplay = latencyDisplay;
            this.description = description;
        }

        public long getTypicalLatencyNanos() {
            return typicalLatencyNanos;
        }

        public String getCognitiveRole() {
            return cognitiveRole;
        }

        public String getLatencyDisplay() {
            return latencyDisplay;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum DataMovementStepStatus {
        FOUND,
        NOT_FOUND,
        PROMOTED,
        SKIPPED
    }

    public record DataMovementStepRecord(
            int stepNumber,
            String stepName,
            BrainMemoryTier tierSearched,
            DataMovementStepStatus status,
            long latencyNanos,
            String details
    ) {
        public DataMovementStepRecord {
            Objects.requireNonNull(stepName, "stepName must not be null");
            Objects.requireNonNull(tierSearched, "tierSearched must not be null");
            Objects.requireNonNull(status, "status must not be null");
            details = (details != null) ? details : "";
        }
    }

    public record DataMovementTrace(
            String queryKey,
            String workspaceId,
            BrainMemoryTier resolvedTier,
            boolean found,
            String finalContent,
            List<DataMovementStepRecord> steps,
            long totalDurationNanos,
            long memorySavedBytes
    ) {
        public DataMovementTrace {
            Objects.requireNonNull(queryKey, "queryKey must not be null");
            Objects.requireNonNull(workspaceId, "workspaceId must not be null");
            Objects.requireNonNull(resolvedTier, "resolvedTier must not be null");
            finalContent = (finalContent != null) ? finalContent : "";
            steps = (steps != null) ? List.copyOf(steps) : List.of();
        }
    }

    public record SmartEvictionScore(
            String key,
            long frequency,
            long recencyMinutes,
            double importance,
            double agentUsage,
            double compositeScore
    ) {
        public SmartEvictionScore {
            Objects.requireNonNull(key, "key must not be null");
        }
    }

    public record IntelligentPrefetchPrediction(
            String triggerKey,
            List<String> predictedKeys,
            double confidenceScore,
            String rationale
    ) {
        public IntelligentPrefetchPrediction {
            Objects.requireNonNull(triggerKey, "triggerKey must not be null");
            predictedKeys = (predictedKeys != null) ? List.copyOf(predictedKeys) : List.of();
            rationale = (rationale != null) ? rationale : "";
        }
    }

    public record ContextCompressionMetrics(
            long originalSizeBytes,
            long compressedSizeBytes,
            long deduplicatedItemsCount,
            long tokensPruned,
            double compressionRatioPercent,
            String compressionAlgorithm
    ) {
        public ContextCompressionMetrics {
            compressionAlgorithm = (compressionAlgorithm != null) ? compressionAlgorithm : "ZSTD+Dict+Quant";
        }
    }

    public record RepositoryModuleNode(
            String moduleId,
            String moduleName,
            int fileCount,
            long totalSizeBytes,
            List<String> dependencies,
            boolean isLoadedInRam
    ) {
        public RepositoryModuleNode {
            Objects.requireNonNull(moduleId, "moduleId must not be null");
            Objects.requireNonNull(moduleName, "moduleName must not be null");
            dependencies = (dependencies != null) ? List.copyOf(dependencies) : List.of();
        }
    }

    public record MemoryEngineV2Stats(
            long l1Count,
            long l2Count,
            long ramCount,
            long compressedCount,
            long ssdCount,
            long knowledgeCount,
            long totalEffectiveMemoryBytes,
            long physicalRamUsedBytes,
            double compressionRatio,
            long prefetchHits,
            long evictionCount
    ) {}
}
