package com.oenexa.api;

import com.oenexa.application.CollaborativeAgentService;
import com.oenexa.application.MemoryService;
import com.oenexa.application.OenexaAutonomousAgentEngine;
import com.oenexa.application.RepositoryKnowledgeService;
import com.oenexa.domain.ChatDomain.*;
import com.oenexa.infrastructure.persistence.entity.*;
import com.oenexa.infrastructure.persistence.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OenexaAutonomousAgentController Zero-Mock Unit Tests")
class OenexaAutonomousAgentControllerTest {

    private final Map<String, OenexaGoalEntity> goalDb = new ConcurrentHashMap<>();
    private final Map<String, OenexaPlanStepEntity> planStepDb = new ConcurrentHashMap<>();
    private final Map<String, OenexaExecutionEventEntity> eventDb = new ConcurrentHashMap<>();
    private final Map<String, OenexaSelfHealingCycleEntity> healingDb = new ConcurrentHashMap<>();
    private final Map<String, ProductRequirementsDocumentEntity> prdDb = new ConcurrentHashMap<>();
    private final Map<String, UserStoryEntity> storyDb = new ConcurrentHashMap<>();
    private final Map<String, MultimodalAttachmentEntity> attachmentDb = new ConcurrentHashMap<>();
    private final Map<String, LocalAgentAuditLogEntity> auditDb = new ConcurrentHashMap<>();

    private OenexaAutonomousAgentEngine engine;
    private CollaborativeAgentService agentService;
    private OenexaGoalRepository goalRepository;
    private OenexaPlanStepRepository planStepRepository;
    private OenexaExecutionEventRepository executionEventRepository;
    private OenexaSelfHealingCycleRepository selfHealingCycleRepository;
    private ProductRequirementsDocumentRepository prdRepository;
    private UserStoryRepository userStoryRepository;
    private MultimodalAttachmentRepository multimodalAttachmentRepository;
    private LocalAgentAuditLogRepository localAgentAuditLogRepository;
    private OenexaAutonomousAgentController controller;

    @BeforeEach
    void setUp() {
        goalDb.clear();
        planStepDb.clear();
        eventDb.clear();
        healingDb.clear();
        prdDb.clear();
        storyDb.clear();
        attachmentDb.clear();
        auditDb.clear();

        agentService = new CollaborativeAgentService();
        RepositoryKnowledgeService repositoryKnowledgeService = new RepositoryKnowledgeService();
        MemoryService memoryService = new MemoryService();
        engine = new OenexaAutonomousAgentEngine(agentService, repositoryKnowledgeService, memoryService);

        goalRepository = (OenexaGoalRepository) Proxy.newProxyInstance(
                OenexaGoalRepository.class.getClassLoader(),
                new Class<?>[]{OenexaGoalRepository.class},
                (proxy, method, args) -> {
                    if ("save".equals(method.getName())) {
                        OenexaGoalEntity e = (OenexaGoalEntity) args[0];
                        goalDb.put(e.getId(), e);
                        return e;
                    }
                    return null;
                }
        );

        planStepRepository = (OenexaPlanStepRepository) Proxy.newProxyInstance(
                OenexaPlanStepRepository.class.getClassLoader(),
                new Class<?>[]{OenexaPlanStepRepository.class},
                (proxy, method, args) -> {
                    if ("save".equals(method.getName())) {
                        OenexaPlanStepEntity e = (OenexaPlanStepEntity) args[0];
                        planStepDb.put(e.getId(), e);
                        return e;
                    }
                    return null;
                }
        );

        executionEventRepository = (OenexaExecutionEventRepository) Proxy.newProxyInstance(
                OenexaExecutionEventRepository.class.getClassLoader(),
                new Class<?>[]{OenexaExecutionEventRepository.class},
                (proxy, method, args) -> {
                    if ("save".equals(method.getName())) {
                        OenexaExecutionEventEntity e = (OenexaExecutionEventEntity) args[0];
                        eventDb.put(e.getId(), e);
                        return e;
                    }
                    return null;
                }
        );

        selfHealingCycleRepository = (OenexaSelfHealingCycleRepository) Proxy.newProxyInstance(
                OenexaSelfHealingCycleRepository.class.getClassLoader(),
                new Class<?>[]{OenexaSelfHealingCycleRepository.class},
                (proxy, method, args) -> {
                    if ("save".equals(method.getName())) {
                        OenexaSelfHealingCycleEntity e = (OenexaSelfHealingCycleEntity) args[0];
                        healingDb.put(e.getId(), e);
                        return e;
                    }
                    return null;
                }
        );

        prdRepository = (ProductRequirementsDocumentRepository) Proxy.newProxyInstance(
                ProductRequirementsDocumentRepository.class.getClassLoader(),
                new Class<?>[]{ProductRequirementsDocumentRepository.class},
                (proxy, method, args) -> {
                    if ("save".equals(method.getName())) {
                        var e = (ProductRequirementsDocumentEntity) args[0];
                        prdDb.put(e.getId(), e);
                        return e;
                    }
                    return null;
                }
        );

        userStoryRepository = (UserStoryRepository) Proxy.newProxyInstance(
                UserStoryRepository.class.getClassLoader(),
                new Class<?>[]{UserStoryRepository.class},
                (proxy, method, args) -> {
                    if ("save".equals(method.getName())) {
                        var e = (UserStoryEntity) args[0];
                        storyDb.put(e.getId(), e);
                        return e;
                    }
                    return null;
                }
        );

        multimodalAttachmentRepository = (MultimodalAttachmentRepository) Proxy.newProxyInstance(
                MultimodalAttachmentRepository.class.getClassLoader(),
                new Class<?>[]{MultimodalAttachmentRepository.class},
                (proxy, method, args) -> {
                    if ("save".equals(method.getName())) {
                        var e = (MultimodalAttachmentEntity) args[0];
                        attachmentDb.put(e.getId(), e);
                        return e;
                    }
                    return null;
                }
        );

        localAgentAuditLogRepository = (LocalAgentAuditLogRepository) Proxy.newProxyInstance(
                LocalAgentAuditLogRepository.class.getClassLoader(),
                new Class<?>[]{LocalAgentAuditLogRepository.class},
                (proxy, method, args) -> {
                    if ("save".equals(method.getName())) {
                        var e = (LocalAgentAuditLogEntity) args[0];
                        auditDb.put(e.getId(), e);
                        return e;
                    }
                    if ("findTopByOrderByCreatedAtDesc".equals(method.getName())) {
                        return auditDb.values().stream().findFirst();
                    }
                    return null;
                }
        );

        controller = new OenexaAutonomousAgentController(
                engine,
                agentService,
                goalRepository,
                planStepRepository,
                executionEventRepository,
                selfHealingCycleRepository,
                prdRepository,
                userStoryRepository,
                multimodalAttachmentRepository,
                localAgentAuditLogRepository
        );
    }

