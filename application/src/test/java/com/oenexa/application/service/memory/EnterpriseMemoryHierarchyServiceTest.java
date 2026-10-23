package com.oenexa.application.service.memory;

import com.oenexa.application.port.*;
import com.oenexa.application.service.agent.*;
import com.oenexa.application.service.auth.*;
import com.oenexa.application.service.chat.*;
import com.oenexa.application.service.code.*;
import com.oenexa.application.service.knowledge.*;
import com.oenexa.application.service.memory.*;
import com.oenexa.application.service.orchestration.*;

import com.oenexa.domain.model.memory.EnterpriseMemoryHierarchyDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EnterpriseMemoryHierarchyService Zero-Mock Unit Tests")
class EnterpriseMemoryHierarchyServiceTest {

    private EnterpriseMemoryHierarchyService service;

    @BeforeEach
    void setUp() throws Exception {
        File temp = File.createTempFile("test-nvme-", ".mmap");
        temp.deleteOnExit();
        service = new EnterpriseMemoryHierarchyService(temp);
    }

    @Test
    @DisplayName("Should store and retrieve in L2 RAM cache")
    void shouldStoreAndRetrieveInL2Ram() {
        var record = service.store(
                "ws-1",
                "usr-1",
                "session-context",
                "Working memory in JVM Heap",
                List.of(),
                MemoryLayerTier.L2_JVM_HEAP_RAM
        );

        assertNotNull(record);
        assertEquals("session-context", record.memoryKey());

        var result = service.retrieve("ws-1", "session-context");
        assertTrue(result.hit());
        assertEquals("Working memory in JVM Heap", result.content());
        assertEquals(MemoryLayerTier.L2_JVM_HEAP_RAM, result.sourceTier());
    }

    @Test
    @DisplayName("Should store in L4 NVMe SSD memory-mapped buffer and promote to L2 RAM upon retrieval")
    void shouldStoreInL4NvmeAndPromoteToRam() {
        String largeAstContent = "class LargeService { /* 100MB repository AST nodes */ }";
        var record = service.store(
                "ws-1",
                "usr-1",
                "ast/LargeService.java",
                largeAstContent,
                List.of(0.1f, 0.2f),
                MemoryLayerTier.L4_NVME_SSD_MMAP
        );

        assertNotNull(record);

        // Retrieve triggers L4 NVMe read and automatic promotion to L2 RAM
        var result = service.retrieve("ws-1", "ast/LargeService.java");
        assertTrue(result.hit());
        assertEquals(largeAstContent, result.content());
        assertEquals(MemoryLayerTier.L4_NVME_SSD_MMAP, result.sourceTier());

        // Subsequent lookup hits the promoted L2 RAM cache directly
        var secondLookup = service.retrieve("ws-1", "ast/LargeService.java");
        assertTrue(secondLookup.hit());
        assertEquals(MemoryLayerTier.L2_JVM_HEAP_RAM, secondLookup.sourceTier());
    }

    @Test
    @DisplayName("Should handle L3 Redis and L5 Postgres tiered retrieval")
    void shouldHandleRedisAndPostgresTieredRetrieval() {
        service.store("ws-1", "usr-1", "token-cache", "redis-token-data", null, MemoryLayerTier.L3_REDIS_DISTRIBUTED);
        service.store("ws-1", "usr-1", "org-settings", "postgres-durable-config", null, MemoryLayerTier.L5_POSTGRESQL_RELATIONAL);

        var redisResult = service.retrieve("ws-1", "token-cache");
        assertTrue(redisResult.hit());
        assertEquals(MemoryLayerTier.L3_REDIS_DISTRIBUTED, redisResult.sourceTier());

        var pgResult = service.retrieve("ws-1", "org-settings");
        assertTrue(pgResult.hit());
        assertEquals(MemoryLayerTier.L5_POSTGRESQL_RELATIONAL, pgResult.sourceTier());
    }

    @Test
    @DisplayName("Should prefetch context keys and compute accurate hierarchy statistics")
    void shouldPrefetchAndComputeStats() {
        service.store("ws-1", "usr-1", "doc1", "Document 1", null, MemoryLayerTier.L4_NVME_SSD_MMAP);
        service.store("ws-1", "usr-1", "doc2", "Document 2", null, MemoryLayerTier.L4_NVME_SSD_MMAP);

        int prefetched = service.prefetch("ws-1", List.of("doc1", "doc2", "doc-missing"), MemoryLayerTier.L2_JVM_HEAP_RAM);
        assertEquals(2, prefetched);

        var stats = service.getHierarchyStats();
        assertNotNull(stats);
        assertTrue(stats.totalEntries() >= 2);
        assertTrue(stats.nvmeMmapSizeBytes() > 0);
        assertTrue(stats.cacheHitRatio() >= 50.0);
    }
}


