package com.oenexa.domain.model.gateway;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Enterprise AI Gateway Domain records and governance specifications.
 * Enforces rate limiting, token quotas, financial budget controls,
 * and prompt compliance before model execution.
 * Pure Java 25 Domain record (Zero framework dependencies).
 */
public final class AiGatewayDomain {

    private AiGatewayDomain() {}

    public enum PolicyAction {
        ALLOW,
        THROTTLE,
        REJECT,
        FALLBACK_SOVEREIGN
    }

    public record GatewayAccessPolicy(
            String policyId,
            String tenantId,
            int requestsPerMinuteLimit,
            int tokensPerMinuteLimit,
            double monthlyBudgetUsd,
            double currentMonthSpendUsd,
            boolean allowCloudProviders,
            List<String> approvedModels
    ) {
        public GatewayAccessPolicy {
            Objects.requireNonNull(policyId, "policyId must not be null");
            tenantId = (tenantId != null && !tenantId.isBlank()) ? tenantId : "default-tenant";
            requestsPerMinuteLimit = Math.max(1, requestsPerMinuteLimit);
            tokensPerMinuteLimit = Math.max(100, tokensPerMinuteLimit);
            monthlyBudgetUsd = Math.max(0.0, monthlyBudgetUsd);
            currentMonthSpendUsd = Math.max(0.0, currentMonthSpendUsd);
            approvedModels = (approvedModels != null) ? List.copyOf(approvedModels) : List.of();
        }

        public boolean hasBudgetRemaining(double estimatedCostUsd) {
            return (currentMonthSpendUsd + estimatedCostUsd) <= monthlyBudgetUsd;
        }

        public boolean isModelApproved(String modelId) {
            if (approvedModels.isEmpty()) {
                return true;
            }
            return approvedModels.stream().anyMatch(m -> m.equalsIgnoreCase(modelId));
        }

        public static GatewayAccessPolicy defaultTier(String tenantId) {
            return new GatewayAccessPolicy(
                    "pol-" + UUID.randomUUID(),
                    tenantId,
                    120,
                    200_000,
                    500.0,
                    0.0,
                    true,
                    List.of("oenexa-gpt-o3-max", "deepseek-v3", "gemini-2.5-flash", "claude-3-5-sonnet", "deepseek-r1:8b")
            );
        }
    }

    public record TokenUsageMetrics(
            long promptTokens,
            long completionTokens,
            long totalTokens,
            double costUsd,
            Instant recordedAt
    ) {
        public TokenUsageMetrics {
            promptTokens = Math.max(0, promptTokens);
            completionTokens = Math.max(0, completionTokens);
            totalTokens = promptTokens + completionTokens;
            costUsd = Math.max(0.0, costUsd);
            recordedAt = (recordedAt != null) ? recordedAt : Instant.now();
        }

        public static TokenUsageMetrics compute(long prompt, long completion, double ratePerMillion) {
            long total = prompt + completion;
            double cost = (total / 1_000_000.0) * ratePerMillion;
            return new TokenUsageMetrics(prompt, completion, total, cost, Instant.now());
        }
    }

    public record GovernanceCheckResult(
            boolean allowed,
            PolicyAction action,
            String violationCode,
            String rationale,
            double estimatedCostUsd
    ) {
        public GovernanceCheckResult {
            action = (action != null) ? action : PolicyAction.ALLOW;
            violationCode = (violationCode != null) ? violationCode : "NONE";
            rationale = (rationale != null) ? rationale : "Governance evaluation passed";
            estimatedCostUsd = Math.max(0.0, estimatedCostUsd);
        }

        public static GovernanceCheckResult approved(double estimatedCost) {
            return new GovernanceCheckResult(true, PolicyAction.ALLOW, "NONE", "Policy invariant checks passed", estimatedCost);
        }

        public static GovernanceCheckResult rejected(String code, String reason) {
            return new GovernanceCheckResult(false, PolicyAction.REJECT, code, reason, 0.0);
        }

        public static GovernanceCheckResult throttled(String reason) {
            return new GovernanceCheckResult(false, PolicyAction.THROTTLE, "RATE_LIMIT_EXCEEDED", reason, 0.0);
        }
    }

    public record GatewayRouteEvaluation(
            String evaluationId,
            String tenantId,
            String requestedModel,
            String routedProvider,
            PolicyAction action,
            boolean sovereignEnforced,
            double remainingBudgetUsd,
            Instant evaluatedAt
    ) {
        public GatewayRouteEvaluation {
            evaluationId = (evaluationId != null) ? evaluationId : "eval-" + UUID.randomUUID();
            tenantId = (tenantId != null) ? tenantId : "default";
            Objects.requireNonNull(requestedModel, "requestedModel must not be null");
            routedProvider = (routedProvider != null) ? routedProvider : "OENEXA_SOVEREIGN_ROUTER";
            action = (action != null) ? action : PolicyAction.ALLOW;
            remainingBudgetUsd = Math.max(0.0, remainingBudgetUsd);
            evaluatedAt = (evaluatedAt != null) ? evaluatedAt : Instant.now();
        }
    }
}
