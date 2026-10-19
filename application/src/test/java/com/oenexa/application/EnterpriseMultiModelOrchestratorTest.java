package com.oenexa.application;

import com.oenexa.domain.ChatDomain.ModelProvider;
import com.oenexa.domain.ChatDomain.TaskType;
import com.oenexa.domain.MultiModelDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EnterpriseMultiModelOrchestrator Zero-Mock Unit Tests")
class EnterpriseMultiModelOrchestratorTest {

    private EnterpriseMultiModelOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new EnterpriseMultiModelOrchestrator();
    }

    @Test
    @DisplayName("Should list all registered frontier and open-source models")
    void shouldListAllRegisteredModels() {
        var models = orchestrator.listAvailableModels();
        assertFalse(models.isEmpty());
        assertTrue(models.stream().anyMatch(m -> m.modelId().equals("oenexa-gpt-o3-max")));
        assertTrue(models.stream().anyMatch(m -> m.modelId().equals("deepseek-v3")));
        assertTrue(models.stream().anyMatch(m -> m.modelId().equals("claude-3-5-sonnet")));
        assertTrue(models.stream().anyMatch(m -> m.modelId().equals("qwen-2.5-coder-32b")));
        assertTrue(models.stream().anyMatch(m -> m.modelId().equals("deepseek-r1:8b") && m.isLocal()));
    }

    @Test
    @DisplayName("Should determine accurate task types from developer queries")
    void shouldDetermineTaskTypes() {
        assertEquals(TaskType.SECURITY_AUDIT, orchestrator.determineTaskType("Perform SAST scan on repository"));
        assertEquals(TaskType.ARCHITECTURE, orchestrator.determineTaskType("Draw C4 blueprint architecture"));
        assertEquals(TaskType.BUG_FIX, orchestrator.determineTaskType("Fix NullPointerException on line 42"));
        assertEquals(TaskType.CODING, orchestrator.determineTaskType("Implement a REST controller"));
        assertEquals(TaskType.FAST_CHAT, orchestrator.determineTaskType("Hello world!"));
    }

    @Test
    @DisplayName("Should route with Cost Optimization strategy")
    void shouldRouteWithCostOptimization() {
        var decision = orchestrator.routeWithStrategy(TaskType.CODING, OptimizationStrategy.COST_OPTIMIZED, null);
        assertNotNull(decision);
        assertEquals(OptimizationStrategy.COST_OPTIMIZED, decision.strategyUsed());
        assertTrue(decision.estimatedCostPerMillionTokensUsd() <= 0.15);
    }

    @Test
    @DisplayName("Should route with Speed Optimization strategy")
    void shouldRouteWithSpeedOptimization() {
        var decision = orchestrator.routeWithStrategy(TaskType.FAST_CHAT, OptimizationStrategy.SPEED_OPTIMIZED, null);
        assertNotNull(decision);
        assertEquals(OptimizationStrategy.SPEED_OPTIMIZED, decision.strategyUsed());
        assertTrue(decision.estimatedLatencyMs() <= 200);
    }

    @Test
    @DisplayName("Should route with Quality Reasoning strategy")
    void shouldRouteWithQualityReasoning() {
        var decision = orchestrator.routeWithStrategy(TaskType.ARCHITECTURE, OptimizationStrategy.QUALITY_REASONING, null);
        assertNotNull(decision);
        assertEquals(OptimizationStrategy.QUALITY_REASONING, decision.strategyUsed());
        assertEquals(ModelProvider.ANTHROPIC_CLAUDE, decision.provider());
    }

    @Test
    @DisplayName("Should honor explicit model preference")
    void shouldHonorExplicitModelPreference() {
        var decision = orchestrator.routeWithStrategy(TaskType.FAST_CHAT, OptimizationStrategy.AUTO, "qwen");
        assertEquals("qwen-2.5-coder-32b", decision.selectedModelId());
        assertEquals(ModelProvider.QWEN, decision.provider());
    }

    @Test
    @DisplayName("Should execute successfully with Primary model when healthy")
    void shouldExecutePrimarySuccessfully() {
        var decision = orchestrator.routeWithStrategy(TaskType.CODING, OptimizationStrategy.AUTO, null);
        var result = orchestrator.executeWithSmartFallback("conv-1", "Build user service", decision, false, false);

        assertEquals(FallbackStatus.PRIMARY_SUCCESS, result.fallbackStatus());
        assertEquals(0, result.failoverAttempts());
        assertFalse(result.responseContent().isBlank());
    }

    @Test
    @DisplayName("Should failover to Secondary model when primary fails")
    void shouldFailoverToSecondaryWhenPrimaryFails() {
        var decision = orchestrator.routeWithStrategy(TaskType.CODING, OptimizationStrategy.AUTO, null);
        var result = orchestrator.executeWithSmartFallback("conv-1", "Build user service", decision, true, false);

        assertEquals(FallbackStatus.SECONDARY_FAILOVER, result.fallbackStatus());
        assertEquals(1, result.failoverAttempts());
        assertTrue(result.executionTrace().stream().anyMatch(t -> t.contains("Secondary Fallback model succeeded")));
    }

    @Test
    @DisplayName("Should failover to Air-Gapped Local Ollama model when both cloud models fail")
    void shouldFailoverToLocalAirGappedWhenBothFail() {
        var decision = orchestrator.routeWithStrategy(TaskType.CODING, OptimizationStrategy.AUTO, null);
        var result = orchestrator.executeWithSmartFallback("conv-1", "Build user service", decision, true, true);

        assertEquals(FallbackStatus.LOCAL_AIR_GAPPED_FAILOVER, result.fallbackStatus());
        assertEquals(2, result.failoverAttempts());
        assertEquals(ModelProvider.LOCAL_OLLAMA, result.executedProvider());
        assertTrue(result.executionTrace().stream().anyMatch(t -> t.contains("Local air-gapped model responded")));
    }

    @Test
    @DisplayName("Should execute multi-model collaboration across Architect, Coder, and Security models")
    void shouldExecuteMultiModelCollaboration() {
        var turn = orchestrator.collaborateMultiModel("Create high-throughput event processing engine", "record Event()");
        assertNotNull(turn);
        assertEquals(3, turn.contributingModels().size());
        assertTrue(turn.architectBlueprint().contains("Architectural Blueprint"));
        assertTrue(turn.coderImplementation().contains("Concrete Implementation"));
        assertTrue(turn.securityAudit().contains("Security & SAST Audit"));
        assertTrue(turn.synthesizedFinalResponse().contains("OENEXA Multi-Model Collaborative Synthesis"));
    }
}
