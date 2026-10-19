package com.oenexa.api;

import com.oenexa.application.EnterpriseAgentEcosystemService;
import com.oenexa.domain.AutonomousAgentEcosystemDomain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * Enterprise REST API Controller for Phase 5: Autonomous AI Agents Ecosystem.
 * Exposes the 8 autonomous agent profiles, multi-step goal planning,
 * autonomous team execution, self-reflection, and inter-agent collaboration telemetry.
 */
@RestController
@RequestMapping("/api/v1/ecosystem")
@CrossOrigin(origins = "*")
public class EnterpriseAgentEcosystemController {

    public record PlanGoalRequest(String goalTitle, String objective, List<EnterpriseAgentType> requestedAgents) {}
    public record ExecutePlanRequest(String planId) {}
    public record ReflectRequest(String stepId, String output) {}

    private final EnterpriseAgentEcosystemService ecosystemService;

    public EnterpriseAgentEcosystemController() {
        this(new EnterpriseAgentEcosystemService());
    }

    @Autowired
    public EnterpriseAgentEcosystemController(EnterpriseAgentEcosystemService ecosystemService) {
        this.ecosystemService = Objects.requireNonNull(ecosystemService, "ecosystemService must not be null");
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
}
