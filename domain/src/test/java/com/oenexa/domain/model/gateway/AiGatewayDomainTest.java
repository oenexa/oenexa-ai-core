package com.oenexa.domain.model.gateway;

import com.oenexa.domain.model.gateway.AiGatewayDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AiGatewayDomain Zero-Mock Unit Tests")
class AiGatewayDomainTest {

    @Test
    @DisplayName("Should initialize default gateway access policy with healthy invariants")
    void shouldInitializeDefaultGatewayAccessPolicy() {
        GatewayAccessPolicy policy = GatewayAccessPolicy.defaultTier("tenant-acme");

        assertNotNull(policy.policyId());
        assertEquals("tenant-acme", policy.tenantId());
        assertEquals(120, policy.requestsPerMinuteLimit());
        assertEquals(200_000, policy.tokensPerMinuteLimit());
        assertEquals(500.0, policy.monthlyBudgetUsd());
        assertEquals(0.0, policy.currentMonthSpendUsd());
        assertTrue(policy.allowCloudProviders());
        assertTrue(policy.isModelApproved("deepseek-v3"));
        assertTrue(policy.isModelApproved("gemini-2.5-flash"));
        assertTrue(policy.hasBudgetRemaining(10.0));
        assertFalse(policy.hasBudgetRemaining(600.0));
    }

    @Test
    @DisplayName("Should correctly compute token usage metrics and cost estimation")
    void shouldComputeTokenUsageMetrics() {
        TokenUsageMetrics metrics = TokenUsageMetrics.compute(10_000, 2_000, 0.50);

        assertEquals(10_000, metrics.promptTokens());
        assertEquals(2_000, metrics.completionTokens());
        assertEquals(12_000, metrics.totalTokens());
        assertEquals(0.006, metrics.costUsd(), 0.0001);
        assertNotNull(metrics.recordedAt());
    }

    @Test
    @DisplayName("Should evaluate governance check outcomes deterministically")
    void shouldEvaluateGovernanceCheckOutcomes() {
        GovernanceCheckResult approved = GovernanceCheckResult.approved(0.05);
        assertTrue(approved.allowed());
        assertEquals(PolicyAction.ALLOW, approved.action());
        assertEquals(0.05, approved.estimatedCostUsd());

        GovernanceCheckResult rejected = GovernanceCheckResult.rejected("BUDGET_EXCEEDED", "Tenant monthly limit reached");
        assertFalse(rejected.allowed());
        assertEquals(PolicyAction.REJECT, rejected.action());
        assertEquals("BUDGET_EXCEEDED", rejected.violationCode());

        GovernanceCheckResult throttled = GovernanceCheckResult.throttled("Exceeded 120 RPM");
        assertFalse(throttled.allowed());
        assertEquals(PolicyAction.THROTTLE, throttled.action());
    }

    @Test
    @DisplayName("Should instantiate gateway route evaluation immutably")
    void shouldInstantiateGatewayRouteEvaluation() {
        GatewayRouteEvaluation eval = new GatewayRouteEvaluation(
                "eval-101",
                "tenant-finance",
                "claude-3-5-sonnet",
                "CLOUD_FEDERATED_ROUTER",
                PolicyAction.ALLOW,
                false,
                480.50,
                null
        );

        assertEquals("eval-101", eval.evaluationId());
        assertEquals("tenant-finance", eval.tenantId());
        assertEquals("claude-3-5-sonnet", eval.requestedModel());
        assertEquals(PolicyAction.ALLOW, eval.action());
        assertFalse(eval.sovereignEnforced());
        assertEquals(480.50, eval.remainingBudgetUsd());
        assertNotNull(eval.evaluatedAt());
    }
}
