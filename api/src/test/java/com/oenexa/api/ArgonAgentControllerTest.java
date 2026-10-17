package com.oenexa.api;

import com.oenexa.application.ArgonAutonomousAgentEngine;
import com.oenexa.application.CollaborativeAgentService;
import com.oenexa.application.MemoryService;
import com.oenexa.application.RepositoryKnowledgeService;
import com.oenexa.domain.ChatDomain.*;
import com.oenexa.infrastructure.persistence.entity.ArgonExecutionEventEntity;
import com.oenexa.infrastructure.persistence.entity.ArgonGoalEntity;
import com.oenexa.infrastructure.persistence.entity.ArgonPlanStepEntity;
import com.oenexa.infrastructure.persistence.entity.ArgonSelfHealingCycleEntity;
import com.oenexa.infrastructure.persistence.repository.ArgonExecutionEventRepository;
import com.oenexa.infrastructure.persistence.repository.ArgonGoalRepository;
import com.oenexa.infrastructure.persistence.repository.ArgonPlanStepRepository;
import com.oenexa.infrastructure.persistence.repository.ArgonSelfHealingCycleRepository;
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

@DisplayName("ArgonAgentController Zero-Mock Unit Tests")
class ArgonAgentControllerTest {

    private final Map<String, ArgonGoalEntity> goalDb = new ConcurrentHashMap<>();
    private final Map<String, ArgonPlanStepEntity> planStepDb = new ConcurrentHashMap<>();
    private final Map<String, ArgonExecutionEventEntity> eventDb = new ConcurrentHashMap<>();
    private final Map<String, ArgonSelfHealingCycleEntity> healingDb = new ConcurrentHashMap<>();

    private ArgonAutonomousAgentEngine engine;
    private ArgonGoalRepository goalRepository;
    private ArgonPlanStepRepository planStepRepository;
    private ArgonExecutionEventRepository executionEventRepository;
    private ArgonSelfHealingCycleRepository selfHealingCycleRepository;
    private ArgonAgentController controller;

    @BeforeEach
    void setUp() {
        goalDb.clear();
        planStepDb.clear();
        eventDb.clear();
        healingDb.clear();

        CollaborativeAgentService agentService = new CollaborativeAgentService();
        RepositoryKnowledgeService repositoryKnowledgeService = new RepositoryKnowledgeService();
        MemoryService memoryService = new MemoryService();
        engine = new ArgonAutonomousAgentEngine(agentService, repositoryKnowledgeService, memoryService);

        goalRepository = (ArgonGoalRepository) Proxy.newProxyInstance(
                ArgonGoalRepository.class.getClassLoader(),
                new Class<?>[]{ArgonGoalRepository.class},
                (proxy, method, args) -> {
                    if ("save".equals(method.getName())) {
                        ArgonGoalEntity e = (ArgonGoalEntity) args[0];
                        goalDb.put(e.getId(), e);
                        return e;
                    }
                    return null;
                }
        );

        planStepRepository = (ArgonPlanStepRepository) Proxy.newProxyInstance(
                ArgonPlanStepRepository.class.getClassLoader(),
                new Class<?>[]{ArgonPlanStepRepository.class},
                (proxy, method, args) -> {
                    if ("save".equals(method.getName())) {
                        ArgonPlanStepEntity e = (ArgonPlanStepEntity) args[0];
                        planStepDb.put(e.getId(), e);
                        return e;
                    }
                    return null;
                }
        );

        executionEventRepository = (ArgonExecutionEventRepository) Proxy.newProxyInstance(
                ArgonExecutionEventRepository.class.getClassLoader(),
                new Class<?>[]{ArgonExecutionEventRepository.class},
                (proxy, method, args) -> {
                    if ("save".equals(method.getName())) {
                        ArgonExecutionEventEntity e = (ArgonExecutionEventEntity) args[0];
                        eventDb.put(e.getId(), e);
                        return e;
                    }
                    return null;
                }
        );

        selfHealingCycleRepository = (ArgonSelfHealingCycleRepository) Proxy.newProxyInstance(
                ArgonSelfHealingCycleRepository.class.getClassLoader(),
                new Class<?>[]{ArgonSelfHealingCycleRepository.class},
                (proxy, method, args) -> {
                    if ("save".equals(method.getName())) {
                        ArgonSelfHealingCycleEntity e = (ArgonSelfHealingCycleEntity) args[0];
                        healingDb.put(e.getId(), e);
                        return e;
                    }
                    return null;
                }
        );

        controller = new ArgonAgentController(
                engine,
                goalRepository,
                planStepRepository,
                executionEventRepository,
                selfHealingCycleRepository
        );
    }

