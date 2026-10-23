package com.oenexa.api.controller;

import com.oenexa.application.service.agent.EnterpriseAgentEcosystemService;
import com.oenexa.application.service.agent.OenexaAgentThreadOrchestratorService;
import com.oenexa.application.service.agent.OenexaAgentThreadOrchestratorService.SwarmExecutionRequest;
import com.oenexa.domain.model.agent.AutonomousAgentEcosystemDomain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * Enterprise REST API Controller for Autonomous AI Agents Ecosystem.
 * Exposes autonomous agent profiles, multi-step goal planning,
 * and high-throughput concurrent agent thread dispatching using Java 25 Virtual Threads.
 */
@RestController
@RequestMapping({"/api/v1/ecosystem", "/api/v4/agents"})
@CrossOrigin(origins = "*")
public class EnterpriseAgentEcosystemController {

    public record PlanGoalRequest(String goalTitle, String objective, List<EnterpriseAgentType> requestedAgents) {}
    public record ExecutePlanRequest(String planId) {}
    public record ReflectRequest(String stepId, String output) {}

    private final EnterpriseAgentEcosystemService ecosystemService;
    private final OenexaAgentThreadOrchestratorService orchestratorService;

    public EnterpriseAgentEcosystemController() {
        this(new EnterpriseAgentEcosystemService(), new OenexaAgentThreadOrchestratorService());
    }

    @Autowired
    public EnterpriseAgentEcosystemController(
            EnterpriseAgentEcosystemService ecosystemService,
            OenexaAgentThreadOrchestratorService orchestratorService
    ) {
        this.ecosystemService = Objects.requireNonNull(ecosystemService, "ecosystemService must not be null");
        this.orchestratorService = Objects.requireNonNull(orchestratorService, "orchestratorService must not be null");
    }

    @GetMapping("/agents")
    public ResponseEntity<List<AgentProfile>> listAgents() {
        return ResponseEntity.ok(ecosystemService.listAgents());
    }

    @PostMapping("/plan")
    public ResponseEntity<AgentGoalExecutionPlan> planGoal(@RequestBody(required = false) PlanGoalRequest request) {
        String title = (request != null) ? request.goalTitle() : "Autonomous System Delivery";
        String objective = (request != null) ? request.objective() : "Full autonomous ecosystem execution";
        List<EnterpriseAgentType> agents = (request != null) ? request.requestedAgents() : null;

        return ResponseEntity.ok(ecosystemService.planGoal(title, objective, agents));
    }

    @PostMapping("/execute")
    public ResponseEntity<AgentGoalExecutionPlan> executePlan(@RequestBody(required = false) ExecutePlanRequest request) {
        String planId = (request != null) ? request.planId() : null;
        return ResponseEntity.ok(ecosystemService.executePlan(planId));
    }

    @PostMapping("/reflect")
    public ResponseEntity<AgentReflectionResult> reflectOnStep(@RequestBody(required = false) ReflectRequest request) {
        String stepId = (request != null) ? request.stepId() : "step-test";
        String output = (request != null) ? request.output() : "";
        return ResponseEntity.ok(ecosystemService.reflectOnStep(stepId, output));
    }

    // =========================================================================
    // MULTI-AGENT THREAD CONCURRENCY (JAVA 25 VIRTUAL THREADS) ENDPOINTS
    // =========================================================================

    @PostMapping("/swarm/execute")
    public ResponseEntity<ConcurrentAgentSwarmResult> executeConcurrentSwarm(
            @RequestBody(required = false) SwarmExecutionRequest request
    ) {
        SwarmExecutionRequest safeReq = (request != null) ? request : new SwarmExecutionRequest(null, null, null);
        return ResponseEntity.ok(orchestratorService.dispatchConcurrentSwarm(safeReq));
    }

    @GetMapping("/swarm/threads")
    public ResponseEntity<List<AgentThreadExecutionRecord>> getActiveThreads(
            @RequestParam(required = false) String swarmId
    ) {
        return ResponseEntity.ok(orchestratorService.getActiveThreads(swarmId));
    }

    @GetMapping("/swarm/{swarmId}")
    public ResponseEntity<ConcurrentAgentSwarmResult> getSwarmResult(@PathVariable String swarmId) {
        return orchestratorService.getSwarmResult(swarmId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}


