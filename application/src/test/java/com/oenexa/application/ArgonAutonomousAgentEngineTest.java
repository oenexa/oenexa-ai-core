package com.oenexa.application;

import com.oenexa.domain.ChatDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ArgonAutonomousAgentEngine Zero-Mock Unit Tests")
class ArgonAutonomousAgentEngineTest {

    private CollaborativeAgentService agentService;
    private RepositoryKnowledgeService repositoryKnowledgeService;
    private MemoryService memoryService;
    private ArgonAutonomousAgentEngine engine;

    @BeforeEach
    void setUp() {
        agentService = new CollaborativeAgentService();
        repositoryKnowledgeService = new RepositoryKnowledgeService();
        memoryService = new MemoryService();
        engine = new ArgonAutonomousAgentEngine(agentService, repositoryKnowledgeService, memoryService);
    }

    @Test
    @DisplayName("Should autonomously execute standard engineering goal with self-healing enabled")
    void shouldExecuteStandardGoalWithSelfHealing() {
        repositoryKnowledgeService.indexRepository("repo-alpha", Map.of(
                "src/Main.java", "public class Main { public static void main(String[] args) {} }"
        ));
        memoryService.learnFromUserTurn("user-1", "I prefer strict zero-mock testing.");

        ArgonAutonomousGoalRequest request = new ArgonAutonomousGoalRequest(
                "Build reactive event streaming gateway",
                "repo-alpha",
                "user-1",
                true,
                true,
                List.of()
        );

        ArgonAutonomousGoalResponse response = engine.executeGoal(request);

        assertNotNull(response);
        assertNotNull(response.goalId());
        assertEquals("Build reactive event streaming gateway", response.goalDescription());
        assertEquals(0.99, response.confidenceScore(), 0.001);
        assertNotNull(response.plan());
        assertEquals(ArgonExecutionPhase.COMPLETED, response.plan().currentPhase());
        assertEquals(6, response.plan().steps().size());
        assertFalse(response.events().isEmpty());
        assertFalse(response.selfHealingCycles().isEmpty());
        assertFalse(response.artifacts().isEmpty());
        assertTrue(response.finalSummary().contains("Gemini 4 Argon Autonomous Goal Execution Succeeded"));

        // Verify retrieval queries
        Optional<ArgonAutonomousGoalResponse> retrieved = engine.getGoal(response.goalId());
        assertTrue(retrieved.isPresent());
        assertEquals(response.goalId(), retrieved.get().goalId());

        List<ArgonAutonomousGoalResponse> allGoals = engine.getAllGoals();
        assertEquals(1, allGoals.size());

        List<ArgonExecutionEvent> events = engine.getGoalEvents(response.goalId());
        assertEquals(response.events().size(), events.size());

        List<ArgonSelfHealingCycle> healingCycles = engine.getGoalHealingCycles(response.goalId());
        assertEquals(response.selfHealingCycles().size(), healingCycles.size());
    }

    @Test
    @DisplayName("Should detect bug intent and apply deep self-healing cycle")
    void shouldExecuteBugFixGoalWithDeepSelfHealing() {
        ArgonAutonomousGoalRequest request = new ArgonAutonomousGoalRequest(
                "Fix compilation error and repair NPE in payment service",
                "repo-beta",
                "user-2",
                true,
                true,
                List.of()
        );

        ArgonAutonomousGoalResponse response = engine.executeGoal(request);

        assertNotNull(response);
        assertFalse(response.selfHealingCycles().isEmpty());
        ArgonSelfHealingCycle cycle = response.selfHealingCycles().get(0);
        assertTrue(cycle.issueDetected().contains("boundary regression"));
        assertTrue(cycle.rootCause().contains("Defensive non-null check missing"));
        assertTrue(cycle.patchDiff().contains("Objects.requireNonNull"));
        assertTrue(cycle.resolved());
    }

