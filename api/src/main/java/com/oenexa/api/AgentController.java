package com.oenexa.api;

import com.oenexa.application.AgentCoordinatorService;
import com.oenexa.domain.AgentContract.WorkflowRun;
import com.oenexa.domain.Workflow;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for Agent Workflow Execution & Monitoring.
 */
@RestController
@RequestMapping("/api/v1/agents")
public class AgentController {

    private final AgentCoordinatorService coordinatorService;

    public AgentController(AgentCoordinatorService coordinatorService) {
        this.coordinatorService = coordinatorService;
    }

    public record RunRequest(String workflow, String objective) {}

    @PostMapping("/runs")
    public ResponseEntity<WorkflowRun> triggerRun(@RequestBody RunRequest request) {
        if (request.workflow() == null || request.objective() == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Workflow workflow = Workflow.valueOf(request.workflow().toUpperCase());
            WorkflowRun run = coordinatorService.executeWorkflow(workflow, request.objective());
            return ResponseEntity.ok(run);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/runs/{id}")
    public ResponseEntity<WorkflowRun> getRun(@PathVariable String id) {
        return coordinatorService.getRun(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/runs")
    public ResponseEntity<List<WorkflowRun>> listRuns() {
        return ResponseEntity.ok(coordinatorService.listRuns());
    }

    @GetMapping("/workflows")
    public ResponseEntity<List<Map<String, Object>>> listWorkflows() {
        List<Map<String, Object>> workflows = List.of(
                Map.of("id", Workflow.REPOSITORY_ANALYSIS.name(), "steps", Workflow.REPOSITORY_ANALYSIS.steps()),
                Map.of("id", Workflow.PR_REVIEW.name(), "steps", Workflow.PR_REVIEW.steps()),
                Map.of("id", Workflow.INCIDENT_RESPONSE.name(), "steps", Workflow.INCIDENT_RESPONSE.steps()),
                Map.of("id", Workflow.FEATURE_DELIVERY.name(), "steps", Workflow.FEATURE_DELIVERY.steps())
        );
        return ResponseEntity.ok(workflows);
    }
}
