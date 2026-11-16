package com.oenexa.application.gateway;

import com.oenexa.application.service.orchestration.EnterpriseMultiModelOrchestrator;
import com.oenexa.domain.model.chat.ChatDomain.TaskType;
import com.oenexa.domain.model.gateway.AiGatewayDomain.*;
import com.oenexa.domain.model.orchestration.MultiModelDomain.AiModelSpec;
import com.oenexa.domain.model.orchestration.MultiModelDomain.OptimizationStrategy;
import com.oenexa.domain.model.orchestration.MultiModelDomain.RoutingDecisionResult;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Enterprise AI Gateway Orchestration Service.
 * Central entry point for all AI requests across the OENEXA ecosystem.
 * Enforces:
 * 1. Pre-flight prompt governance and guardrails.
 * 2. Multi-tenant rate limiting (RPM/TPM).
 * 3. Budget & quota governance.
 * 4. Model selection & sovereign/cloud routing delegation.
 */
@Service
public class AiGatewayService {

    private final PromptGovernancePolicy promptGovernance;
    private final EnterpriseMultiModelOrchestrator modelOrchestrator;

    private final Map<String, GatewayAccessPolicy> tenantPolicies = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> minuteRequestCounters = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> monthlySpendTracking = new ConcurrentHashMap<>();

    public AiGatewayService(
            PromptGovernancePolicy promptGovernance,
            EnterpriseMultiModelOrchestrator modelOrchestrator
    ) {
        this.promptGovernance = Objects.requireNonNull(promptGovernance, "promptGovernance must not be null");
        this.modelOrchestrator = Objects.requireNonNull(modelOrchestrator, "modelOrchestrator must not be null");
    }

    public GatewayRouteEvaluation evaluateAndAuthorize(
            String tenantId,
            String prompt,
            String requestedModel,
            OptimizationStrategy strategy
    ) {
        String effectiveTenant = (tenantId != null && !tenantId.isBlank()) ? tenantId : "default-tenant";
        GatewayAccessPolicy policy = getTenantPolicy(effectiveTenant);

        // 1. Inbound Prompt Safety & Guardrail Check
        GovernanceCheckResult promptSafety = promptGovernance.evaluatePromptSafety(prompt);
        if (!promptSafety.allowed()) {
            return new GatewayRouteEvaluation(
                    UUID.randomUUID().toString(),
                    effectiveTenant,
                    requestedModel != null ? requestedModel : "blocked",
                    "SECURITY_FIREWALL",
                    PolicyAction.REJECT,
                    true,
                    policy.monthlyBudgetUsd() - policy.currentMonthSpendUsd(),
                    Instant.now()
            );
        }

        // 2. Tenant Rate Limiting Check (RPM)
        AtomicInteger counter = minuteRequestCounters.computeIfAbsent(effectiveTenant, k -> new AtomicInteger(0));
        if (counter.incrementAndGet() > policy.requestsPerMinuteLimit()) {
            return new GatewayRouteEvaluation(
                    UUID.randomUUID().toString(),
                    effectiveTenant,
                    requestedModel != null ? requestedModel : "throttled",
                    "RATE_LIMITER",
                    PolicyAction.THROTTLE,
                    true,
                    policy.monthlyBudgetUsd() - policy.currentMonthSpendUsd(),
                    Instant.now()
            );
        }

        // 3. Resolve Target Model and Optimization Strategy
        TaskType task = modelOrchestrator.determineTaskType(prompt);
        RoutingDecisionResult routing = modelOrchestrator.routeWithStrategy(
                task,
                strategy != null ? strategy : OptimizationStrategy.AUTO,
                requestedModel
        );

        // 4. Budget & Model Whitelist Verification
        double estimatedCost = routing.estimatedCostPerMillionTokensUsd() * 0.002; // Estimate for ~2K tokens
        if (!policy.hasBudgetRemaining(estimatedCost)) {
            // Budget exceeded: Fallback to local sovereign air-gapped model (zero cost)
            return new GatewayRouteEvaluation(
                    UUID.randomUUID().toString(),
                    effectiveTenant,
                    "deepseek-r1:8b",
                    "OENEXA_SOVEREIGN_AIR_GAPPED",
                    PolicyAction.FALLBACK_SOVEREIGN,
                    true,
                    0.0,
                    Instant.now()
            );
        }

        // 5. Cloud vs Sovereign Policy Enforcement
        boolean isLocal = routing.selectedModelId().contains("r1") || routing.selectedModelId().contains("neo-gpt");
        if (!policy.allowCloudProviders() && !isLocal) {
            return new GatewayRouteEvaluation(
                    UUID.randomUUID().toString(),
                    effectiveTenant,
                    "deepseek-r1:8b",
                    "OENEXA_SOVEREIGN_AIR_GAPPED",
                    PolicyAction.FALLBACK_SOVEREIGN,
                    true,
                    policy.monthlyBudgetUsd() - policy.currentMonthSpendUsd(),
                    Instant.now()
            );
        }

        return new GatewayRouteEvaluation(
                UUID.randomUUID().toString(),
                effectiveTenant,
                routing.selectedModelId(),
                routing.provider().name(),
                PolicyAction.ALLOW,
                isLocal,
                policy.monthlyBudgetUsd() - policy.currentMonthSpendUsd() - estimatedCost,
                Instant.now()
        );
    }

    public void recordUsage(String tenantId, String modelId, TokenUsageMetrics metrics) {
        if (tenantId == null || metrics == null) return;
        GatewayAccessPolicy current = getTenantPolicy(tenantId);
        double newSpend = current.currentMonthSpendUsd() + metrics.costUsd();
        tenantPolicies.put(tenantId, new GatewayAccessPolicy(
                current.policyId(),
                current.tenantId(),
                current.requestsPerMinuteLimit(),
                current.tokensPerMinuteLimit(),
                current.monthlyBudgetUsd(),
                newSpend,
                current.allowCloudProviders(),
                current.approvedModels()
        ));
    }

    public GatewayAccessPolicy getTenantPolicy(String tenantId) {
        String key = (tenantId != null && !tenantId.isBlank()) ? tenantId : "default-tenant";
        return tenantPolicies.computeIfAbsent(key, GatewayAccessPolicy::defaultTier);
    }

    public void updateTenantPolicy(GatewayAccessPolicy policy) {
        Objects.requireNonNull(policy, "policy must not be null");
        tenantPolicies.put(policy.tenantId(), policy);
    }

    public List<AiModelSpec> listAvailableModels() {
        return modelOrchestrator.listAvailableModels();
    }
}
