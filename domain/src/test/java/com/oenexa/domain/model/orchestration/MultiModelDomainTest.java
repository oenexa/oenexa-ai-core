package com.oenexa.domain.model.orchestration;

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

import com.oenexa.domain.model.chat.ChatDomain.ModelProvider;
import com.oenexa.domain.model.chat.ChatDomain.TaskType;
import com.oenexa.domain.model.orchestration.MultiModelDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MultiModelDomain Zero-Mock Unit Tests")
class MultiModelDomainTest {

    @Test
    @DisplayName("Should create and validate AiModelSpec invariant contracts")
    void shouldCreateAiModelSpec() {
        var spec = new AiModelSpec(
                "qwen-2.5-coder-32b",
                "Qwen 2.5 Coder 32B",
                ModelProvider.QWEN,
                128_000,
                0.20,
                210,
                List.of("CODING", "REASONING", "TOOLS"),
                true,
                false
        );

        assertEquals("qwen-2.5-coder-32b", spec.modelId());
        assertEquals("Qwen 2.5 Coder 32B", spec.name());
        assertEquals(ModelProvider.QWEN, spec.provider());
        assertTrue(spec.supportsCapability("CODING"));
        assertTrue(spec.supportsCapability("coding"));
        assertFalse(spec.supportsCapability("VISION"));
        assertFalse(spec.isLocal());
    }

    @Test
    @DisplayName("Should validate SmartFallbackChain contracts")
    void shouldValidateSmartFallbackChain() {
        var chain = new SmartFallbackChain("claude-3-5-sonnet", "deepseek-v3", "ollama-deepseek-r1");
        assertEquals("claude-3-5-sonnet", chain.primaryModelId());
        assertEquals(3, chain.toList().size());
        assertEquals("ollama-deepseek-r1", chain.localAirGappedModelId());

        assertThrows(NullPointerException.class, () -> new SmartFallbackChain(null, "b", "c"));
    }

    @Test
    @DisplayName("Should validate RoutingDecisionResult invariants")
    void shouldValidateRoutingDecisionResult() {
        var chain = new SmartFallbackChain("oenexa-gpt-o3-max", "deepseek-v3", "ollama-deepseek-r1");
        var decision = new RoutingDecisionResult(
                "oenexa-gpt-o3-max",
                "OENEXA GPT o3-max",
                ModelProvider.OENEXA_GPT,
                TaskType.REASONING,
                OptimizationStrategy.QUALITY_REASONING,
                2.50,
                550,
                "Premier reasoning frontier model",
                chain
        );

        assertEquals(ModelProvider.OENEXA_GPT, decision.provider());
        assertEquals(OptimizationStrategy.QUALITY_REASONING, decision.strategyUsed());
        assertEquals(2.50, decision.estimatedCostPerMillionTokensUsd());
    }

    @Test
    @DisplayName("Should validate MultiModelCollaborationTurn invariants")
    void shouldValidateMultiModelCollaborationTurn() {
        var turn = new MultiModelCollaborationTurn(
                "turn-1",
                "claude-3-5-sonnet",
                "C4 Architecture Blueprint",
                "qwen-2.5-coder-32b",
                "Zero-mock Java implementation",
                "oenexa-security-sast-v1",
                "No CVE vulnerabilities detected",
                "Unified Synthesized Solution",
                List.of("claude-3-5-sonnet", "qwen-2.5-coder-32b", "oenexa-security-sast-v1"),
                null
        );

        assertEquals("turn-1", turn.turnId());
        assertEquals(3, turn.contributingModels().size());
        assertNotNull(turn.timestamp());
    }

    @Test
    @DisplayName("Should validate ExecutionWithFallbackResult contracts")
    void shouldValidateExecutionWithFallbackResult() {
        var result = new ExecutionWithFallbackResult(
                "conv-101",
                "deepseek-r1:8b",
                ModelProvider.LOCAL_OLLAMA,
                FallbackStatus.LOCAL_AIR_GAPPED_FAILOVER,
                2,
                "Air-gapped response synthesized locally",
                "Reasoned locally via Ollama",
                List.of("Primary failed", "Secondary failed", "Local air-gapped succeeded"),
                null
        );

        assertEquals(FallbackStatus.LOCAL_AIR_GAPPED_FAILOVER, result.fallbackStatus());
        assertEquals(2, result.failoverAttempts());
        assertEquals(3, result.executionTrace().size());
    }
}


