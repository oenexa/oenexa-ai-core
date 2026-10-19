package com.oenexa.api;

import com.oenexa.application.EnterpriseMemoryHierarchyService;
import com.oenexa.domain.EnterpriseMemoryHierarchyDomain.*;
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
        controller = new EnterpriseMemoryHierarchyController(new EnterpriseMemoryHierarchyService(temp));
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

        // Invalid requests
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

        // Invalid retrieval
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

        // Invalid prefetch
        assertEquals(HttpStatus.BAD_REQUEST, controller.prefetchMemory(null).getStatusCode());
    }

    @Test
    @DisplayName("Should get hierarchy statistics via REST endpoint")
    void shouldGetStats() {
        ResponseEntity<MemoryHierarchyStats> response = controller.getHierarchyStats();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
