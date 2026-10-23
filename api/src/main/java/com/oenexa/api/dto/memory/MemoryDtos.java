package com.oenexa.api.dto.memory;

/**
 * Standardized DTO Layer for Memory Hierarchy Subsystem.
 */
public class MemoryDtos {

    public record MemoryStoreRequestDto(
            String key,
            String content,
            String tier,
            long ttlSeconds
    ) {
        public MemoryStoreRequestDto {
            key = (key != null) ? key : "";
            content = (content != null) ? content : "";
            tier = (tier != null && !tier.isBlank()) ? tier : "L2_JVM_HEAP_RAM";
        }
    }

    public record MemoryLookupResponseDto(
            String key,
            String content,
            String tierFound,
            boolean hit,
            long latencyNanos
    ) {}

    public record MemoryHierarchyStatsDto(
            long ramEntries,
            long redisEntries,
            long nvmeMmapEntries,
            long postgresEntries,
            double cacheHitRatio,
            long totalCapacityBytes
    ) {}
}
