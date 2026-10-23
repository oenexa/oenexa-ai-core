package com.oenexa.api.controller;

import com.oenexa.api.controller.*;
import com.oenexa.api.exception.*;
import com.oenexa.application.service.agent.CollaborativeAgentService;
import com.oenexa.application.service.memory.MemoryService;
import com.oenexa.application.service.agent.OenexaAutonomousAgentEngine;
import com.oenexa.application.service.knowledge.RepositoryKnowledgeService;
import com.oenexa.domain.model.chat.ChatDomain.*;
import com.oenexa.infrastructure.persistence.entity.*;
import com.oenexa.infrastructure.persistence.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OenexaAutonomousAgent PRD, Self-Healing & Multimodal Zero-Mock Tests")
class OenexaAutonomousAgentPrdAndHealingTest {

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

        OenexaGoalRepository goalRepository = (OenexaGoalRepository) Proxy.newProxyInstance(
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

        OenexaPlanStepRepository planStepRepository = (OenexaPlanStepRepository) Proxy.newProxyInstance(
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

        OenexaExecutionEventRepository executionEventRepository = (OenexaExecutionEventRepository) Proxy.newProxyInstance(
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

        OenexaSelfHealingCycleRepository selfHealingCycleRepository = (OenexaSelfHealingCycleRepository) Proxy.newProxyInstance(
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

        ProductRequirementsDocumentRepository prdRepository = (ProductRequirementsDocumentRepository) Proxy.newProxyInstance(
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

        UserStoryRepository userStoryRepository = (UserStoryRepository) Proxy.newProxyInstance(
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

        MultimodalAttachmentRepository multimodalAttachmentRepository = (MultimodalAttachmentRepository) Proxy.newProxyInstance(
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

        LocalAgentAuditLogRepository localAgentAuditLogRepository = (LocalAgentAuditLogRepository) Proxy.newProxyInstance(
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

        assertEquals(1, healingDb.size());

        assertEquals(HttpStatus.BAD_REQUEST, controller.triggerSelfHealing("g1", null).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.triggerSelfHealing("g1", new OenexaAutonomousAgentController.OenexaManualHealRequest(null, "cause")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.triggerSelfHealing("g1", new OenexaAutonomousAgentController.OenexaManualHealRequest("   ", "cause")).getStatusCode());
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

        assertEquals(1, prdDb.size());
        assertEquals(2, storyDb.size());

        assertEquals(HttpStatus.BAD_REQUEST, controller.generatePrd(null).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.generatePrd(new OenexaAutonomousAgentController.GeneratePrdRequest(null, "Teams")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.generatePrd(new OenexaAutonomousAgentController.GeneratePrdRequest("  ", "Teams")).getStatusCode());

        OenexaAutonomousAgentController standalone = new OenexaAutonomousAgentController(engine, agentService);
        ResponseEntity<ProductRequirementsDocument> standaloneResp = standalone.generatePrd(req);
        assertEquals(HttpStatus.OK, standaloneResp.getStatusCode());
    }

    @Test
    @DisplayName("Should upload multimodal attachments across image, voice, and document types")
    void shouldUploadMultimodalAttachments() {
        var imgReq = new OenexaAutonomousAgentController.MultimodalUploadRequest(
                MultimodalType.IMAGE, "arch.png", "image/png", "base64-img"
        );
        ResponseEntity<MultimodalAttachment> imgResp = controller.uploadMultimodalAttachment(imgReq);
        assertEquals(HttpStatus.OK, imgResp.getStatusCode());
        assertTrue(imgResp.getBody().extractedTextOrTranscript().contains("Extracted visual diagram context"));

        var voiceReq = new OenexaAutonomousAgentController.MultimodalUploadRequest(
                MultimodalType.AUDIO_VOICE, "directive.wav", null, null
        );
        ResponseEntity<MultimodalAttachment> voiceResp = controller.uploadMultimodalAttachment(voiceReq);
        assertEquals(HttpStatus.OK, voiceResp.getStatusCode());
        assertTrue(voiceResp.getBody().extractedTextOrTranscript().contains("Transcribed developer voice speech directive"));

        var docReq = new OenexaAutonomousAgentController.MultimodalUploadRequest(
                MultimodalType.FILE_DOCUMENT, "schema.sql", "text/plain", "CREATE TABLE ..."
        );
        ResponseEntity<MultimodalAttachment> docResp = controller.uploadMultimodalAttachment(docReq);
        assertEquals(HttpStatus.OK, docResp.getStatusCode());
        assertTrue(docResp.getBody().extractedTextOrTranscript().contains("Parsed document requirements"));

        assertEquals(3, attachmentDb.size());

        assertEquals(HttpStatus.BAD_REQUEST, controller.uploadMultimodalAttachment(null).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.uploadMultimodalAttachment(new OenexaAutonomousAgentController.MultimodalUploadRequest(null, "file", "text", "data")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.uploadMultimodalAttachment(new OenexaAutonomousAgentController.MultimodalUploadRequest(MultimodalType.IMAGE, null, "text", "data")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.uploadMultimodalAttachment(new OenexaAutonomousAgentController.MultimodalUploadRequest(MultimodalType.IMAGE, "  ", "text", "data")).getStatusCode());

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

        assertEquals(1, prdDb.size());
        assertEquals(2, storyDb.size());
        assertEquals(1, auditDb.size());
        assertEquals(1, attachmentDb.size());
    }
}



