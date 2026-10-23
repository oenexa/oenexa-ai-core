package com.oenexa.domain.model.memory;

import com.oenexa.domain.exception.*;
import com.oenexa.domain.model.agent.*;
import com.oenexa.domain.model.auth.*;
import com.oenexa.domain.model.chat.*;
import com.oenexa.domain.model.code.*;
import com.oenexa.domain.model.cognitive.*;
import com.oenexa.domain.model.common.*;
import com.oenexa.domain.model.knowledge.*;
import com.oenexa.domain.model.memory.*;
import com.oenexa.domain.model.orchestration.*;

import com.oenexa.domain.model.memory.EnterpriseMemoryHierarchyDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EnterpriseMemoryHierarchyDomain Zero-Mock Unit Tests")
class EnterpriseMemoryHierarchyDomainTest {

    @Test
    @DisplayName("Should verify all 7 memory hierarchy tiers exist with latency characteristics")
    void shouldVerifySevenTiers() {
        assertEquals(7, MemoryLayerTier.values().length);
        assertTrue(MemoryLayerTier.L1_CPU_CACHE.getTypicalLatencyNanos() < MemoryLayerTier.L2_JVM_HEAP_RAM.getTypicalLatencyNanos());
        assertTrue(MemoryLayerTier.L2_JVM_HEAP_RAM.getTypicalLatencyNanos() < MemoryLayerTier.L3_REDIS_DISTRIBUTED.getTypicalLatencyNanos());
        assertTrue(MemoryLayerTier.L3_REDIS_DISTRIBUTED.getTypicalLatencyNanos() < MemoryLayerTier.L4_NVME_SSD_MMAP.getTypicalLatencyNanos());
        assertTrue(MemoryLayerTier.L4_NVME_SSD_MMAP.getTypicalLatencyNanos() < MemoryLayerTier.L5_POSTGRESQL_RELATIONAL.getTypicalLatencyNanos());
        assertTrue(MemoryLayerTier.L5_POSTGRESQL_RELATIONAL.getTypicalLatencyNanos() < MemoryLayerTier.L6_VECTOR_EMBEDDING.getTypicalLatencyNanos());
        assertTrue(MemoryLayerTier.L6_VECTOR_EMBEDDING.getTypicalLatencyNanos() < MemoryLayerTier.L7_OBJECT_STORAGE_COLD.getTypicalLatencyNanos());
    }

    @Test
    @DisplayName("Should enforce invariant constraints on HierarchicalMemoryRecord")
    void shouldValidateHierarchicalMemoryRecord() {
        var record = new HierarchicalMemoryRecord(
                "mem-1",
                "ws-default",
                "usr-1",
                MemoryLayerTier.L4_NVME_SSD_MMAP,
                "ast/UserService.java",
                "public class UserService { ... }",
                List.of(0.12f, 0.45f, 0.99f),
                12,
                Instant.now(),
                4096,
                false
        );

        assertEquals("mem-1", record.id());
        assertEquals("ws-default", record.workspaceId());
        assertEquals(MemoryLayerTier.L4_NVME_SSD_MMAP, record.layer());
        assertEquals(3, record.embedding().size());
        assertEquals(4096, record.sizeBytes());
    }

    @Test
    @DisplayName("Should validate MemoryRetrievalResult and MemoryPrefetchRequest")
    void shouldValidateRetrievalAndPrefetch() {
        var retrieval = new MemoryRetrievalResult(
                "ctx-key",
                MemoryLayerTier.L2_JVM_HEAP_RAM,
                "Content from NVMe promoted to RAM",
                12000L,
                MemoryLayerTier.L4_NVME_SSD_MMAP,
                true,
                false
        );

        assertTrue(retrieval.hit());
        assertEquals(MemoryLayerTier.L4_NVME_SSD_MMAP, retrieval.sourceTier());

        var prefetch = new MemoryPrefetchRequest("ws-prod", List.of("k1", "k2"), null);
        assertEquals("ws-prod", prefetch.workspaceId());
        assertEquals(2, prefetch.memoryKeys().size());
        assertEquals(MemoryLayerTier.L2_JVM_HEAP_RAM, prefetch.targetTier());
    }
}


