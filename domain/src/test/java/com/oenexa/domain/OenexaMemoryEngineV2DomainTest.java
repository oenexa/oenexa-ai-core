package com.oenexa.domain;

import com.oenexa.domain.OenexaMemoryEngineV2Domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OenexaMemoryEngineV2Domain Zero-Mock Unit Tests")
class OenexaMemoryEngineV2DomainTest {

    @Test
    @DisplayName("Should verify all 6 BrainMemoryTier values have cognitive mappings and latency progression")
    void shouldVerifyAllSixBrainTiers() {
        BrainMemoryTier[] tiers = BrainMemoryTier.values();
        assertEquals(6, tiers.length);

        assertEquals("Current Thought", BrainMemoryTier.L1_HOT_EXECUTION.getCognitiveRole());
        assertEquals("Active Thought", BrainMemoryTier.L2_ACTIVE_THOUGHT.getCognitiveRole());
        assertEquals("Working Memory", BrainMemoryTier.L3_RAM_WORKING_MEMORY.getCognitiveRole());
        assertEquals("Short-Term Memory", BrainMemoryTier.L4_COMPRESSED_SHORT_TERM.getCognitiveRole());
        assertEquals("Long-Term Memory", BrainMemoryTier.L5_NVME_SSD_LONG_TERM.getCognitiveRole());
        assertEquals("Permanent Knowledge", BrainMemoryTier.L6_KNOWLEDGE_PERMANENT.getCognitiveRole());

        // Latency progression verification
        assertTrue(BrainMemoryTier.L1_HOT_EXECUTION.getTypicalLatencyNanos() < BrainMemoryTier.L2_ACTIVE_THOUGHT.getTypicalLatencyNanos());
        assertTrue(BrainMemoryTier.L2_ACTIVE_THOUGHT.getTypicalLatencyNanos() < BrainMemoryTier.L3_RAM_WORKING_MEMORY.getTypicalLatencyNanos());
        assertTrue(BrainMemoryTier.L3_RAM_WORKING_MEMORY.getTypicalLatencyNanos() < BrainMemoryTier.L4_COMPRESSED_SHORT_TERM.getTypicalLatencyNanos());
        assertTrue(BrainMemoryTier.L4_COMPRESSED_SHORT_TERM.getTypicalLatencyNanos() < BrainMemoryTier.L5_NVME_SSD_LONG_TERM.getTypicalLatencyNanos());
        assertTrue(BrainMemoryTier.L5_NVME_SSD_LONG_TERM.getTypicalLatencyNanos() < BrainMemoryTier.L6_KNOWLEDGE_PERMANENT.getTypicalLatencyNanos());
    }

    @Test
    @DisplayName("Should enforce invariant constraints on DataMovementStepRecord and DataMovementTrace")
    void shouldValidateDataMovementTrace() {
        var step1 = new DataMovementStepRecord(
                1,
                "Check Hot Cache",
                BrainMemoryTier.L1_HOT_EXECUTION,
                DataMovementStepStatus.NOT_FOUND,
                4L,
                "Miss in L1 registers"
        );
        var step2 = new DataMovementStepRecord(
                2,
                "Check Working Memory",
                BrainMemoryTier.L3_RAM_WORKING_MEMORY,
                DataMovementStepStatus.FOUND,
                85L,
                "Hit in RAM Heap"
        );

        var trace = new DataMovementTrace(
                "UserService.java",
                "ws-enterprise",
                BrainMemoryTier.L3_RAM_WORKING_MEMORY,
                true,
                "public class UserService {}",
                List.of(step1, step2),
                89L,
                4096L
        );

        assertEquals("UserService.java", trace.queryKey());
        assertEquals("ws-enterprise", trace.workspaceId());
        assertTrue(trace.found());
        assertEquals(2, trace.steps().size());
        assertEquals(BrainMemoryTier.L3_RAM_WORKING_MEMORY, trace.resolvedTier());
        assertEquals("public class UserService {}", trace.finalContent());
    }

    @Test
    @DisplayName("Should validate SmartEvictionScore and composite calculation")
    void shouldValidateSmartEvictionScore() {
        var score = new SmartEvictionScore("auth/JwtService.java", 15, 2, 0.9, 0.8, 88.5);
        assertEquals("auth/JwtService.java", score.key());
        assertEquals(15, score.frequency());
        assertEquals(2, score.recencyMinutes());
        assertEquals(0.9, score.importance());
        assertEquals(0.8, score.agentUsage());
        assertEquals(88.5, score.compositeScore());
    }

    @Test
    @DisplayName("Should validate IntelligentPrefetchPrediction and ContextCompressionMetrics")
    void shouldValidatePrefetchAndCompression() {
        var prefetch = new IntelligentPrefetchPrediction(
                "UserService.java",
                List.of("UserRepository.java", "UserController.java", "JwtService.java"),
                0.95,
                "Direct dependency in Spring controller-service-repo triad"
        );

        assertEquals(3, prefetch.predictedKeys().size());
        assertEquals(0.95, prefetch.confidenceScore());
        assertTrue(prefetch.rationale().contains("triad"));

        var compression = new ContextCompressionMetrics(
                104857600L, // 100 MB
                25165824L,  // 24 MB
                1420L,
                8500L,
                76.0,
                "ZSTD+Dict+INT8"
        );

        assertEquals(104857600L, compression.originalSizeBytes());
        assertEquals(25165824L, compression.compressedSizeBytes());
        assertEquals(76.0, compression.compressionRatioPercent());
    }

    @Test
    @DisplayName("Should validate RepositoryModuleNode and MemoryEngineV2Stats")
    void shouldValidateModuleNodeAndStats() {
        var module = new RepositoryModuleNode(
                "mod-core",
                "Core Engine",
                450,
                52428800L,
                List.of("mod-common", "mod-auth"),
                true
        );

        assertEquals("mod-core", module.moduleId());
        assertEquals(2, module.dependencies().size());
        assertTrue(module.isLoadedInRam());

        var stats = new MemoryEngineV2Stats(
                10,
                45,
                250,
                800,
                3500,
                15000,
                805306368000L, // ~750 GB effective
                68719476736L,  // 64 GB RAM
                75.5,
                420,
                15
        );

        assertEquals(10, stats.l1Count());
        assertEquals(15000, stats.knowledgeCount());
        assertEquals(75.5, stats.compressionRatio());
    }
}
