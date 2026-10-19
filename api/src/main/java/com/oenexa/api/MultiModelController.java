package com.oenexa.api;

import com.oenexa.application.EnterpriseMultiModelOrchestrator;
import com.oenexa.domain.ChatDomain.TaskType;
import com.oenexa.domain.MultiModelDomain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API Controller for Enterprise Multi-Model System.
 * Exposes model catalog, dynamic strategy routing, smart fallback execution,
 * and multi-model collaboration synthesis.
 */
@RestController
@RequestMapping("/api/v1/models")
public class MultiModelController {

    public record RouteRequest(String prompt, TaskType taskType, OptimizationStrategy strategy, String explicitPreference) {}
    public record ExecuteFallbackRequest(String conversationId, String message, OptimizationStrategy strategy, String modelPreference, boolean simulatePrimaryFailure, boolean simulateSecondaryFailure) {}
    public record CollaborateRequest(String taskDescription, String codeContext) {}

    private final EnterpriseMultiModelOrchestrator orchestrator;

    @Autowired
    public MultiModelController(EnterpriseMultiModelOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
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
}
