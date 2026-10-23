package com.oenexa.application.cognitive;

import com.oenexa.domain.model.cognitive.OenexaAutonomousProblemSolverDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OenexaAutonomousLifecycleOrchestrator Zero-Mock Tests")
class OenexaAutonomousLifecycleOrchestratorTest {

    private OenexaAutonomousLifecycleOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new OenexaAutonomousLifecycleOrchestrator();
    }

    @Test
    @DisplayName("Should throw NullPointerException when input is null")
    void testNullInput() {
        assertThrows(NullPointerException.class, () -> orchestrator.executeLifecycle(null, 0.90));
    }

    @Test
    @DisplayName("Should execute 7-stage lifecycle successfully for standard query")
    void testStandardQueryLifecycle() {
        AutonomousLifecycleResult result = orchestrator.executeLifecycle("How to scale distributed cache in Java?", 0.90);

        assertNotNull(result.lifecycleId());
        assertTrue(result.successful());
        assertEquals(DetectionCategory.QUERY, result.detect().category());
        assertTrue(result.analyze().knowledgeBaseHit());
        assertTrue(result.resolve().validated());
        assertNotNull(result.learn().learnedPatternId());
        assertFalse(result.escalate().escalated());
        assertFalse(result.selfUpgrade().upgradeTriggered());
        assertTrue(result.continuousImprovement().latencyMs() > 0);
        assertTrue(result.overallConfidence() >= 0.90);
    }

    @Test
    @DisplayName("Should detect error and track root cause with telemetry update")
    void testErrorLifecycle() {
        int initialErrorsPrevented = orchestrator.getRepeatedErrorsPrevented();

        AutonomousLifecycleResult result = orchestrator.executeLifecycle(
                "Encountered NullPointerException at line 102 in user auth service", 0.90
        );

        assertEquals(DetectionCategory.ERROR, result.detect().category());
        assertTrue(result.analyze().rootCause().contains("null verification"));
        assertTrue(result.resolve().validated());
        assertTrue(orchestrator.getRepeatedErrorsPrevented() > initialErrorsPrevented);
        assertTrue(orchestrator.getProcessedCount() >= 1);
        assertTrue(orchestrator.getAverageLatencyMs() > 0);
    }

    @Test
    @DisplayName("Should escalate intelligence and trigger self-upgrade on unknown scenario")
    void testUnknownScenarioEscalation() {
        AutonomousLifecycleResult result = orchestrator.executeLifecycle(
                "Unprecedented unknown scenario with novel quantum-resistant state consensus", 0.99
        );

        assertEquals(DetectionCategory.UNKNOWN_SCENARIO, result.detect().category());
        assertTrue(result.escalate().escalated());
        assertFalse(result.escalate().alternativeSolutions().isEmpty());
        assertTrue(result.selfUpgrade().upgradeTriggered());
        assertTrue(result.selfUpgrade().safelyDeployed());
        assertEquals(0.99, result.overallConfidence());
    }

    @Test
    @DisplayName("Should classify bug and feature request inputs accurately")
    void testBugAndFeatureCategorization() {
        AutonomousLifecycleResult bugResult = orchestrator.executeLifecycle("Fix the broken authentication session timeout", 0.90);
        assertEquals(DetectionCategory.BUG, bugResult.detect().category());

        AutonomousLifecycleResult featureResult = orchestrator.executeLifecycle("Implement new feature for OAuth2 token revocation", 0.90);
        assertEquals(DetectionCategory.FEATURE_REQUEST, featureResult.detect().category());
    }
}

