package com.oenexa.application.service.agent;

import com.oenexa.application.port.*;
import com.oenexa.application.service.agent.*;
import com.oenexa.application.service.auth.*;
import com.oenexa.application.service.chat.*;
import com.oenexa.application.service.code.*;
import com.oenexa.application.service.knowledge.*;
import com.oenexa.application.service.memory.*;
import com.oenexa.application.service.orchestration.*;

import com.oenexa.domain.model.chat.ChatDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OenexaAutonomousAgentEngine Zero-Mock Unit Tests")
class OenexaAutonomousAgentEngineTest {

    private CollaborativeAgentService agentService;
    private RepositoryKnowledgeService repositoryKnowledgeService;
    private MemoryService memoryService;
    private OenexaAutonomousAgentEngine engine;

    @BeforeEach
    void setUp() {
        agentService = new CollaborativeAgentService();
        repositoryKnowledgeService = new RepositoryKnowledgeService();
        memoryService = new MemoryService();
        engine = new OenexaAutonomousAgentEngine(agentService, repositoryKnowledgeService, memoryService);
    }

    @Test
    @DisplayName("Should autonomously execute standard engineering goal with self-healing enabled")
    void shouldExecuteStandardGoalWithSelfHealing() {
        repositoryKnowledgeService.indexRepository("repo-alpha", Map.of(
                "src/Main.java", "public class Main { public static void main(String[] args) {} }"
        ));
        memoryService.learnFromUserTurn("user-1", "I prefer strict zero-mock testing.");

        OenexaAutonomousGoalRequest request = new OenexaAutonomousGoalRequest(
                "Build reactive event streaming gateway",
                "repo-alpha",
                "user-1",
                true,
                true,
                List.of()
        );

        OenexaAutonomousGoalResponse response = engine.executeGoal(request);

        assertNotNull(response);
        assertNotNull(response.goalId());
        assertEquals("Build reactive event streaming gateway", response.goalDescription());
        assertEquals(0.99, response.confidenceScore(), 0.001);
        assertNotNull(response.plan());
        assertEquals(OenexaExecutionPhase.COMPLETED, response.plan().currentPhase());
        assertEquals(7, response.plan().steps().size());
        assertFalse(response.events().isEmpty());
        assertFalse(response.selfHealingCycles().isEmpty());
        assertFalse(response.artifacts().isEmpty());
        assertNotNull(response.userStories());
        assertEquals(2, response.userStories().size());
        assertNotNull(response.prd());
        assertNotNull(response.privacyAudit());
        assertTrue(response.privacyAudit().localOnlyMode());
        assertTrue(response.privacyAudit().airGappedVerification());
        assertTrue(response.finalSummary().contains("OENEXA Neo-GPT Autonomous Goal Execution Succeeded"));

        // Verify retrieval queries
        Optional<OenexaAutonomousGoalResponse> retrieved = engine.getGoal(response.goalId());
        assertTrue(retrieved.isPresent());
        assertEquals(response.goalId(), retrieved.get().goalId());

        List<OenexaAutonomousGoalResponse> allGoals = engine.getAllGoals();
        assertEquals(1, allGoals.size());

        List<OenexaExecutionEvent> events = engine.getGoalEvents(response.goalId());
        assertEquals(response.events().size(), events.size());

        List<OenexaSelfHealingCycle> healingCycles = engine.getGoalHealingCycles(response.goalId());
        assertEquals(response.selfHealingCycles().size(), healingCycles.size());

        assertNotNull(engine.getAgentService());
        assertNotNull(engine.getRepositoryKnowledgeService());
        assertNotNull(engine.getMemoryService());
        assertTrue(engine.getExecutedGoal(response.goalId()).isPresent());
    }

    @Test
    @DisplayName("Should detect bug intent and apply deep self-healing cycle")
    void shouldExecuteBugFixGoalWithDeepSelfHealing() {
        OenexaAutonomousGoalRequest request = new OenexaAutonomousGoalRequest(
                "Fix compilation error and repair NPE in payment service",
                "repo-beta",
                "user-2",
                true,
                true,
                List.of()
        );

        OenexaAutonomousGoalResponse response = engine.executeGoal(request);

        assertNotNull(response);
        assertFalse(response.selfHealingCycles().isEmpty());
        OenexaSelfHealingCycle cycle = response.selfHealingCycles().get(0);
        assertTrue(cycle.issueDetected().contains("boundary regression"));
        assertTrue(cycle.rootCause().contains("Defensive non-null check missing"));
        assertTrue(cycle.patchDiff().contains("Objects.requireNonNull"));
        assertTrue(cycle.resolved());
    }

    @Test
    @DisplayName("Should execute goal with autonomousSelfHealing disabled and default values")
    void shouldExecuteGoalWithoutSelfHealing() {
        OenexaAutonomousGoalRequest request = new OenexaAutonomousGoalRequest(
                "Generate architecture diagrams",
                "",
                "",
                false,
                false,
                List.of()
        );

        OenexaAutonomousGoalResponse response = engine.executeGoal(request);

        assertNotNull(response);
        assertTrue(response.selfHealingCycles().isEmpty());
    }

