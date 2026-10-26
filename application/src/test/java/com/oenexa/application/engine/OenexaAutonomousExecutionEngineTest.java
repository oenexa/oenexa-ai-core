package com.oenexa.application.engine;

import com.oenexa.domain.model.orchestration.TargetCapabilitiesDomain.AutonomousTaskExecutionPlan;
import com.oenexa.domain.model.orchestration.TargetCapabilitiesDomain.DevinTaskExecutionPlan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OenexaAutonomousExecutionEngine Zero-Mock Unit Tests")
class OenexaAutonomousExecutionEngineTest {

    private final OenexaAutonomousExecutionEngine engine = new OenexaAutonomousExecutionEngine();

    @Test
    @DisplayName("Should reject null goal")
    void shouldRejectNullGoal() {
        assertThrows(NullPointerException.class, () -> engine.executeAutonomousTask(null, "repo", true));
        assertThrows(NullPointerException.class, () -> engine.executeDevinAutonomousTask(null, "repo", true));
    }

    @Test
    @DisplayName("Should execute autonomous end-to-end task with zero-mock verification")
    void shouldExecuteAutonomousTask() {
        AutonomousTaskExecutionPlan plan = engine.executeAutonomousTask("Refactor domain models", "oenexa-core", true);
        assertNotNull(plan);
        assertEquals("Refactor domain models", plan.goal());
        assertEquals("oenexa-core", plan.repositoryId());
        assertEquals(4, plan.stages().size());
        assertTrue(plan.buildPassed());
        assertTrue(plan.zeroMockVerificationPassed());
        assertNotNull(plan.appliedPatch());
        assertTrue(plan.finalSummary().contains("OENEXA Autonomous Task Execution Report"));
    }

    @Test
    @DisplayName("Should provide backward-compatible DevinTaskExecutionPlan")
    void shouldProvideDevinTaskPlan() {
        DevinTaskExecutionPlan plan = engine.executeDevinAutonomousTask("Build microservice gateway", null, false);
        assertNotNull(plan);
        assertEquals("Build microservice gateway", plan.goal());
        assertEquals("oenexa-monolith", plan.repositoryId());
        assertEquals(4, plan.stages().size());
        assertTrue(plan.buildPassed());
    }
}
