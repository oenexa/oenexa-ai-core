package com.oenexa.api;

import com.oenexa.application.ArgonAutonomousAgentEngine;
import com.oenexa.application.CollaborativeAgentService;
import com.oenexa.domain.ChatDomain.*;
import com.oenexa.infrastructure.persistence.entity.ArgonExecutionEventEntity;
import com.oenexa.infrastructure.persistence.entity.ArgonGoalEntity;
import com.oenexa.infrastructure.persistence.entity.ArgonPlanStepEntity;
import com.oenexa.infrastructure.persistence.entity.ArgonSelfHealingCycleEntity;
import com.oenexa.infrastructure.persistence.repository.ArgonExecutionEventRepository;
import com.oenexa.infrastructure.persistence.repository.ArgonGoalRepository;
import com.oenexa.infrastructure.persistence.repository.ArgonPlanStepRepository;
import com.oenexa.infrastructure.persistence.repository.ArgonSelfHealingCycleRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

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
    public record GeneratePrdRequest(String goal, String targetAudience) {}
    public record MultimodalUploadRequest(MultimodalType type, String fileName, String mimeType, String base64DataOrUrl) {}

    private final ArgonAutonomousAgentEngine engine;
    private final CollaborativeAgentService agentService;
    private final ArgonGoalRepository goalRepository;
    private final ArgonPlanStepRepository planStepRepository;
    private final ArgonExecutionEventRepository executionEventRepository;
    private final ArgonSelfHealingCycleRepository selfHealingCycleRepository;
    private final com.oenexa.infrastructure.persistence.repository.ProductRequirementsDocumentRepository prdRepository;
    private final com.oenexa.infrastructure.persistence.repository.UserStoryRepository userStoryRepository;
    private final com.oenexa.infrastructure.persistence.repository.MultimodalAttachmentRepository multimodalAttachmentRepository;
    private final com.oenexa.infrastructure.persistence.repository.LocalAgentAuditLogRepository localAgentAuditLogRepository;

    @Autowired
    public ArgonAgentController(
            ArgonAutonomousAgentEngine engine,
            CollaborativeAgentService agentService,
            ArgonGoalRepository goalRepository,
            ArgonPlanStepRepository planStepRepository,
            ArgonExecutionEventRepository executionEventRepository,
            ArgonSelfHealingCycleRepository selfHealingCycleRepository,
            com.oenexa.infrastructure.persistence.repository.ProductRequirementsDocumentRepository prdRepository,
            com.oenexa.infrastructure.persistence.repository.UserStoryRepository userStoryRepository,
            com.oenexa.infrastructure.persistence.repository.MultimodalAttachmentRepository multimodalAttachmentRepository,
            com.oenexa.infrastructure.persistence.repository.LocalAgentAuditLogRepository localAgentAuditLogRepository
    ) {
        this.engine = Objects.requireNonNull(engine, "engine must not be null");
        this.agentService = agentService != null ? agentService : new CollaborativeAgentService();
        this.goalRepository = goalRepository;
        this.planStepRepository = planStepRepository;
        this.executionEventRepository = executionEventRepository;
        this.selfHealingCycleRepository = selfHealingCycleRepository;
        this.prdRepository = prdRepository;
        this.userStoryRepository = userStoryRepository;
        this.multimodalAttachmentRepository = multimodalAttachmentRepository;
        this.localAgentAuditLogRepository = localAgentAuditLogRepository;
    }

    public ArgonAgentController(
            ArgonAutonomousAgentEngine engine,
            ArgonGoalRepository goalRepository,
            ArgonPlanStepRepository planStepRepository,
            ArgonExecutionEventRepository executionEventRepository,
            ArgonSelfHealingCycleRepository selfHealingCycleRepository
    ) {
        this(engine, new CollaborativeAgentService(), goalRepository, planStepRepository, executionEventRepository, selfHealingCycleRepository, null, null, null, null);
    }

    public ArgonAgentController(ArgonAutonomousAgentEngine engine) {
        this(engine, null, null, null, null);
    }

    @GetMapping("/argon/privacy")
    public ResponseEntity<LocalPrivacyConfig> getPrivacyStatus() {
        if (localAgentAuditLogRepository != null) {
            var latestLog = localAgentAuditLogRepository.findTopByOrderByCreatedAtDesc();
            if (latestLog.isPresent()) {
                var l = latestLog.get();
                return ResponseEntity.ok(new LocalPrivacyConfig(
                        l.isLocalOnlyMode(),
                        l.isTelemetryDisabled(),
                        l.getLocalModelEndpoint(),
                        l.getSelectedLocalModel(),
                        l.getMaxVramAllocationMb(),
                        l.isAirGappedVerification()
                ));
            }
        }
        return ResponseEntity.ok(new LocalPrivacyConfig());
    }

    @PostMapping("/argon/prd")
    public ResponseEntity<ProductRequirementsDocument> generatePrd(@RequestBody GeneratePrdRequest request) {
        if (request == null || request.goal() == null || request.goal().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        ProductRequirementsDocument prd = agentService.generateProductRequirementsDocument(request.goal(), request.targetAudience());
        if (prdRepository != null) {
            var entity = new com.oenexa.infrastructure.persistence.entity.ProductRequirementsDocumentEntity(
                    prd.prdId(),
                    prd.title(),
                    prd.targetAudience(),
                    prd.problemStatement(),
                    String.join(", ", prd.keyFeatures()),
                    String.join(", ", prd.technicalConstraints()),
                    prd.releaseRoadmap()
            );
            prdRepository.save(entity);

            if (userStoryRepository != null && prd.userStories() != null) {
                for (UserStory s : prd.userStories()) {
                    var storyEntity = new com.oenexa.infrastructure.persistence.entity.UserStoryEntity(
                            s.id(),
                            prd.prdId(),
                            s.title(),
                            s.asA(),
                            s.iWant(),
                            s.soThat(),
                            String.join("; ", s.acceptanceCriteria()),
                            s.storyPoints(),
                            s.status()
                    );
                    userStoryRepository.save(storyEntity);
                }
            }
        }
        return ResponseEntity.ok(prd);
    }

    @PostMapping("/argon/multimodal/upload")
    public ResponseEntity<MultimodalAttachment> uploadMultimodalAttachment(@RequestBody MultimodalUploadRequest request) {
        if (request == null || request.type() == null || request.fileName() == null || request.fileName().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        MultimodalAttachment attachment = getAttachment(request);

        if (multimodalAttachmentRepository != null) {
            var entity = new com.oenexa.infrastructure.persistence.entity.MultimodalAttachmentEntity(
                    attachment.id(),
                    null,
                    attachment.type().name(),
                    attachment.fileName(),
                    attachment.mimeType(),
                    attachment.base64DataOrUrl(),
                    attachment.extractedTextOrTranscript()
            );
            multimodalAttachmentRepository.save(entity);
        }
        return ResponseEntity.ok(attachment);
    }

    private static @NonNull MultimodalAttachment getAttachment(MultimodalUploadRequest request) {
        String extracted = switch (request.type()) {
            case IMAGE -> "Extracted visual diagram context & architecture component graph from image: " + request.fileName();
            case AUDIO_VOICE -> "Transcribed developer voice speech directive from audio clip: " + request.fileName();
            case FILE_DOCUMENT -> "Parsed document requirements & technical schema invariants from: " + request.fileName();
        };

        AtomicReference<MultimodalAttachment> attachment = new AtomicReference<>(new MultimodalAttachment(
                request.type(),
                request.fileName(),
                request.mimeType() != null ? request.mimeType() : "application/octet-stream",
                request.base64DataOrUrl() != null ? request.base64DataOrUrl() : "",
                extracted
        ));
        return attachment.get();
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
            ArgonGoalEntity goalEntity = createGoalEntity(request, response);
            goalRepository.save(goalEntity);

            if (planStepRepository != null && response.plan().steps() != null) {
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

            if (prdRepository != null && response.prd() != null) {
                var p = response.prd();
                var prdEntity = new com.oenexa.infrastructure.persistence.entity.ProductRequirementsDocumentEntity(
                        p.prdId(),
                        p.title(),
                        p.targetAudience(),
                        p.problemStatement(),
                        String.join(", ", p.keyFeatures()),
                        String.join(", ", p.technicalConstraints()),
                        p.releaseRoadmap()
                );
                prdRepository.save(prdEntity);
            }

            if (userStoryRepository != null && response.userStories() != null) {
                for (UserStory s : response.userStories()) {
                    var storyEntity = new com.oenexa.infrastructure.persistence.entity.UserStoryEntity(
                            s.id(),
                            response.prd() != null ? response.prd().prdId() : null,
                            s.title(),
                            s.asA(),
                            s.iWant(),
                            s.soThat(),
                            String.join("; ", s.acceptanceCriteria()),
                            s.storyPoints(),
                            s.status()
                    );
                    userStoryRepository.save(storyEntity);
                }
            }

            if (localAgentAuditLogRepository != null && response.privacyAudit() != null) {
                var priv = response.privacyAudit();
                var logEntity = new com.oenexa.infrastructure.persistence.entity.LocalAgentAuditLogEntity(
                        java.util.UUID.randomUUID().toString(),
                        response.goalId(),
                        priv.localOnlyMode(),
                        priv.telemetryDisabled(),
                        priv.localModelEndpoint(),
                        priv.selectedLocalModel(),
                        priv.maxVramAllocationMb(),
                        priv.airGappedVerification()
                );
                localAgentAuditLogRepository.save(logEntity);
            }

            if (multimodalAttachmentRepository != null && request.multimodalAttachments() != null) {
                for (MultimodalAttachment att : request.multimodalAttachments()) {
                    var attEntity = new com.oenexa.infrastructure.persistence.entity.MultimodalAttachmentEntity(
                            att.id(),
                            response.goalId(),
                            att.type().name(),
                            att.fileName(),
                            att.mimeType(),
                            att.base64DataOrUrl(),
                            att.extractedTextOrTranscript()
                    );
                    multimodalAttachmentRepository.save(attEntity);
                }
            }
        }
    }

    private static @NonNull ArgonGoalEntity createGoalEntity(ArgonAutonomousGoalRequest request, ArgonAutonomousGoalResponse response) {
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
        return goalEntity;
    }
}
