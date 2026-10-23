package com.oenexa.api.dto.multimodel;

import java.util.List;

/**
 * Standard Data Transfer Objects for Enterprise Multi-Model Orchestration.
 */
public final class MultiModelDtos {

    private MultiModelDtos() {}

    public record ModelRouteRequestDto(
            String prompt,
            String taskType,
            String strategy,
            String explicitPreference
    ) {}

    public record ModelRoutingResultDto(
            String selectedModel,
            String provider,
            String reason,
            double estimatedCost,
            int priority
    ) {}

    public record FallbackExecutionRequestDto(
            String conversationId,
            String message,
            String strategy,
            String modelPreference,
            boolean simulatePrimaryFailure,
            boolean simulateSecondaryFailure
    ) {}

    public record FallbackExecutionResultDto(
            String finalOutput,
            String executedModel,
            boolean fallbackTriggered,
            List<String> attemptedModels,
            long executionTimeMs
    ) {}

    public record ModelCollaborationRequestDto(
            String taskDescription,
            String codeContext
    ) {}

    public record ModelCollaborationResultDto(
            String synthesizedSolution,
            List<String> contributingModels,
            double consensusScore
    ) {}
}
