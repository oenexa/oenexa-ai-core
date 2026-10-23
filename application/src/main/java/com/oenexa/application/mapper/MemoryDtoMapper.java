package com.oenexa.application.mapper;

import com.oenexa.domain.model.memory.EnterpriseMemoryHierarchyDomain.HierarchicalMemoryRecord;
import com.oenexa.domain.model.memory.EnterpriseMemoryHierarchyDomain.MemoryLayerTier;
import com.oenexa.domain.model.memory.EnterpriseMemoryHierarchyDomain.MemoryRetrievalResult;
import org.springframework.stereotype.Component;

/**
 * Enterprise Mapper Layer for Memory Hierarchy Domain.
 * Maps between hierarchical memory records and retrieval results across memory tiers.
 */
@Component
public class MemoryDtoMapper {

    public MemoryRetrievalResult toRetrievalResult(
            HierarchicalMemoryRecord record,
            MemoryLayerTier requestedTier,
            long latencyNanos
    ) {
        if (record == null) {
            return new MemoryRetrievalResult(
                    "not_found",
                    requestedTier != null ? requestedTier : MemoryLayerTier.L2_JVM_HEAP_RAM,
                    "",
                    latencyNanos,
                    requestedTier != null ? requestedTier : MemoryLayerTier.L2_JVM_HEAP_RAM,
                    false,
                    false
            );
        }
        return new MemoryRetrievalResult(
                record.memoryKey(),
                requestedTier != null ? requestedTier : record.layer(),
                record.content(),
                latencyNanos,
                record.layer(),
                true,
                record.isCompressed()
        );
    }
}

