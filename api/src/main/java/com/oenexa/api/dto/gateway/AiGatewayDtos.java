package com.oenexa.api.dto.gateway;

import com.oenexa.domain.model.gateway.AiGatewayDomain.PolicyAction;

import java.time.Instant;
import java.util.List;

/**
 * Enterprise AI Gateway API Data Transfer Objects (DTOs).
 * Strictly immutable boundary schemas for request/response serialization.
 */
public final class AiGatewayDtos {

    private AiGatewayDtos() {}

    public record EvaluateGatewayRequest(
            String tenantId,
            String prompt,
            String requestedModel,
            String strategy
    ) {}

    public record UpdatePolicyRequest(
            int requestsPerMinuteLimit,
            int tokensPerMinuteLimit,
            double monthlyBudgetUsd,
            boolean allowCloudProviders,
            List<String> approvedModels
    ) {}

    public record GatewayEvaluationResponse(
            String evaluationId,
            String tenantId,
            String requestedModel,
            String routedProvider,
            PolicyAction action,
            boolean sovereignEnforced,
            double remainingBudgetUsd,
            Instant evaluatedAt
    ) {}

    public record GatewayPolicyResponse(
            String policyId,
            String tenantId,
            int requestsPerMinuteLimit,
            int tokensPerMinuteLimit,
            double monthlyBudgetUsd,
            double currentMonthSpendUsd,
            boolean allowCloudProviders,
            List<String> approvedModels
    ) {}
}
