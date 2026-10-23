package com.oenexa.api;

import com.oenexa.api.dto.ApiResponse;
import com.oenexa.api.dto.agent.AgentDtos.AgentDirectiveDto;
import com.oenexa.api.dto.agent.AgentDtos.GoalExecutionStatusDto;
import com.oenexa.api.dto.agent.AgentDtos.GoalSubmissionDto;
import com.oenexa.api.mapper.AgentApiMapper;
import com.oenexa.application.AgentCoordinatorService;
import com.oenexa.application.security.AgentSecurityPolicy;
import com.oenexa.application.validation.AgentGoalValidator;
import com.oenexa.domain.AgentContract.WorkflowRun;
import com.oenexa.domain.ChatDomain.OenexaAutonomousGoalRequest;
import com.oenexa.domain.OenexaCoreBrainDomain.CoreBrainDirective;
import com.oenexa.domain.Workflow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * REST API for Agent Workflow Execution & Monitoring.
 */
@RestController
@RequestMapping("/api/v1/agents")
public class AgentController {

    private final AgentCoordinatorService coordinatorService;
    private final AgentGoalValidator agentGoalValidator;
    private final AgentSecurityPolicy agentSecurityPolicy;
    private final AgentApiMapper agentApiMapper;

    public AgentController(AgentCoordinatorService coordinatorService) {
        this(coordinatorService, new AgentGoalValidator(), new AgentSecurityPolicy(), new AgentApiMapper());
    }

    @Autowired
    public AgentController(
            AgentCoordinatorService coordinatorService,
            AgentGoalValidator agentGoalValidator,
            AgentSecurityPolicy agentSecurityPolicy,
            AgentApiMapper agentApiMapper
    ) {
        this.coordinatorService = Objects.requireNonNull(coordinatorService, "coordinatorService must not be null");
        this.agentGoalValidator = agentGoalValidator != null ? agentGoalValidator : new AgentGoalValidator();
        this.agentSecurityPolicy = agentSecurityPolicy != null ? agentSecurityPolicy : new AgentSecurityPolicy();
        this.agentApiMapper = agentApiMapper != null ? agentApiMapper : new AgentApiMapper();
    }

    public AgentGoalValidator getAgentGoalValidator() {
        return agentGoalValidator;
    }

    public AgentSecurityPolicy getAgentSecurityPolicy() {
        return agentSecurityPolicy;
    }

    public AgentApiMapper getAgentApiMapper() {
        return agentApiMapper;
    }

    public record RunRequest(String workflow, String objective) {}

    @PostMapping("/v2/goals")
    public ResponseEntity<ApiResponse<GoalExecutionStatusDto>> submitGoalV2(
            @RequestHeader(value = "X-User-Id", defaultValue = "default-user") String userId,
            @RequestBody GoalSubmissionDto submissionDto
    ) {
        if (submissionDto == null || submissionDto.goal() == null || submissionDto.goal().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        agentSecurityPolicy.verifyCommandSafety(submissionDto.goal());
        OenexaAutonomousGoalRequest domainReq = agentApiMapper.toDomainGoalRequest(submissionDto, userId);
        agentGoalValidator.validateGoalRequest(domainReq);
        WorkflowRun run = coordinatorService.executeWorkflow(Workflow.REPOSITORY_ANALYSIS, submissionDto.goal());
        GoalExecutionStatusDto statusDto = new GoalExecutionStatusDto(
                run.id(),
                submissionDto.goal(),
                run.status(),
                "EXECUTION",
                run.steps() != null ? run.steps().size() : 0,
                run.steps() != null ? run.steps().size() : 0,
                0.95,
                run.finishedAt() != null ? run.finishedAt() : Instant.now()
        );
        return ResponseEntity.ok(ApiResponse.ok("Autonomous goal submitted successfully", statusDto));
    }

    @PostMapping("/v2/directives")
    public ResponseEntity<ApiResponse<Map<String, Object>>> submitDirectiveV2(@RequestBody AgentDirectiveDto directiveDto) {
        if (directiveDto == null || directiveDto.directiveId() == null) {
            return ResponseEntity.badRequest().build();
        }
        CoreBrainDirective directive = agentApiMapper.toBrainDirective(directiveDto);
        agentGoalValidator.validateBrainDirective(directive);
        agentSecurityPolicy.verifyCommandSafety(directive.objective());
        return ResponseEntity.ok(ApiResponse.ok("Brain directive accepted", Map.of(
                "directiveId", directive.directiveId(),
                "objective", directive.objective(),
                "status", "ACTIVE"
        )));
    }

    @PostMapping("/runs")
    public ResponseEntity<WorkflowRun> triggerRun(@RequestBody RunRequest request) {
        if (request.workflow() == null || request.objective() == null) {
            return ResponseEntity.badRequest().build();
        }
        agentSecurityPolicy.verifyCommandSafety(request.objective());

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
