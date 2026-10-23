package com.oenexa.api.mapper;

import com.oenexa.api.dto.multimodel.MultiModelDtos.*;
import com.oenexa.domain.model.orchestration.MultiModelDomain.*;
import org.springframework.stereotype.Component;

/**
 * Enterprise API Mapper for Multi-Model Routing & Fallback DTOs.
 */
@Component
public class MultiModelApiMapper {

    public ModelRoutingResultDto toRoutingResultDto(RoutingDecisionResult result) {
        if (result == null) {
            return null;
        }
        return new ModelRoutingResultDto(
                result.selectedModelId(),
                result.provider() != null ? result.provider().name() : "UNKNOWN",
                result.rationale(),
                result.estimatedCostPerMillionTokensUsd(),
                result.estimatedLatencyMs()
        );
    }

    public FallbackExecutionResultDto toFallbackResultDto(ExecutionWithFallbackResult result) {
        if (result == null) {
            return null;
        }
        boolean fallbackTriggered = result.fallbackStatus() != FallbackStatus.PRIMARY_SUCCESS;
        return new FallbackExecutionResultDto(
                result.responseContent(),
                result.executedModelId(),
                fallbackTriggered,
                result.executionTrace(),
                0L
        );
    }

    public ModelCollaborationResultDto toCollaborationResultDto(MultiModelCollaborationTurn turn) {
        if (turn == null) {
            return null;
        }
        return new ModelCollaborationResultDto(
                turn.synthesizedFinalResponse(),
                turn.contributingModels(),
                1.0
        );
    }
}