    @Test
    @DisplayName("Should execute autonomous goal and persist entities into repositories")
    void shouldExecuteGoalAndPersist() {
        OenexaAutonomousGoalRequest request = new OenexaAutonomousGoalRequest(
                "Build scalable reactive event bus",
                "repo-delta",
                "user-dev",
                true,
                true,
                List.of()
        );

        ResponseEntity<OenexaAutonomousGoalResponse> response = controller.executeGoal(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Build scalable reactive event bus", response.getBody().goalDescription());
        assertEquals(0.99, response.getBody().confidenceScore(), 0.001);

        // Verify entities were persisted
        assertEquals(1, goalDb.size());
        assertFalse(planStepDb.isEmpty());
        assertFalse(eventDb.isEmpty());
        assertFalse(healingDb.isEmpty());
    }

    @Test
    @DisplayName("Should reject invalid goal execution with 400 Bad Request")
    void shouldRejectInvalidGoal() {
        assertEquals(HttpStatus.BAD_REQUEST, controller.executeGoal(null).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.executeGoal(new OenexaAutonomousGoalRequest(null)).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.executeGoal(new OenexaAutonomousGoalRequest("   ")).getStatusCode());
    }

    @Test
    @DisplayName("Should stream autonomous goal events via SSE emitter")
    void shouldStreamGoalEvents() {
        OenexaAutonomousGoalRequest request = new OenexaAutonomousGoalRequest(
                "Orchestrate microservices with K8s",
                "repo-stream",
                "user-stream",
                true,
                true,
                List.of()
        );

        SseEmitter emitter = controller.streamGoal(request);
        assertNotNull(emitter);

        // Test invalid stream requests
        SseEmitter nullEmitter = controller.streamGoal(null);
        assertNotNull(nullEmitter);

        SseEmitter blankEmitter = controller.streamGoal(new OenexaAutonomousGoalRequest(""));
        assertNotNull(blankEmitter);
    }

    @Test
    @DisplayName("Should retrieve all goals and single goal by ID")
    void shouldRetrieveGoals() {
        // Initial state
        ResponseEntity<List<OenexaAutonomousGoalResponse>> allEmpty = controller.getAllGoals();
        assertEquals(HttpStatus.OK, allEmpty.getStatusCode());
        assertTrue(allEmpty.getBody().isEmpty());

        // Execute a goal
        OenexaAutonomousGoalRequest req = new OenexaAutonomousGoalRequest("Design domain entity");
        ResponseEntity<OenexaAutonomousGoalResponse> executed = controller.executeGoal(req);
        String goalId = executed.getBody().goalId();

        // Get single goal (Found)
        ResponseEntity<OenexaAutonomousGoalResponse> found = controller.getGoal(goalId);
        assertEquals(HttpStatus.OK, found.getStatusCode());
        assertEquals(goalId, found.getBody().goalId());

        // Get single goal (Not Found)
        ResponseEntity<OenexaAutonomousGoalResponse> notFound = controller.getGoal("non-existent-goal");
        assertEquals(HttpStatus.NOT_FOUND, notFound.getStatusCode());

        // Get goal events
        ResponseEntity<List<OenexaExecutionEvent>> events = controller.getGoalEvents(goalId);
        assertEquals(HttpStatus.OK, events.getStatusCode());
        assertFalse(events.getBody().isEmpty());

        // Get goal healing cycles
        ResponseEntity<List<OenexaSelfHealingCycle>> cycles = controller.getGoalHealingCycles(goalId);
        assertEquals(HttpStatus.OK, cycles.getStatusCode());
        assertFalse(cycles.getBody().isEmpty());
    }

    @Test
    @DisplayName("Should trigger manual self-healing and persist cycle")
    void shouldTriggerManualSelfHealing() {
        var healReq = new OenexaAutonomousAgentController.OenexaManualHealRequest(
                "StackOverflowError in recursion loop",
                "Missing base case recursion terminal check"
        );

        ResponseEntity<OenexaSelfHealingCycle> response = controller.triggerSelfHealing("goal-manual-1", healReq);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("goal-manual-1", response.getBody().goalId());
        assertEquals("StackOverflowError in recursion loop", response.getBody().issueDetected());
        assertTrue(response.getBody().resolved());

        // Verify cycle was persisted into repository
        assertEquals(1, healingDb.size());

        // Invalid heal requests
        assertEquals(HttpStatus.BAD_REQUEST, controller.triggerSelfHealing("g1", null).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.triggerSelfHealing("g1", new OenexaAutonomousAgentController.OenexaManualHealRequest(null, "cause")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.triggerSelfHealing("g1", new OenexaAutonomousAgentController.OenexaManualHealRequest("   ", "cause")).getStatusCode());
    }

    @Test
    @DisplayName("Should support standalone constructor without JPA repositories")
    void shouldSupportStandaloneConstructor() {
        OenexaAutonomousAgentController standaloneController = new OenexaAutonomousAgentController(engine, agentService);

        OenexaAutonomousGoalRequest request = new OenexaAutonomousGoalRequest("Build cache layer");
        ResponseEntity<OenexaAutonomousGoalResponse> response = standaloneController.executeGoal(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        // Trigger manual heal without repositories
        var healReq = new OenexaAutonomousAgentController.OenexaManualHealRequest("Minor lint warning", "Unused import");
        ResponseEntity<OenexaSelfHealingCycle> healResp = standaloneController.triggerSelfHealing("g-standalone", healReq);
        assertEquals(HttpStatus.OK, healResp.getStatusCode());
    }

    @Test
    @DisplayName("Should enforce null safety checks on constructor")
    void shouldEnforceConstructorInvariants() {
        assertThrows(NullPointerException.class, () -> new OenexaAutonomousAgentController(null, agentService));
        assertThrows(NullPointerException.class, () -> new OenexaAutonomousAgentController(engine, null));
    }

    @Test
    @DisplayName("Should retrieve privacy status (default and from repository)")
    void shouldRetrievePrivacyStatus() {
        // Initial default
        ResponseEntity<LocalPrivacyConfig> defaultResp = controller.getPrivacyConfig();
        assertEquals(HttpStatus.OK, defaultResp.getStatusCode());
        assertNotNull(defaultResp.getBody());
        assertTrue(defaultResp.getBody().localOnlyMode());
        assertTrue(defaultResp.getBody().airGappedVerification());

        // Put an audit log into auditDb
        auditDb.put("log-1", new LocalAgentAuditLogEntity(
                "log-1", "goal-1", true, true, "in-process-native-neural-engine", "oenexa-neo-gpt-o3-max", 8192, true
        ));
        ResponseEntity<LocalPrivacyConfig> persistedResp = controller.getPrivacyConfig();
        assertEquals(HttpStatus.OK, persistedResp.getStatusCode());
        assertEquals("oenexa-neo-gpt-o3-max", persistedResp.getBody().selectedLocalModel());

        // Standalone without repository
        OenexaAutonomousAgentController standalone = new OenexaAutonomousAgentController(engine, agentService);
        ResponseEntity<LocalPrivacyConfig> standaloneResp = standalone.getPrivacyConfig();
        assertEquals(HttpStatus.OK, standaloneResp.getStatusCode());
        assertTrue(standaloneResp.getBody().localOnlyMode());
    }

    @Test
    @DisplayName("Should generate PRD and persist user stories")
    void shouldGeneratePrdAndPersist() {
        var req = new OenexaAutonomousAgentController.GeneratePrdRequest("Build Autonomous AI Platform", "Enterprise Teams");
        ResponseEntity<ProductRequirementsDocument> resp = controller.generatePrd(req);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("PRD: Build Autonomous AI Platform", resp.getBody().title());
        assertEquals(2, resp.getBody().userStories().size());

        // Verify persistence
        assertEquals(1, prdDb.size());
        assertEquals(2, storyDb.size());

        // Invalid requests
        assertEquals(HttpStatus.BAD_REQUEST, controller.generatePrd(null).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.generatePrd(new OenexaAutonomousAgentController.GeneratePrdRequest(null, "Teams")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.generatePrd(new OenexaAutonomousAgentController.GeneratePrdRequest("  ", "Teams")).getStatusCode());

        // Standalone controller without repository
        OenexaAutonomousAgentController standalone = new OenexaAutonomousAgentController(engine, agentService);
        ResponseEntity<ProductRequirementsDocument> standaloneResp = standalone.generatePrd(req);
        assertEquals(HttpStatus.OK, standaloneResp.getStatusCode());
    }

    @Test
    @DisplayName("Should upload multimodal attachments across image, voice, and document types")
    void shouldUploadMultimodalAttachments() {
        // IMAGE
        var imgReq = new OenexaAutonomousAgentController.MultimodalUploadRequest(
                MultimodalType.IMAGE, "arch.png", "image/png", "base64-img"
        );
        ResponseEntity<MultimodalAttachment> imgResp = controller.uploadMultimodalAttachment(imgReq);
        assertEquals(HttpStatus.OK, imgResp.getStatusCode());
        assertTrue(imgResp.getBody().extractedTextOrTranscript().contains("Extracted visual diagram context"));

        // AUDIO_VOICE
        var voiceReq = new OenexaAutonomousAgentController.MultimodalUploadRequest(
                MultimodalType.AUDIO_VOICE, "directive.wav", null, null
        );
        ResponseEntity<MultimodalAttachment> voiceResp = controller.uploadMultimodalAttachment(voiceReq);
        assertEquals(HttpStatus.OK, voiceResp.getStatusCode());
        assertTrue(voiceResp.getBody().extractedTextOrTranscript().contains("Transcribed developer voice speech directive"));

        // FILE_DOCUMENT
        var docReq = new OenexaAutonomousAgentController.MultimodalUploadRequest(
                MultimodalType.FILE_DOCUMENT, "schema.sql", "text/plain", "CREATE TABLE ..."
        );
        ResponseEntity<MultimodalAttachment> docResp = controller.uploadMultimodalAttachment(docReq);
        assertEquals(HttpStatus.OK, docResp.getStatusCode());
        assertTrue(docResp.getBody().extractedTextOrTranscript().contains("Parsed document requirements"));

        // Verify persistence in attachmentDb
        assertEquals(3, attachmentDb.size());

        // Invalid upload requests
        assertEquals(HttpStatus.BAD_REQUEST, controller.uploadMultimodalAttachment(null).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.uploadMultimodalAttachment(new OenexaAutonomousAgentController.MultimodalUploadRequest(null, "file", "text", "data")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.uploadMultimodalAttachment(new OenexaAutonomousAgentController.MultimodalUploadRequest(MultimodalType.IMAGE, null, "text", "data")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.uploadMultimodalAttachment(new OenexaAutonomousAgentController.MultimodalUploadRequest(MultimodalType.IMAGE, "  ", "text", "data")).getStatusCode());

        // Standalone without repository
        OenexaAutonomousAgentController standalone = new OenexaAutonomousAgentController(engine, agentService);
        ResponseEntity<MultimodalAttachment> standaloneResp = standalone.uploadMultimodalAttachment(imgReq);
        assertEquals(HttpStatus.OK, standaloneResp.getStatusCode());
    }

    @Test
    @DisplayName("Should execute goal with multimodal attachments and persist all entities")
    void shouldExecuteGoalWithMultimodalAttachmentsAndPersistAll() {
        MultimodalAttachment att = new MultimodalAttachment(
                MultimodalType.IMAGE, "c4_model.png", "image/png", "base64data", "C4 Model"
        );

        OenexaAutonomousGoalRequest request = new OenexaAutonomousGoalRequest(
                "Build Event-Driven Order Processing Engine",
                "repo-orders",
                "user-orders",
                true,
                true,
                List.of(),
                true,
                List.of(att)
        );

        ResponseEntity<OenexaAutonomousGoalResponse> response = controller.executeGoal(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        // Verify PRD, user stories, audit logs, and attachments were persisted into DB
        assertEquals(1, prdDb.size());
        assertEquals(2, storyDb.size());
        assertEquals(1, auditDb.size());
        assertEquals(1, attachmentDb.size());
    }
}
