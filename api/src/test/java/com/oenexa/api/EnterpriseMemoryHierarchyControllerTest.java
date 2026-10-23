package com.oenexa.api;

import com.oenexa.api.controller.*;
import com.oenexa.api.exception.*;
import com.oenexa.application.EnterpriseMemoryHierarchyService;
import com.oenexa.application.OenexaContextCompressionEngine;
import com.oenexa.application.OenexaIntelligentPrefetchEngine;
import com.oenexa.application.OenexaMemoryEngineV2Service;
import com.oenexa.application.OenexaRepositoryMemoryGraphService;
import com.oenexa.domain.EnterpriseMemoryHierarchyDomain.*;
import com.oenexa.domain.OenexaMemoryEngineV2Domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EnterpriseMemoryHierarchyController Zero-Mock Unit Tests")
class EnterpriseMemoryHierarchyControllerTest {

    private EnterpriseMemoryHierarchyController controller;

    @BeforeEach
    void setUp() throws Exception {
        File temp = File.createTempFile("test-ctrl-nvme-", ".mmap");
        temp.deleteOnExit();
        File tempV2 = File.createTempFile("test-ctrl-v2-", ".mmap");
        tempV2.deleteOnExit();

        var compEngine = new OenexaContextCompressionEngine();
        var prefetchEngine = new OenexaIntelligentPrefetchEngine();
        var graphService = new OenexaRepositoryMemoryGraphService();
        var v2Service = new OenexaMemoryEngineV2Service(compEngine, prefetchEngine, graphService, tempV2);

        controller = new EnterpriseMemoryHierarchyController(
                new EnterpriseMemoryHierarchyService(temp),
                v2Service,
                compEngine,
                prefetchEngine,
                graphService
        );
    }

    @Test
    @DisplayName("Should store memory via REST endpoint")
    void shouldStoreMemory() {
        var req = new EnterpriseMemoryHierarchyController.StoreMemoryRequest(
                "ws-1",
                "usr-1",
                "key-1",
                "Context content",
                List.of(0.1f),
                MemoryLayerTier.L4_NVME_SSD_MMAP
        );

        ResponseEntity<HierarchicalMemoryRecord> response = controller.storeMemory(req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("key-1", response.getBody().memoryKey());
        assertEquals(MemoryLayerTier.L4_NVME_SSD_MMAP, response.getBody().layer());

        assertEquals(HttpStatus.BAD_REQUEST, controller.storeMemory(null).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.storeMemory(new EnterpriseMemoryHierarchyController.StoreMemoryRequest(null, "u", "k", "c", null, null)).getStatusCode());
    }

    @Test
    @DisplayName("Should retrieve memory via REST endpoint")
    void shouldRetrieveMemory() {
        controller.storeMemory(new EnterpriseMemoryHierarchyController.StoreMemoryRequest("ws-1", "usr-1", "k-doc", "Document Content", null, MemoryLayerTier.L2_JVM_HEAP_RAM));

        ResponseEntity<MemoryRetrievalResult> response = controller.retrieveMemory("ws-1", "k-doc");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().hit());
        assertEquals("Document Content", response.getBody().content());

        assertEquals(HttpStatus.BAD_REQUEST, controller.retrieveMemory("ws-1", "").getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.retrieveMemory("ws-1", null).getStatusCode());
    }

    @Test
    @DisplayName("Should prefetch keys via REST endpoint")
    void shouldPrefetch() {
        controller.storeMemory(new EnterpriseMemoryHierarchyController.StoreMemoryRequest("ws-1", "usr-1", "k1", "C1", null, MemoryLayerTier.L4_NVME_SSD_MMAP));
        controller.storeMemory(new EnterpriseMemoryHierarchyController.StoreMemoryRequest("ws-1", "usr-1", "k2", "C2", null, MemoryLayerTier.L4_NVME_SSD_MMAP));

        var req = new EnterpriseMemoryHierarchyController.PrefetchRequest("ws-1", List.of("k1", "k2"), MemoryLayerTier.L2_JVM_HEAP_RAM);
        ResponseEntity<Integer> response = controller.prefetchMemory(req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody());

        assertEquals(HttpStatus.BAD_REQUEST, controller.prefetchMemory(null).getStatusCode());
    }

    @Test
    @DisplayName("Should get hierarchy statistics via REST endpoint")
    void shouldGetStats() {
        ResponseEntity<MemoryHierarchyStats> response = controller.getHierarchyStats();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    // =========================================================================
    // V2 REST ENDPOINT ZERO-MOCK TESTS
    // =========================================================================

    @Test
    @DisplayName("Should query V2 6-step data movement pipeline via REST endpoint")
    void shouldQueryV2Pipeline() {
        var req = new EnterpriseMemoryHierarchyController.V2QueryRequest("default-ws", "UserService.java");
        ResponseEntity<DataMovementTrace> response = controller.queryV2(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().found());
        assertEquals("UserService.java", response.getBody().queryKey());

        // Invalid request
        assertEquals(HttpStatus.BAD_REQUEST, controller.queryV2(null).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.queryV2(new EnterpriseMemoryHierarchyController.V2QueryRequest("ws", "")).getStatusCode());
    }

    @Test
    @DisplayName("Should compress context via V2 REST endpoint")
    void shouldCompressV2() {
        var req = new EnterpriseMemoryHierarchyController.V2CompressRequest("class Test { int a = 1; }\nclass Test { int a = 1; }");
        ResponseEntity<ContextCompressionMetrics> response = controller.compressV2(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().compressionRatioPercent() >= 0.0);

        assertEquals(HttpStatus.BAD_REQUEST, controller.compressV2(null).getStatusCode());
    }

    @Test
    @DisplayName("Should predict prefetch via V2 REST endpoint")
    void shouldPredictPrefetchV2() {
        var req = new EnterpriseMemoryHierarchyController.V2PrefetchPredictionRequest("UserService.java");
        ResponseEntity<IntelligentPrefetchPrediction> response = controller.prefetchV2(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().predictedKeys().contains("UserRepository.java"));

        assertEquals(HttpStatus.BAD_REQUEST, controller.prefetchV2(null).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.prefetchV2(new EnterpriseMemoryHierarchyController.V2PrefetchPredictionRequest("")).getStatusCode());
    }

    @Test
    @DisplayName("Should run eviction and lifecycle decay cycles via V2 REST endpoints")
    void shouldRunEvictionAndLifecycleV2() {
        ResponseEntity<Integer> evictRes = controller.evictV2();
        assertEquals(HttpStatus.OK, evictRes.getStatusCode());

        ResponseEntity<Integer> lifeRes = controller.lifecycleV2();
        assertEquals(HttpStatus.OK, lifeRes.getStatusCode());
    }

    @Test
    @DisplayName("Should retrieve modular repository graph and V2 stats via REST endpoints")
    void shouldGetGraphAndStatsV2() {
        ResponseEntity<List<RepositoryModuleNode>> graphRes = controller.getGraphV2("default-ws");
        assertEquals(HttpStatus.OK, graphRes.getStatusCode());
        assertNotNull(graphRes.getBody());
        assertFalse(graphRes.getBody().isEmpty());

        ResponseEntity<MemoryEngineV2Stats> statsRes = controller.getStatsV2();
        assertEquals(HttpStatus.OK, statsRes.getStatusCode());
        assertNotNull(statsRes.getBody());
        assertTrue(statsRes.getBody().totalEffectiveMemoryBytes() > 0);
    }
}
