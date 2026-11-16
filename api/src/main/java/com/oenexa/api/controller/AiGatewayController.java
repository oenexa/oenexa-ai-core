package com.oenexa.api.controller;

import com.oenexa.api.dto.ApiResponse;
import com.oenexa.api.dto.gateway.AiGatewayDtos.*;
import com.oenexa.application.gateway.AiGatewayService;
import com.oenexa.domain.model.gateway.AiGatewayDomain.GatewayAccessPolicy;
import com.oenexa.domain.model.gateway.AiGatewayDomain.GatewayRouteEvaluation;
import com.oenexa.domain.model.orchestration.MultiModelDomain.AiModelSpec;
import com.oenexa.domain.model.orchestration.MultiModelDomain.OptimizationStrategy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Enterprise AI Gateway Controller.
 * Single entry point REST API for all OENEXA ecosystem AI traffic.
 * Handles:
 * - Authentication & multi-tenant access control
 * - Rate limit evaluation (RPM / TPM)
 * - Financial budget verification
 * - Provider & model routing resolution
 */
@RestController
@RequestMapping("/api/v4/gateway")
@CrossOrigin(originPatterns = "*", allowedHeaders = "*")
public class AiGatewayController {

    private final AiGatewayService gatewayService;

    public AiGatewayController(AiGatewayService gatewayService) {
        this.gatewayService = Objects.requireNonNull(gatewayService, "gatewayService must not be null");
    }

    @PostMapping("/evaluate")
    public ResponseEntity<ApiResponse<GatewayEvaluationResponse>> evaluateDirective(
            @RequestHeader(value = "X-Oenexa-Tenant-Id", defaultValue = "default-tenant") String tenantHeader,
            @RequestBody EvaluateGatewayRequest request
    ) {
        String tenantId = (request != null && request.tenantId() != null && !request.tenantId().isBlank())
                ? request.tenantId()
                : tenantHeader;

        String prompt = (request != null) ? request.prompt() : "";
        String requestedModel = (request != null) ? request.requestedModel() : null;

        OptimizationStrategy strategy = OptimizationStrategy.AUTO;
        if (request != null && request.strategy() != null && !request.strategy().isBlank()) {
            try {
                strategy = OptimizationStrategy.valueOf(request.strategy().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                strategy = OptimizationStrategy.AUTO;
            }
        }

        GatewayRouteEvaluation eval = gatewayService.evaluateAndAuthorize(tenantId, prompt, requestedModel, strategy);

        GatewayEvaluationResponse response = new GatewayEvaluationResponse(
                eval.evaluationId(),
                eval.tenantId(),
                eval.requestedModel(),
                eval.routedProvider(),
                eval.action(),
                eval.sovereignEnforced(),
                eval.remainingBudgetUsd(),
                eval.evaluatedAt()
        );

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/policy/{tenantId}")
    public ResponseEntity<ApiResponse<GatewayPolicyResponse>> getPolicy(@PathVariable String tenantId) {
        GatewayAccessPolicy policy = gatewayService.getTenantPolicy(tenantId);
        return ResponseEntity.ok(ApiResponse.ok(toPolicyResponse(policy)));
    }

    @PutMapping("/policy/{tenantId}")
    public ResponseEntity<ApiResponse<GatewayPolicyResponse>> updatePolicy(
            @PathVariable String tenantId,
            @RequestBody UpdatePolicyRequest request
    ) {
        GatewayAccessPolicy existing = gatewayService.getTenantPolicy(tenantId);
        GatewayAccessPolicy updated = new GatewayAccessPolicy(
                existing.policyId(),
                tenantId,
                request.requestsPerMinuteLimit() > 0 ? request.requestsPerMinuteLimit() : existing.requestsPerMinuteLimit(),
                request.tokensPerMinuteLimit() > 0 ? request.tokensPerMinuteLimit() : existing.tokensPerMinuteLimit(),
                request.monthlyBudgetUsd() >= 0 ? request.monthlyBudgetUsd() : existing.monthlyBudgetUsd(),
                existing.currentMonthSpendUsd(),
                request.allowCloudProviders(),
                (request.approvedModels() != null) ? request.approvedModels() : existing.approvedModels()
        );

        gatewayService.updateTenantPolicy(updated);
        return ResponseEntity.ok(ApiResponse.ok(toPolicyResponse(updated)));
    }

    @GetMapping("/models")
    public ResponseEntity<ApiResponse<List<AiModelSpec>>> listModels() {
        return ResponseEntity.ok(ApiResponse.ok(gatewayService.listAvailableModels()));
    }

    private GatewayPolicyResponse toPolicyResponse(GatewayAccessPolicy policy) {
        return new GatewayPolicyResponse(
                policy.policyId(),
                policy.tenantId(),
                policy.requestsPerMinuteLimit(),
                policy.tokensPerMinuteLimit(),
                policy.monthlyBudgetUsd(),
                policy.currentMonthSpendUsd(),
                policy.allowCloudProviders(),
                policy.approvedModels()
        );
    }
}
