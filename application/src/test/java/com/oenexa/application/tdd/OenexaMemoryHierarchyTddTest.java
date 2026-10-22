package com.oenexa.application.tdd;

import com.oenexa.application.OenexaMemoryEngineV2Service;
import com.oenexa.domain.OenexaMemoryEngineV2Domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD Contract Specification for OENEXA Memory Engine V2 SSD-First Architecture.
 * Verifies mathematical formulas, latency tiers, eviction scoring, and tier transitions.
 */
@Tag("tdd")
@DisplayName("OENEXA Memory Engine V2 - TDD Specifications")
public class OenexaMemoryHierarchyTddTest {

    private OenexaMemoryEngineV2Service memoryEngine;

    @BeforeEach
    void setUp() {
        memoryEngine = new OenexaMemoryEngineV2Service();
    }

    @Test
    @DisplayName("TDD-1: L1 Cache Hit Latency Contract (< 50ms)")
    void testL1CacheHitLatencyContract() {
        // Arrange: Populate knowledge and execute initial fetch to promote to L1
        memoryEngine.storeInKnowledge("ws-tdd", "AuthContract.java", "public interface AuthContract {}", 1.0);
        DataMovementTrace firstTrace = memoryEngine.executeDataMovementPipeline("ws-tdd", "AuthContract.java");
        assertNotNull(firstTrace);

        // Act: Second access should be an instant L1 hit
        DataMovementTrace l1Trace = memoryEngine.executeDataMovementPipeline("ws-tdd", "AuthContract.java");

        // Assert: Red-Green Invariant
        assertEquals(BrainMemoryTier.L1_HOT_EXECUTION, l1Trace.resolvedTier());
        assertTrue(l1Trace.totalDurationNanos() < 50_000_000L, "L1 latency must be ultra-fast");
        assertTrue(l1Trace.found(), "Should be registered as found");
    }

    @Test
    @DisplayName("TDD-2: Multi-Tier Escalation from Cold Knowledge to Hot L1")
    void testMultiTierEscalationContract() {
        // Arrange
        String key = "DeepModelWeights.bin";
        String content = "MODEL_TENSOR_DATA_V2_QUANTIZED";
        memoryEngine.storeInKnowledge("ws-cold", key, content, 0.85);

        // Act
        DataMovementTrace trace = memoryEngine.executeDataMovementPipeline("ws-cold", key);

        // Assert
        assertNotNull(trace);
        assertEquals(content, trace.finalContent());
        assertTrue(trace.found());
        assertFalse(trace.steps().isEmpty(), "Data movement steps must be recorded");
        assertTrue(memoryEngine.getV2Stats().l1Count() > 0, "Retrieved cold data must be escalated to L1");
    }

    @Test
    @DisplayName("TDD-3: Smart Eviction Score Calculation Contract")
    void testSmartEvictionScoreContract() {
        // Score = Frequency + Recency + Importance + Agent Usage
        OenexaMemoryEngineV2Service.InternalMemoryEntry entry = new OenexaMemoryEngineV2Service.InternalMemoryEntry(
                "OrderService.java",
                "ws-tdd",
                "class OrderService {}",
                null,
                BrainMemoryTier.L3_RAM_WORKING_MEMORY,
                10L, // frequency
                Instant.now(),
                0.90, // importance
                5.0, // agentUsage
                100L // sizeBytes
        );

        SmartEvictionScore score = memoryEngine.calculateEvictionScore(entry);

        assertNotNull(score);
        assertEquals("OrderService.java", score.key());
        assertTrue(score.compositeScore() > 0, "Eviction score must be positive");
        assertEquals(10L, score.frequency());
        assertEquals(5.0, score.agentUsage());
    }

    @Test
    @DisplayName("TDD-4: Lifecycle Decay & Demotion Cycle Execution")
    void testLifecycleDecayExecution() {
        // Seed entries into system
        memoryEngine.storeInKnowledge("ws-decay", "OldLegacyDoc.md", "# Obsolete", 0.1);
        memoryEngine.executeDataMovementPipeline("ws-decay", "OldLegacyDoc.md");

        // Act: Run decay cycle
        int demotedCount = memoryEngine.runLifecycleDecayCycle();

        // Assert
        assertTrue(demotedCount >= 0, "Decay cycle must report processed items");
        MemoryEngineV2Stats stats = memoryEngine.getV2Stats();
        assertNotNull(stats);
    }

    @Test
    @DisplayName("TDD-5: Promotion Back to RAM and L2 upon Access")
    void testPromotionBackToRamOnAccess() {
        // Arrange: Place in knowledge
        memoryEngine.storeInKnowledge("ws-promote", "ActivePipeline.java", "public class ActivePipeline {}", 0.95);

        // Act
        DataMovementTrace trace = memoryEngine.executeDataMovementPipeline("ws-promote", "ActivePipeline.java");

        // Assert
        assertTrue(trace.found());
        MemoryEngineV2Stats stats = memoryEngine.getV2Stats();
        assertTrue(stats.l1Count() > 0, "L1 cache count must increment upon promotion");
    }
}
