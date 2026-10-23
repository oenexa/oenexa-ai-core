package com.oenexa.application.service.memory;

import com.oenexa.application.port.*;
import com.oenexa.application.service.agent.*;
import com.oenexa.application.service.auth.*;
import com.oenexa.application.service.chat.*;
import com.oenexa.application.service.code.*;
import com.oenexa.application.service.knowledge.*;
import com.oenexa.application.service.memory.*;
import com.oenexa.application.service.orchestration.*;

import com.oenexa.domain.model.memory.OenexaMemoryEngineV2Domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OenexaMemoryEngineV2Service Zero-Mock Unit Tests")
class OenexaMemoryEngineV2ServiceTest {

    private OenexaMemoryEngineV2Service service;

    @BeforeEach
    void setUp() throws Exception {
        File temp = File.createTempFile("test-v2-mmap-", ".mmap");
        temp.deleteOnExit();
        service = new OenexaMemoryEngineV2Service(
                new OenexaContextCompressionEngine(),
                new OenexaIntelligentPrefetchEngine(),
                new OenexaRepositoryMemoryGraphService(),
                temp
        );
    }

    @Test
    @DisplayName("Should execute 6-step data movement pipeline and cascade promotion")
    void shouldExecuteDataMovementPipeline() {
        // Initial query finds in permanent knowledge base (Step 6) and promotes up to L1
        var trace = service.executeDataMovementPipeline("default-ws", "UserService.java");
        assertNotNull(trace);
        assertTrue(trace.found());
        assertEquals("UserService.java", trace.queryKey());
        assertTrue(trace.finalContent().contains("UserService"));
        assertTrue(trace.steps().size() >= 5);

        // Subsequent query hits L1 hot cache immediately (Step 2)
        var hotTrace = service.executeDataMovementPipeline("default-ws", "UserService.java");
        assertTrue(hotTrace.found());
        assertEquals(BrainMemoryTier.L1_HOT_EXECUTION, hotTrace.resolvedTier());
    }

    @Test
    @DisplayName("Should handle missing keys gracefully through all 6 steps")
    void shouldHandleMissingKeys() {
        var trace = service.executeDataMovementPipeline("default-ws", "NonExistentClass.java");
        assertNotNull(trace);
        assertFalse(trace.found());
        assertEquals(BrainMemoryTier.L6_KNOWLEDGE_PERMANENT, trace.resolvedTier());
    }

    @Test
    @DisplayName("Should compute smart eviction scores and execute eviction cycle")
    void shouldExecuteSmartEviction() {
        // Populate items in knowledge and promote to RAM
        service.executeDataMovementPipeline("default-ws", "UserService.java");
        service.executeDataMovementPipeline("default-ws", "UserRepository.java");
        service.executeDataMovementPipeline("default-ws", "UserController.java");
        service.executeDataMovementPipeline("default-ws", "JwtService.java");

        int evicted = service.runSmartEvictionCycle();
        assertTrue(evicted >= 0);

        var stats = service.getV2Stats();
        assertNotNull(stats);
        assertTrue(stats.totalEffectiveMemoryBytes() > 0);
        assertTrue(stats.physicalRamUsedBytes() > 0);
    }

    @Test
    @DisplayName("Should execute lifecycle decay cycle")
    void shouldExecuteLifecycleDecay() {
        service.executeDataMovementPipeline("default-ws", "UserService.java");
        int decayed = service.runLifecycleDecayCycle();
        assertEquals(0, decayed); // Just accessed, not decayed yet
    }
}


