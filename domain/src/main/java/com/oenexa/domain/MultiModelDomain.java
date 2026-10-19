package com.oenexa.domain;

import com.oenexa.domain.ChatDomain.ModelProvider;
import com.oenexa.domain.ChatDomain.TaskType;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Enterprise Multi-Model Domain contracts supporting OENEXA GPT, DeepSeek,
 * Gemini, Claude, Llama, Mistral, Qwen, and Custom Models with cost optimization,
 * speed optimization, smart fallback failover, and multi-model collaboration.
 */
public class MultiModelDomain {

    public enum OptimizationStrategy {
        AUTO,
        COST_OPTIMIZED,
        SPEED_OPTIMIZED,
        QUALITY_REASONING,
        MULTI_MODEL_COLLABORATION
    }

    public enum FallbackStatus {
        PRIMARY_SUCCESS,
        SECONDARY_FAILOVER,
        LOCAL_AIR_GAPPED_FAILOVER,
        ALL_FAILED
    }

    public record AiModelSpec(
            String modelId,
            String name,
            ModelProvider provider,
            int contextWindowTokens,
            double costPerMillionTokensUsd,
            int avgLatencyMs,
            List<String> capabilities,
            boolean isAvailable,
            boolean isLocal
    ) {
        public AiModelSpec {
            Objects.requireNonNull(modelId, "modelId must not be null");
            Objects.requireNonNull(name, "name must not be null");
            Objects.requireNonNull(provider, "provider must not be null");
            capabilities = (capabilities != null) ? List.copyOf(capabilities) : List.of();
        }

        public boolean supportsCapability(String cap) {
            return capabilities.stream().anyMatch(c -> c.equalsIgnoreCase(cap));
        }
    }

    public record SmartFallbackChain(
            String primaryModelId,
            String secondaryModelId,
            String localAirGappedModelId
    ) {
        public SmartFallbackChain {
            Objects.requireNonNull(primaryModelId, "primaryModelId must not be null");
            Objects.requireNonNull(secondaryModelId, "secondaryModelId must not be null");
            Objects.requireNonNull(localAirGappedModelId, "localAirGappedModelId must not be null");
        }

        public List<String> toList() {
            return List.of(primaryModelId, secondaryModelId, localAirGappedModelId);
        }
    }

    public record RoutingDecisionResult(
            String selectedModelId,
            String modelName,
            ModelProvider provider,
            TaskType detectedTask,
            OptimizationStrategy strategyUsed,
            double estimatedCostPerMillionTokensUsd,
            int estimatedLatencyMs,
            String rationale,
            SmartFallbackChain fallbackChain
    ) {
        public RoutingDecisionResult {
            Objects.requireNonNull(selectedModelId, "selectedModelId must not be null");
            Objects.requireNonNull(provider, "provider must not be null");
            Objects.requireNonNull(detectedTask, "detectedTask must not be null");
            Objects.requireNonNull(strategyUsed, "strategyUsed must not be null");
            Objects.requireNonNull(fallbackChain, "fallbackChain must not be null");
        }
    }

    public record MultiModelCollaborationTurn(
            String turnId,
            String architectModel,
            String architectBlueprint,
            String coderModel,
            String coderImplementation,
            String securityModel,
            String securityAudit,
            String synthesizedFinalResponse,
            List<String> contributingModels,
            Instant timestamp
    ) {
        public MultiModelCollaborationTurn {
            Objects.requireNonNull(turnId, "turnId must not be null");
            Objects.requireNonNull(synthesizedFinalResponse, "synthesizedFinalResponse must not be null");
            contributingModels = (contributingModels != null) ? List.copyOf(contributingModels) : List.of();
            timestamp = (timestamp != null) ? timestamp : Instant.now();
        }
    }

    public record ExecutionWithFallbackResult(
            String conversationId,
            String executedModelId,
            ModelProvider executedProvider,
            FallbackStatus fallbackStatus,
            int failoverAttempts,
            String responseContent,
            String thoughtProcess,
            List<String> executionTrace,
            Instant timestamp
    ) {
        public ExecutionWithFallbackResult {
            Objects.requireNonNull(executedModelId, "executedModelId must not be null");
            Objects.requireNonNull(executedProvider, "executedProvider must not be null");
            Objects.requireNonNull(fallbackStatus, "fallbackStatus must not be null");
            Objects.requireNonNull(responseContent, "responseContent must not be null");
            executionTrace = (executionTrace != null) ? List.copyOf(executionTrace) : List.of();
            timestamp = (timestamp != null) ? timestamp : Instant.now();
        }
    }
}
