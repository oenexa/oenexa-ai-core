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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OenexaAutonomousAgent Goal Execution & Lifecycle Zero-Mock Tests")
class OenexaAutonomousAgentGoalExecutionTest {

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

        SseEmitter nullEmitter = controller.streamGoal(null);
        assertNotNull(nullEmitter);

        SseEmitter blankEmitter = controller.streamGoal(new OenexaAutonomousGoalRequest(""));
        assertNotNull(blankEmitter);
    }

    @Test
    @DisplayName("Should retrieve all goals and single goal by ID")
    void shouldRetrieveGoals() {
        ResponseEntity<List<OenexaAutonomousGoalResponse>> allEmpty = controller.getAllGoals();
        assertEquals(HttpStatus.OK, allEmpty.getStatusCode());
        assertTrue(allEmpty.getBody().isEmpty());

        OenexaAutonomousGoalRequest req = new OenexaAutonomousGoalRequest("Design domain entity");
        ResponseEntity<OenexaAutonomousGoalResponse> executed = controller.executeGoal(req);
        String goalId = executed.getBody().goalId();

        ResponseEntity<OenexaAutonomousGoalResponse> found = controller.getGoal(goalId);
        assertEquals(HttpStatus.OK, found.getStatusCode());
        assertEquals(goalId, found.getBody().goalId());

        ResponseEntity<OenexaAutonomousGoalResponse> notFound = controller.getGoal("non-existent-goal");
        assertEquals(HttpStatus.NOT_FOUND, notFound.getStatusCode());

        ResponseEntity<List<OenexaExecutionEvent>> events = controller.getGoalEvents(goalId);
        assertEquals(HttpStatus.OK, events.getStatusCode());
        assertFalse(events.getBody().isEmpty());

        ResponseEntity<List<OenexaSelfHealingCycle>> cycles = controller.getGoalHealingCycles(goalId);
        assertEquals(HttpStatus.OK, cycles.getStatusCode());
        assertFalse(cycles.getBody().isEmpty());
    }

    @Test
    @DisplayName("Should support standalone constructor without JPA repositories")
    void shouldSupportStandaloneConstructor() {
        OenexaAutonomousAgentController standaloneController = new OenexaAutonomousAgentController(engine, agentService);

        OenexaAutonomousGoalRequest request = new OenexaAutonomousGoalRequest("Build cache layer");
        ResponseEntity<OenexaAutonomousGoalResponse> response = standaloneController.executeGoal(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

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
        ResponseEntity<LocalPrivacyConfig> defaultResp = controller.getPrivacyConfig();
        assertEquals(HttpStatus.OK, defaultResp.getStatusCode());
        assertNotNull(defaultResp.getBody());
        assertTrue(defaultResp.getBody().localOnlyMode());
        assertTrue(defaultResp.getBody().airGappedVerification());

        auditDb.put("log-1", new LocalAgentAuditLogEntity(
                "log-1", "goal-1", true, true, "in-process-native-neural-engine", "oenexa-neo-gpt-o3-max", 8192, true
        ));
        ResponseEntity<LocalPrivacyConfig> persistedResp = controller.getPrivacyConfig();
        assertEquals(HttpStatus.OK, persistedResp.getStatusCode());
        assertEquals("oenexa-neo-gpt-o3-max", persistedResp.getBody().selectedLocalModel());

        OenexaAutonomousAgentController standalone = new OenexaAutonomousAgentController(engine, agentService);
        ResponseEntity<LocalPrivacyConfig> standaloneResp = standalone.getPrivacyConfig();
        assertEquals(HttpStatus.OK, standaloneResp.getStatusCode());
        assertTrue(standaloneResp.getBody().localOnlyMode());
    }
}



