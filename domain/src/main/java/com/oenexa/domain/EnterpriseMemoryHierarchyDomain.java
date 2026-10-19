package com.oenexa.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Domain contracts for OENEXA AI V4 7-Layer Memory Subsystem.
 * Models L1 (CPU), L2 (JVM RAM), L3 (Redis Distributed),
 * L4 (NVMe SSD Memory-Mapped Context Cache), L5 (PostgreSQL),
 * L6 (Vector Embeddings), and L7 (Object Storage).
 */
public class EnterpriseMemoryHierarchyDomain {

    public enum MemoryLayerTier {
        L1_CPU_CACHE(0, "Sub-nanosecond L1/L2/L3 registers"),
        L2_JVM_HEAP_RAM(50, "JVM Heap working memory"),
        L3_REDIS_DISTRIBUTED(1_000, "Redis distributed session and token cache"),
        L4_NVME_SSD_MMAP(20_000, "NVMe SSD memory-mapped buffer for large AST contexts"),
        L5_POSTGRESQL_RELATIONAL(5_000_000, "PostgreSQL 17 durable relational tables"),
        L6_VECTOR_EMBEDDING(15_000_000, "pgvector / OpenSearch 1536-dim HNSW vector store"),
        L7_OBJECT_STORAGE_COLD(50_000_000, "MinIO / S3 cold blob archives");

        private final long typicalLatencyNanos;
        private final String description;

        MemoryLayerTier(long typicalLatencyNanos, String description) {
            this.typicalLatencyNanos = typicalLatencyNanos;
            this.description = description;
        }

        public long getTypicalLatencyNanos() {
            return typicalLatencyNanos;
        }

        public String getDescription() {
            return description;
        }
    }

    public record HierarchicalMemoryRecord(
            String id,
            String workspaceId,
            String userId,
            MemoryLayerTier layer,
            String memoryKey,
            String content,
            List<Float> embedding,
            long accessCount,
            Instant lastAccessedAt,
            long sizeBytes,
            boolean isCompressed
    ) {
        public HierarchicalMemoryRecord {
            Objects.requireNonNull(id, "id must not be null");
            Objects.requireNonNull(workspaceId, "workspaceId must not be null");
            Objects.requireNonNull(layer, "layer must not be null");
            Objects.requireNonNull(memoryKey, "memoryKey must not be null");
            Objects.requireNonNull(content, "content must not be null");
            embedding = (embedding != null) ? List.copyOf(embedding) : List.of();
            lastAccessedAt = (lastAccessedAt != null) ? lastAccessedAt : Instant.now();
        }
    }

    public record MemoryRetrievalResult(
            String key,
            MemoryLayerTier requestedTier,
            String content,
            long latencyNanos,
            MemoryLayerTier sourceTier,
            boolean hit,
            boolean isCompressed
    ) {
        public MemoryRetrievalResult {
            Objects.requireNonNull(key, "key must not be null");
            Objects.requireNonNull(requestedTier, "requestedTier must not be null");
            sourceTier = (sourceTier != null) ? sourceTier : requestedTier;
        }
    }

    public record MemoryPrefetchRequest(
            String workspaceId,
            List<String> memoryKeys,
            MemoryLayerTier targetTier
    ) {
        public MemoryPrefetchRequest {
            Objects.requireNonNull(workspaceId, "workspaceId must not be null");
            memoryKeys = (memoryKeys != null) ? List.copyOf(memoryKeys) : List.of();
            targetTier = (targetTier != null) ? targetTier : MemoryLayerTier.L2_JVM_HEAP_RAM;
        }
    }

    public record MemoryHierarchyStats(
            long totalEntries,
            long ramEntries,
            long redisEntries,
            long nvmeMmapEntries,
            long postgresEntries,
            long vectorEntries,
            long totalCapacityBytes,
            long nvmeMmapSizeBytes,
            double cacheHitRatio
    ) {}
}
