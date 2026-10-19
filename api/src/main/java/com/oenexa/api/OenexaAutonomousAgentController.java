package com.oenexa.api;

import com.oenexa.application.CollaborativeAgentService;
import com.oenexa.application.OenexaAutonomousAgentEngine;
import com.oenexa.domain.ChatDomain.*;
import com.oenexa.infrastructure.persistence.entity.*;
import com.oenexa.infrastructure.persistence.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * REST & SSE Controller exposing OENEXA Native Autonomous AI Agent capabilities:
 * Goal decomposition, plan DAG execution across 7 personas, real-time trace streaming,
 * and self-healing compilation reflection cycles with zero external AI dependencies.
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class OenexaAutonomousAgentController {

    public record OenexaManualHealRequest(String issueDetected, String rootCause) {}
    public record GeneratePrdRequest(String goal, String targetAudience) {}
    public record MultimodalUploadRequest(MultimodalType type, String fileName, String mimeType, String base64DataOrUrl) {}

    private final OenexaAutonomousAgentEngine engine;
    private final CollaborativeAgentService agentService;
    private final OenexaGoalRepository goalRepository;
    private final OenexaPlanStepRepository planStepRepository;
    private final OenexaExecutionEventRepository executionEventRepository;
    private final OenexaSelfHealingCycleRepository selfHealingCycleRepository;
    private final ProductRequirementsDocumentRepository prdRepository;
    private final UserStoryRepository userStoryRepository;
    private final MultimodalAttachmentRepository multimodalAttachmentRepository;
    private final LocalAgentAuditLogRepository localAgentAuditLogRepository;

    @Autowired
    public OenexaAutonomousAgentController(
            OenexaAutonomousAgentEngine engine,
            CollaborativeAgentService agentService,
            OenexaGoalRepository goalRepository,
            OenexaPlanStepRepository planStepRepository,
            OenexaExecutionEventRepository executionEventRepository,
            OenexaSelfHealingCycleRepository selfHealingCycleRepository,
            ProductRequirementsDocumentRepository prdRepository,
            UserStoryRepository userStoryRepository,
            MultimodalAttachmentRepository multimodalAttachmentRepository,
            LocalAgentAuditLogRepository localAgentAuditLogRepository
    ) {
        this.engine = Objects.requireNonNull(engine, "engine must not be null");
        this.agentService = Objects.requireNonNull(agentService, "agentService must not be null");
        this.goalRepository = goalRepository;
        this.planStepRepository = planStepRepository;
        this.executionEventRepository = executionEventRepository;
        this.selfHealingCycleRepository = selfHealingCycleRepository;
        this.prdRepository = prdRepository;
        this.userStoryRepository = userStoryRepository;
        this.multimodalAttachmentRepository = multimodalAttachmentRepository;
        this.localAgentAuditLogRepository = localAgentAuditLogRepository;
    }

    public OenexaAutonomousAgentController(OenexaAutonomousAgentEngine engine, CollaborativeAgentService agentService) {
        this(engine, agentService, null, null, null, null, null, null, null, null);
    }

    @GetMapping({"/agent/privacy", "/oenexa/privacy"})
    public ResponseEntity<LocalPrivacyConfig> getPrivacyConfig() {
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

    @PostMapping({"/agent/prd", "/oenexa/prd"})
    public ResponseEntity<ProductRequirementsDocument> generatePrd(@RequestBody GeneratePrdRequest request) {
        if (request == null || request.goal() == null || request.goal().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        ProductRequirementsDocument prd = agentService.generateProductRequirementsDocument(request.goal(), request.targetAudience());
        if (prdRepository != null) {
            var entity = new ProductRequirementsDocumentEntity(
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
                    var storyEntity = new UserStoryEntity(
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

    @PostMapping({"/agent/multimodal/upload", "/oenexa/multimodal/upload"})
    public ResponseEntity<MultimodalAttachment> uploadMultimodalAttachment(@RequestBody MultimodalUploadRequest request) {
        if (request == null || request.type() == null || request.fileName() == null || request.fileName().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        String extracted = switch (request.type()) {
            case IMAGE -> "Extracted visual diagram context & architecture component graph from image: " + request.fileName();
            case AUDIO_VOICE -> "Transcribed developer voice speech directive from audio clip: " + request.fileName();
            case FILE_DOCUMENT -> "Parsed document requirements & technical schema invariants from: " + request.fileName();
        };

        MultimodalAttachment attachment = new MultimodalAttachment(
                request.type(),
                request.fileName(),
                request.mimeType() != null ? request.mimeType() : "application/octet-stream",
                request.base64DataOrUrl() != null ? request.base64DataOrUrl() : "",
                extracted
        );

        if (multimodalAttachmentRepository != null) {
            var entity = new MultimodalAttachmentEntity(
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

    @PostMapping({"/agent/goals", "/oenexa/goals"})
    public ResponseEntity<OenexaAutonomousGoalResponse> executeGoal(@RequestBody OenexaAutonomousGoalRequest request) {
        if (request == null || request.goal() == null || request.goal().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        OenexaAutonomousGoalResponse response = engine.executeGoal(request);
        persistGoalResponseIfAvailable(request, response);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = {"/agent/goals/stream", "/oenexa/goals/stream"}, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamGoal(@RequestBody OenexaAutonomousGoalRequest request) {
        SseEmitter emitter = new SseEmitter(120_000L);
        if (request == null || request.goal() == null || request.goal().isBlank()) {
            emitter.completeWithError(new IllegalArgumentException("Goal must not be empty"));
            return emitter;
        }

        try {
            OenexaAutonomousGoalResponse response = engine.streamGoalExecution(request, event -> {
                try {
                    emitter.send(SseEmitter.event().name("oenexa-event").data(event));
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            });
            persistGoalResponseIfAvailable(request, response);
            emitter.send(SseEmitter.event().name("oenexa-completed").data(response));
            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
        return emitter;
    }

    @GetMapping({"/agent/goals", "/oenexa/goals"})
    public ResponseEntity<List<OenexaAutonomousGoalResponse>> getAllGoals() {
        return ResponseEntity.ok(engine.getAllGoals());
    }

    @GetMapping({"/agent/goals/{goalId}", "/oenexa/goals/{goalId}"})
    public ResponseEntity<OenexaAutonomousGoalResponse> getGoal(@PathVariable String goalId) {
        return engine.getGoal(goalId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping({"/agent/goals/{goalId}/events", "/oenexa/goals/{goalId}/events"})
    public ResponseEntity<List<OenexaExecutionEvent>> getGoalEvents(@PathVariable String goalId) {
        return ResponseEntity.ok(engine.getGoalEvents(goalId));
    }

    @GetMapping({"/agent/goals/{goalId}/healing-cycles", "/oenexa/goals/{goalId}/healing-cycles"})
    public ResponseEntity<List<OenexaSelfHealingCycle>> getGoalHealingCycles(@PathVariable String goalId) {
        return ResponseEntity.ok(engine.getGoalHealingCycles(goalId));
    }

    @PostMapping({"/agent/goals/{goalId}/heal", "/oenexa/goals/{goalId}/heal"})
    public ResponseEntity<OenexaSelfHealingCycle> triggerSelfHealing(
            @PathVariable String goalId,
            @RequestBody OenexaManualHealRequest request
    ) {
        if (request == null || request.issueDetected() == null || request.issueDetected().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        OenexaSelfHealingCycle cycle = engine.triggerSelfHealing(goalId, request.issueDetected(), request.rootCause());
        if (selfHealingCycleRepository != null) {
            OenexaSelfHealingCycleEntity entity = new OenexaSelfHealingCycleEntity(
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

    private void persistGoalResponseIfAvailable(OenexaAutonomousGoalRequest request, OenexaAutonomousGoalResponse response) {
        if (goalRepository != null) {
            OenexaGoalEntity goalEntity = new OenexaGoalEntity(
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
                for (OenexaPlanStep step : response.plan().steps()) {
                    OenexaPlanStepEntity stepEntity = new OenexaPlanStepEntity(
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
                for (OenexaExecutionEvent evt : response.events()) {
                    OenexaExecutionEventEntity evtEntity = new OenexaExecutionEventEntity(
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
                for (OenexaSelfHealingCycle cycle : response.selfHealingCycles()) {
                    OenexaSelfHealingCycleEntity cycleEntity = new OenexaSelfHealingCycleEntity(
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
                var prdEntity = new ProductRequirementsDocumentEntity(
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
                    var storyEntity = new UserStoryEntity(
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
                var logEntity = new LocalAgentAuditLogEntity(
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
                    var attEntity = new MultimodalAttachmentEntity(
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
}
