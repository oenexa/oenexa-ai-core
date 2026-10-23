package com.oenexa.api.controller;

import com.oenexa.api.dto.ApiResponse;
import com.oenexa.api.dto.multimodel.MultiModelDtos.*;
import com.oenexa.api.mapper.MultiModelApiMapper;
import com.oenexa.application.security.MultiModelSecurityPolicy;
import com.oenexa.application.service.orchestration.EnterpriseMultiModelOrchestrator;
import com.oenexa.application.validation.MultiModelValidator;
import com.oenexa.domain.model.chat.ChatDomain.TaskType;
import com.oenexa.domain.model.orchestration.MultiModelDomain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * REST API Controller for Enterprise Multi-Model System.
 * Supports standard 11-Layer Clean Hexagonal Architecture:
 * Controller -> DTO -> Validation -> Security -> Mapper -> Service -> ApiResponse<T>.
 */
@RestController
@RequestMapping("/api/v1/models")
public class MultiModelController {

    public record RouteRequest(String prompt, TaskType taskType, OptimizationStrategy strategy, String explicitPreference) {}
    public record ExecuteFallbackRequest(String conversationId, String message, OptimizationStrategy strategy, String modelPreference, boolean simulatePrimaryFailure, boolean simulateSecondaryFailure) {}
    public record CollaborateRequest(String taskDescription, String codeContext) {}

    private final EnterpriseMultiModelOrchestrator orchestrator;
    private final MultiModelValidator validator;
    private final MultiModelSecurityPolicy securityPolicy;
    private final MultiModelApiMapper mapper;

    public MultiModelController(EnterpriseMultiModelOrchestrator orchestrator) {
        this(orchestrator, new MultiModelValidator(), new MultiModelSecurityPolicy(), new MultiModelApiMapper());
    }

    @Autowired
    public MultiModelController(
            EnterpriseMultiModelOrchestrator orchestrator,
            MultiModelValidator validator,
            MultiModelSecurityPolicy securityPolicy,
            MultiModelApiMapper mapper
    ) {
        this.orchestrator = Objects.requireNonNull(orchestrator, "orchestrator must not be null");
        this.validator = Objects.requireNonNull(validator, "validator must not be null");
        this.securityPolicy = Objects.requireNonNull(securityPolicy, "securityPolicy must not be null");
        this.mapper = Objects.requireNonNull(mapper, "mapper must not be null");
    }

    @GetMapping
    public ResponseEntity<List<AiModelSpec>> listModels() {
        return ResponseEntity.ok(orchestrator.listAvailableModels());
    }

    @GetMapping("/{modelId}")
    public ResponseEntity<AiModelSpec> getModel(@PathVariable String modelId) {
        return orchestrator.getModel(modelId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/route")
    public ResponseEntity<RoutingDecisionResult> routeModel(@RequestBody RouteRequest request) {
        TaskType task = (request != null && request.taskType() != null)
                ? request.taskType()
                : orchestrator.determineTaskType(request != null ? request.prompt() : "");
        OptimizationStrategy strat = (request != null && request.strategy() != null)
                ? request.strategy()
                : OptimizationStrategy.AUTO;
        String pref = (request != null) ? request.explicitPreference() : null;

        return ResponseEntity.ok(orchestrator.routeWithStrategy(task, strat, pref));
    }

    @PostMapping("/execute")
    public ResponseEntity<ExecutionWithFallbackResult> executeWithFallback(@RequestBody ExecuteFallbackRequest request) {
        TaskType task = orchestrator.determineTaskType(request != null ? request.message() : "");
        OptimizationStrategy strat = (request != null && request.strategy() != null) ? request.strategy() : OptimizationStrategy.AUTO;
        String pref = (request != null) ? request.modelPreference() : null;
        RoutingDecisionResult routing = orchestrator.routeWithStrategy(task, strat, pref);

        boolean simPri = request != null && request.simulatePrimaryFailure();
        boolean simSec = request != null && request.simulateSecondaryFailure();
        String convId = (request != null) ? request.conversationId() : null;
        String msg = (request != null && request.message() != null) ? request.message() : "Execute software task";

        return ResponseEntity.ok(orchestrator.executeWithSmartFallback(convId, msg, routing, simPri, simSec));
    }

    @PostMapping("/collaborate")
    public ResponseEntity<MultiModelCollaborationTurn> collaborate(@RequestBody(required = false) CollaborateRequest request) {
        String desc = (request != null) ? request.taskDescription() : "Build distributed architecture";
        String ctx = (request != null) ? request.codeContext() : "";
        return ResponseEntity.ok(orchestrator.collaborateMultiModel(desc, ctx));
    }

    // ==========================================
    // 11-Layer Clean Hexagonal Architecture v2 Endpoints
    // ==========================================

    @PostMapping("/v2/route")
    public ResponseEntity<ApiResponse<ModelRoutingResultDto>> routeModelV2(@RequestBody ModelRouteRequestDto request) {
        validator.validateRouteRequest(request.prompt(), request.taskType(), request.strategy());
        securityPolicy.verifyExecutionSafety(request.prompt());

        TaskType task = request.taskType() != null ? TaskType.valueOf(request.taskType().toUpperCase()) : orchestrator.determineTaskType(request.prompt());
        OptimizationStrategy strat = OptimizationStrategy.AUTO;
        if (request.strategy() != null) {
            String s = request.strategy().toUpperCase();
            try {
                strat = OptimizationStrategy.valueOf(s);
            } catch (IllegalArgumentException ignored) {
                if (s.contains("QUALITY")) {
                    strat = OptimizationStrategy.QUALITY_REASONING;
                } else if (s.contains("COST")) {
                    strat = OptimizationStrategy.COST_OPTIMIZED;
                } else if (s.contains("SPEED")) {
                    strat = OptimizationStrategy.SPEED_OPTIMIZED;
                }
            }
        }
        RoutingDecisionResult decision = orchestrator.routeWithStrategy(task, strat, request.explicitPreference());

        return ResponseEntity.ok(ApiResponse.ok("Model routing successful", mapper.toRoutingResultDto(decision)));
    }

    @PostMapping("/v2/execute")
    public ResponseEntity<ApiResponse<FallbackExecutionResultDto>> executeWithFallbackV2(@RequestBody FallbackExecutionRequestDto request) {
        validator.validateFallbackRequest(request.message(), request.strategy());
        securityPolicy.verifyExecutionSafety(request.message());

        TaskType task = orchestrator.determineTaskType(request.message());
        OptimizationStrategy strat = OptimizationStrategy.AUTO;
        if (request.strategy() != null) {
            String s = request.strategy().toUpperCase();
            try {
                strat = OptimizationStrategy.valueOf(s);
            } catch (IllegalArgumentException ignored) {
                if (s.contains("QUALITY")) {
                    strat = OptimizationStrategy.QUALITY_REASONING;
                } else if (s.contains("COST")) {
                    strat = OptimizationStrategy.COST_OPTIMIZED;
                } else if (s.contains("SPEED")) {
                    strat = OptimizationStrategy.SPEED_OPTIMIZED;
                }
            }
        }
        RoutingDecisionResult routing = orchestrator.routeWithStrategy(task, strat, request.modelPreference());

        ExecutionWithFallbackResult result = orchestrator.executeWithSmartFallback(
                request.conversationId(),
                request.message(),
                routing,
                request.simulatePrimaryFailure(),
                request.simulateSecondaryFailure()
        );

        return ResponseEntity.ok(ApiResponse.ok("Multi-model fallback execution successful", mapper.toFallbackResultDto(result)));
    }
}