    @Test
    @DisplayName("Should execute autonomous goal and persist entities into repositories")
    void shouldExecuteGoalAndPersist() {
        ArgonAutonomousGoalRequest request = new ArgonAutonomousGoalRequest(
                "Build scalable reactive event bus",
                "repo-delta",
                "user-dev",
                true,
                true,
                List.of()
        );

        ResponseEntity<ArgonAutonomousGoalResponse> response = controller.executeGoal(request);

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
        assertEquals(HttpStatus.BAD_REQUEST, controller.executeGoal(new ArgonAutonomousGoalRequest(null)).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.executeGoal(new ArgonAutonomousGoalRequest("   ")).getStatusCode());
    }

    @Test
    @DisplayName("Should stream autonomous goal events via SSE emitter")
    void shouldStreamGoalEvents() {
        ArgonAutonomousGoalRequest request = new ArgonAutonomousGoalRequest(
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

        SseEmitter blankEmitter = controller.streamGoal(new ArgonAutonomousGoalRequest(""));
        assertNotNull(blankEmitter);
    }

    @Test
    @DisplayName("Should retrieve all goals and single goal by ID")
    void shouldRetrieveGoals() {
        // Initial state
        ResponseEntity<List<ArgonAutonomousGoalResponse>> allEmpty = controller.getAllGoals();
        assertEquals(HttpStatus.OK, allEmpty.getStatusCode());
        assertTrue(allEmpty.getBody().isEmpty());

        // Execute a goal
        ArgonAutonomousGoalRequest req = new ArgonAutonomousGoalRequest("Design domain entity");
        ResponseEntity<ArgonAutonomousGoalResponse> executed = controller.executeGoal(req);
        String goalId = executed.getBody().goalId();

        // Get single goal (Found)
        ResponseEntity<ArgonAutonomousGoalResponse> found = controller.getGoal(goalId);
        assertEquals(HttpStatus.OK, found.getStatusCode());
        assertEquals(goalId, found.getBody().goalId());

        // Get single goal (Not Found)
        ResponseEntity<ArgonAutonomousGoalResponse> notFound = controller.getGoal("non-existent-goal");
        assertEquals(HttpStatus.NOT_FOUND, notFound.getStatusCode());

        // Get goal events
        ResponseEntity<List<ArgonExecutionEvent>> events = controller.getGoalEvents(goalId);
        assertEquals(HttpStatus.OK, events.getStatusCode());
        assertFalse(events.getBody().isEmpty());

        // Get goal healing cycles
        ResponseEntity<List<ArgonSelfHealingCycle>> cycles = controller.getGoalHealingCycles(goalId);
        assertEquals(HttpStatus.OK, cycles.getStatusCode());
        assertFalse(cycles.getBody().isEmpty());
    }

    @Test
    @DisplayName("Should trigger manual self-healing and persist cycle")
    void shouldTriggerManualSelfHealing() {
        var healReq = new ArgonAgentController.ArgonManualHealRequest(
                "StackOverflowError in recursion loop",
                "Missing base case recursion terminal check"
        );

        ResponseEntity<ArgonSelfHealingCycle> response = controller.triggerSelfHealing("goal-manual-1", healReq);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("goal-manual-1", response.getBody().goalId());
        assertEquals("StackOverflowError in recursion loop", response.getBody().issueDetected());
        assertTrue(response.getBody().resolved());

        // Verify cycle was persisted into repository
        assertEquals(1, healingDb.size());

        // Invalid heal requests
        assertEquals(HttpStatus.BAD_REQUEST, controller.triggerSelfHealing("g1", null).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.triggerSelfHealing("g1", new ArgonAgentController.ArgonManualHealRequest(null, "cause")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.triggerSelfHealing("g1", new ArgonAgentController.ArgonManualHealRequest("   ", "cause")).getStatusCode());
    }

    @Test
    @DisplayName("Should support standalone constructor without JPA repositories")
    void shouldSupportStandaloneConstructor() {
        ArgonAgentController standaloneController = new ArgonAgentController(engine);

        ArgonAutonomousGoalRequest request = new ArgonAutonomousGoalRequest("Build cache layer");
        ResponseEntity<ArgonAutonomousGoalResponse> response = standaloneController.executeGoal(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        // Trigger manual heal without repositories
        var healReq = new ArgonAgentController.ArgonManualHealRequest("Minor lint warning", "Unused import");
        ResponseEntity<ArgonSelfHealingCycle> healResp = standaloneController.triggerSelfHealing("g-standalone", healReq);
        assertEquals(HttpStatus.OK, healResp.getStatusCode());
    }

    @Test
    @DisplayName("Should enforce null safety checks on constructor")
    void shouldEnforceConstructorInvariants() {
        assertThrows(NullPointerException.class, () -> new ArgonAgentController(null));
        assertThrows(NullPointerException.class, () -> new ArgonAgentController(null, goalRepository, planStepRepository, executionEventRepository, selfHealingCycleRepository));
    }
}
