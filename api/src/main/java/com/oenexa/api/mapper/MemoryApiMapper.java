package com.oenexa.api.mapper;

import com.oenexa.api.dto.memory.MemoryDtos.MemoryHierarchyStatsDto;
import com.oenexa.api.dto.memory.MemoryDtos.MemoryLookupResponseDto;
import com.oenexa.domain.model.memory.EnterpriseMemoryHierarchyDomain.MemoryHierarchyStats;
import com.oenexa.domain.model.memory.EnterpriseMemoryHierarchyDomain.MemoryRetrievalResult;
import org.springframework.stereotype.Component;

/**
 * Enterprise API Mapper for Memory Hierarchy DTOs.
 */
@Component
public class MemoryApiMapper {

    public MemoryLookupResponseDto toLookupDto(MemoryRetrievalResult result) {
        if (result == null) {
            return null;
        }
        return new MemoryLookupResponseDto(
                result.key(),
                result.content(),
                result.sourceTier() != null ? result.sourceTier().name() : "UNKNOWN",
                result.hit(),
                result.latencyNanos()
        );
    }

    public MemoryHierarchyStatsDto toStatsDto(MemoryHierarchyStats stats) {
        if (stats == null) {
            return null;
        }
        return new MemoryHierarchyStatsDto(
                stats.ramEntries(),
                stats.redisEntries(),
                stats.nvmeMmapEntries(),
                stats.postgresEntries(),
                stats.cacheHitRatio(),
                stats.totalCapacityBytes()
        );
    }
}