    @Test
    @DisplayName("Should stream execution events in real time to consumer")
    void shouldStreamExecutionEvents() {
        OenexaAutonomousGoalRequest request = new OenexaAutonomousGoalRequest("Design microservices schema");
        List<OenexaExecutionEvent> streamedEvents = new ArrayList<>();

        OenexaAutonomousGoalResponse response = engine.streamGoalExecution(request, streamedEvents::add);

        assertNotNull(response);
        assertFalse(streamedEvents.isEmpty());
        assertEquals(response.events().size(), streamedEvents.size());

        assertEquals(OenexaExecutionPhase.PLANNING, streamedEvents.get(0).phase());
        assertTrue(streamedEvents.stream().anyMatch(e -> e.phase() == OenexaExecutionPhase.DECOMPOSITION));
        assertTrue(streamedEvents.stream().anyMatch(e -> e.phase() == OenexaExecutionPhase.TOOL_EXECUTION));
        assertTrue(streamedEvents.stream().anyMatch(e -> e.phase() == OenexaExecutionPhase.REFLECTION));
    }

    @Test
    @DisplayName("Should execute local-first goal with multimodal attachments")
    void shouldExecuteLocalFirstMultimodalGoal() {
        MultimodalAttachment att = new MultimodalAttachment(
                MultimodalType.IMAGE,
                "architecture_c4.png",
                "image/png",
                "base64-diagram-bytes",
                "C4 Container diagram showing React UI and Spring Boot API"
        );

        OenexaAutonomousGoalRequest request = new OenexaAutonomousGoalRequest(
                "Implement Hexagonal Architecture from Diagram",
                "repo-gamma",
                "user-3",
                true,
                true,
                List.of(),
                true,
                List.of(att)
        );

        OenexaAutonomousGoalResponse response = engine.executeGoal(request);

        assertNotNull(response);
        assertTrue(response.privacyAudit().localOnlyMode());
        assertTrue(response.privacyAudit().airGappedVerification());
        assertEquals("in-process-native-neural-engine", response.privacyAudit().localModelEndpoint());

        boolean hasLocalPrivacyEvent = response.events().stream()
                .anyMatch(e -> e.toolAction().equals("VERIFY_LOCAL_PRIVACY"));
        assertTrue(hasLocalPrivacyEvent);

        boolean hasMultimodalEvent = response.events().stream()
                .anyMatch(e -> e.toolAction().equals("PROCESS_MULTIMODAL"));
        assertTrue(hasMultimodalEvent);

        // Verify PM PRD markdown artifact generated
        boolean hasPrdArtifact = response.artifacts().stream()
                .anyMatch(a -> a.language().equals("MARKDOWN") && a.filePath().contains("PRD_"));
        assertTrue(hasPrdArtifact);
    }

    @Test
    @DisplayName("Should trigger on-demand manual self-healing cycle")
    void shouldTriggerManualSelfHealing() {
        String goalId = "custom-goal-123";
        OenexaSelfHealingCycle cycle = engine.triggerSelfHealing(
                goalId,
                "Simulated Test Assertion Failure: expected 200 OK got 500",
                "Missing null check on request headers"
        );

        assertNotNull(cycle);
        assertEquals(goalId, cycle.goalId());
        assertEquals("Simulated Test Assertion Failure: expected 200 OK got 500", cycle.issueDetected());
        assertEquals("Missing null check on request headers", cycle.rootCause());
        assertTrue(cycle.resolved());

        List<OenexaSelfHealingCycle> cycles = engine.getGoalHealingCycles(goalId);
        assertEquals(1, cycles.size());

        List<OenexaExecutionEvent> events = engine.getGoalEvents(goalId);
        assertEquals(1, events.size());
        assertEquals(OenexaExecutionPhase.SELF_HEALING_VERIFICATION, events.get(0).phase());

        // Trigger with null rootCause
        OenexaSelfHealingCycle cycle2 = engine.triggerSelfHealing(goalId, "Another issue", null);
        assertNotNull(cycle2);
        assertTrue(cycle2.rootCause().contains("Identified runtime or compilation discrepancy"));
    }

    @Test
    @DisplayName("Should enforce defensive null-safety invariants")
    void shouldEnforceInvariants() {
        assertThrows(NullPointerException.class, () -> new OenexaAutonomousAgentEngine(null, repositoryKnowledgeService, memoryService));
        assertThrows(NullPointerException.class, () -> new OenexaAutonomousAgentEngine(agentService, null, memoryService));
        assertThrows(NullPointerException.class, () -> new OenexaAutonomousAgentEngine(agentService, repositoryKnowledgeService, null));
        assertThrows(NullPointerException.class, () -> engine.executeGoal(null));
        assertThrows(NullPointerException.class, () -> engine.executeGoal(new OenexaAutonomousGoalRequest(null)));
        assertThrows(NullPointerException.class, () -> engine.streamGoalExecution(null, e -> {}));
        assertThrows(NullPointerException.class, () -> engine.streamGoalExecution(new OenexaAutonomousGoalRequest("goal"), null));
        assertThrows(NullPointerException.class, () -> engine.triggerSelfHealing(null, "issue", "cause"));
        assertThrows(NullPointerException.class, () -> engine.triggerSelfHealing("g1", null, "cause"));
    }
}