    @Test
    @DisplayName("Should execute goal with autonomousSelfHealing disabled and default values")
    void shouldExecuteGoalWithoutSelfHealing() {
        ArgonAutonomousGoalRequest request = new ArgonAutonomousGoalRequest(
                "Generate architecture diagrams",
                "",
                "",
                false,
                false,
                List.of()
        );

        ArgonAutonomousGoalResponse response = engine.executeGoal(request);

        assertNotNull(response);
        assertTrue(response.selfHealingCycles().isEmpty());
    }

    @Test
    @DisplayName("Should stream execution events in real time to consumer")
    void shouldStreamExecutionEvents() {
        ArgonAutonomousGoalRequest request = new ArgonAutonomousGoalRequest("Design microservices schema");
        List<ArgonExecutionEvent> streamedEvents = new ArrayList<>();

        ArgonAutonomousGoalResponse response = engine.streamGoalExecution(request, streamedEvents::add);

        assertNotNull(response);
        assertFalse(streamedEvents.isEmpty());
        assertEquals(response.events().size(), streamedEvents.size());

        // Verify event order: PLANNING -> DECOMPOSITION -> TOOL_EXECUTION -> SELF_HEALING -> REFLECTION
        assertEquals(ArgonExecutionPhase.PLANNING, streamedEvents.get(0).phase());
        assertEquals(ArgonExecutionPhase.DECOMPOSITION, streamedEvents.get(1).phase());
    }

    @Test
    @DisplayName("Should trigger on-demand manual self-healing cycle")
    void shouldTriggerManualSelfHealing() {
        String goalId = "custom-goal-123";
        ArgonSelfHealingCycle cycle = engine.triggerSelfHealing(
                goalId,
                "Simulated Test Assertion Failure: expected 200 OK got 500",
                "Missing null check on request headers"
        );

        assertNotNull(cycle);
        assertEquals(goalId, cycle.goalId());
        assertEquals("Simulated Test Assertion Failure: expected 200 OK got 500", cycle.issueDetected());
        assertEquals("Missing null check on request headers", cycle.rootCause());
        assertTrue(cycle.resolved());

        List<ArgonSelfHealingCycle> cycles = engine.getGoalHealingCycles(goalId);
        assertEquals(1, cycles.size());

        List<ArgonExecutionEvent> events = engine.getGoalEvents(goalId);
        assertEquals(1, events.size());
        assertEquals(ArgonExecutionPhase.SELF_HEALING_VERIFICATION, events.get(0).phase());

        // Trigger with null rootCause
        ArgonSelfHealingCycle cycle2 = engine.triggerSelfHealing(goalId, "Another issue", null);
        assertNotNull(cycle2);
        assertTrue(cycle2.rootCause().contains("Identified runtime or compilation discrepancy"));
    }

    @Test
    @DisplayName("Should enforce defensive null-safety invariants")
    void shouldEnforceInvariants() {
        assertThrows(NullPointerException.class, () -> new ArgonAutonomousAgentEngine(null, repositoryKnowledgeService, memoryService));
        assertThrows(NullPointerException.class, () -> new ArgonAutonomousAgentEngine(agentService, null, memoryService));
        assertThrows(NullPointerException.class, () -> new ArgonAutonomousAgentEngine(agentService, repositoryKnowledgeService, null));
        assertThrows(NullPointerException.class, () -> engine.executeGoal(null));
        assertThrows(NullPointerException.class, () -> engine.executeGoal(new ArgonAutonomousGoalRequest(null)));
        assertThrows(NullPointerException.class, () -> engine.streamGoalExecution(null, e -> {}));
        assertThrows(NullPointerException.class, () -> engine.streamGoalExecution(new ArgonAutonomousGoalRequest("goal"), null));
        assertThrows(NullPointerException.class, () -> engine.triggerSelfHealing(null, "issue", "cause"));
        assertThrows(NullPointerException.class, () -> engine.triggerSelfHealing("g1", null, "cause"));
    }
}
