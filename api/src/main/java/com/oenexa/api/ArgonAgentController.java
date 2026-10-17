package com.oenexa.api;

import com.oenexa.application.ArgonAutonomousAgentEngine;
import com.oenexa.domain.ChatDomain.*;
import com.oenexa.infrastructure.persistence.entity.ArgonExecutionEventEntity;
import com.oenexa.infrastructure.persistence.entity.ArgonGoalEntity;
import com.oenexa.infrastructure.persistence.entity.ArgonPlanStepEntity;
import com.oenexa.infrastructure.persistence.entity.ArgonSelfHealingCycleEntity;
import com.oenexa.infrastructure.persistence.repository.ArgonExecutionEventRepository;
import com.oenexa.infrastructure.persistence.repository.ArgonGoalRepository;
import com.oenexa.infrastructure.persistence.repository.ArgonPlanStepRepository;
import com.oenexa.infrastructure.persistence.repository.ArgonSelfHealingCycleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * REST & SSE Controller exposing Google Gemini 4 Argon Autonomous AI Agent capabilities:
 * Goal decomposition, plan DAG execution across 7 personas, real-time trace streaming,
 * and self-healing compilation reflection cycles.
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class ArgonAgentController {

    public record ArgonManualHealRequest(String issueDetected, String rootCause) {}

    private final ArgonAutonomousAgentEngine engine;
    private final ArgonGoalRepository goalRepository;
    private final ArgonPlanStepRepository planStepRepository;
    private final ArgonExecutionEventRepository executionEventRepository;
    private final ArgonSelfHealingCycleRepository selfHealingCycleRepository;

    @Autowired
    public ArgonAgentController(
            ArgonAutonomousAgentEngine engine,
            ArgonGoalRepository goalRepository,
            ArgonPlanStepRepository planStepRepository,
            ArgonExecutionEventRepository executionEventRepository,
            ArgonSelfHealingCycleRepository selfHealingCycleRepository
    ) {
        this.engine = Objects.requireNonNull(engine, "engine must not be null");
        this.goalRepository = goalRepository;
        this.planStepRepository = planStepRepository;
        this.executionEventRepository = executionEventRepository;
        this.selfHealingCycleRepository = selfHealingCycleRepository;
    }

    public ArgonAgentController(ArgonAutonomousAgentEngine engine) {
        this(engine, null, null, null, null);
    }

    @PostMapping("/argon/goals")
    public ResponseEntity<ArgonAutonomousGoalResponse> executeGoal(@RequestBody ArgonAutonomousGoalRequest request) {
        if (request == null || request.goal() == null || request.goal().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        ArgonAutonomousGoalResponse response = engine.executeGoal(request);
        persistGoalResponseIfAvailable(request, response);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/argon/goals/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamGoal(@RequestBody ArgonAutonomousGoalRequest request) {
        SseEmitter emitter = new SseEmitter(120_000L);
        if (request == null || request.goal() == null || request.goal().isBlank()) {
            emitter.completeWithError(new IllegalArgumentException("Goal must not be empty"));
            return emitter;
        }

        try {
            ArgonAutonomousGoalResponse response = engine.streamGoalExecution(request, event -> {
                try {
                    emitter.send(SseEmitter.event().name("argon-event").data(event));
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            });
            persistGoalResponseIfAvailable(request, response);
            emitter.send(SseEmitter.event().name("argon-completed").data(response));
            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
        return emitter;
    }

    @GetMapping("/argon/goals")
    public ResponseEntity<List<ArgonAutonomousGoalResponse>> getAllGoals() {
        return ResponseEntity.ok(engine.getAllGoals());
    }

    @GetMapping("/argon/goals/{goalId}")
    public ResponseEntity<ArgonAutonomousGoalResponse> getGoal(@PathVariable String goalId) {
        return engine.getGoal(goalId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/argon/goals/{goalId}/events")
    public ResponseEntity<List<ArgonExecutionEvent>> getGoalEvents(@PathVariable String goalId) {
        return ResponseEntity.ok(engine.getGoalEvents(goalId));
    }

    @GetMapping("/argon/goals/{goalId}/healing-cycles")
    public ResponseEntity<List<ArgonSelfHealingCycle>> getGoalHealingCycles(@PathVariable String goalId) {
        return ResponseEntity.ok(engine.getGoalHealingCycles(goalId));
    }

    @PostMapping("/argon/goals/{goalId}/heal")
    public ResponseEntity<ArgonSelfHealingCycle> triggerSelfHealing(
            @PathVariable String goalId,
            @RequestBody ArgonManualHealRequest request
    ) {
        if (request == null || request.issueDetected() == null || request.issueDetected().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        ArgonSelfHealingCycle cycle = engine.triggerSelfHealing(goalId, request.issueDetected(), request.rootCause());
        if (selfHealingCycleRepository != null) {
            ArgonSelfHealingCycleEntity entity = new ArgonSelfHealingCycleEntity(
                    cycle.cycleId(),
                    cycle.goalId(),
                    cycle.issueDetected(),
                    cycle.rootCause(),
                    cycle.patchDiff(),
                    cycle.resolved()
            );
            selfHealingCycleRepository.save(entity);
        }
        return ResponseEntity.ok(cycle);
    }

    private void persistGoalResponseIfAvailable(ArgonAutonomousGoalRequest request, ArgonAutonomousGoalResponse response) {
        if (goalRepository != null) {
            ArgonGoalEntity goalEntity = new ArgonGoalEntity(
                    response.goalId(),
                    request.userId() != null ? request.userId() : "default-user",
                    request.repositoryId() != null ? request.repositoryId() : "default-repo",
                    response.goalDescription()
            );
            goalEntity.setCurrentPhase(response.plan().currentPhase().name());
            goalEntity.setAutonomousSelfHealing(request.autonomousSelfHealing());
            goalEntity.setExtendedThinking(request.extendedThinking());
            goalEntity.setConfidenceScore(response.confidenceScore());
            goalEntity.setFinalSummary(response.finalSummary());
            goalEntity.setStatus("COMPLETED");
            goalEntity.setCompletedAt(response.completedAt());
            goalRepository.save(goalEntity);

            if (planStepRepository != null && response.plan() != null && response.plan().steps() != null) {
                int idx = 0;
                for (ArgonPlanStep step : response.plan().steps()) {
                    ArgonPlanStepEntity stepEntity = new ArgonPlanStepEntity(
                            step.stepId(),
                            response.goalId(),
                            idx++,
                            step.agentPersona().name(),
                            step.description(),
                            step.toolToInvoke()
                    );
                    stepEntity.setStatus(step.status());
                    stepEntity.setOutput(step.output());
                    stepEntity.setRetryCount(step.retryCount());
                    planStepRepository.save(stepEntity);
                }
            }

            if (executionEventRepository != null && response.events() != null) {
                for (ArgonExecutionEvent evt : response.events()) {
                    ArgonExecutionEventEntity evtEntity = new ArgonExecutionEventEntity(
                            evt.eventId(),
                            evt.goalId(),
                            evt.phase().name(),
                            evt.agentPersona().name(),
                            evt.thoughtProcess(),
                            evt.toolAction(),
                            evt.outputSnippet(),
                            evt.status()
                    );
                    executionEventRepository.save(evtEntity);
                }
            }

            if (selfHealingCycleRepository != null && response.selfHealingCycles() != null) {
                for (ArgonSelfHealingCycle cycle : response.selfHealingCycles()) {
                    ArgonSelfHealingCycleEntity cycleEntity = new ArgonSelfHealingCycleEntity(
                            cycle.cycleId(),
                            cycle.goalId(),
                            cycle.issueDetected(),
                            cycle.rootCause(),
                            cycle.patchDiff(),
                            cycle.resolved()
                    );
                    selfHealingCycleRepository.save(cycleEntity);
                }
            }
        }
    }
}
