package com.oenexa.application.engine;

import com.oenexa.domain.TargetCapabilitiesDomain.DevinTaskExecutionPlan;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DevinExecutionEngineTest {

    private final DevinExecutionEngine engine = new DevinExecutionEngine();

    @Test
    void shouldThrowExceptionWhenConstructedWithNullCursorEngine() {
        assertThrows(NullPointerException.class, () -> new DevinExecutionEngine(null));
    }

    @Test
    void shouldThrowExceptionWhenGoalIsNull() {
        assertThrows(NullPointerException.class, () -> engine.executeDevinAutonomousTask(null, "repo", true));
    }

    @Test
    void shouldExecuteDevinAutonomousTaskWithDefaultRepository() {
        DevinTaskExecutionPlan plan = engine.executeDevinAutonomousTask("Refactor TokenService to Sealed Records", null, true);

        assertNotNull(plan);
        assertEquals("Refactor TokenService to Sealed Records", plan.goal());
        assertEquals("oenexa-monolith", plan.repositoryId());
        assertEquals(4, plan.stages().size());
        assertTrue(plan.buildPassed());
        assertTrue(plan.zeroMockVerificationPassed());
        assertNotNull(plan.appliedPatch());
        assertTrue(plan.finalSummary().contains("Devin-Level Autonomous Task Execution Report"));
        assertTrue(plan.finalSummary().contains("**Zero-Mock Policy Enforced**: true"));
        assertNotNull(plan.taskId());
        assertNotNull(plan.completedAt());
    }

    @Test
    void shouldExecuteDevinAutonomousTaskWithCustomRepositoryAndFalseZeroMock() {
        CursorCodingEngine customCursor = new CursorCodingEngine();
        DevinExecutionEngine customEngine = new DevinExecutionEngine(customCursor);

        DevinTaskExecutionPlan plan = customEngine.executeDevinAutonomousTask("Setup CI/CD Pipeline", "custom-repo-prod", false);

        assertNotNull(plan);
        assertEquals("Setup CI/CD Pipeline", plan.goal());
        assertEquals("custom-repo-prod", plan.repositoryId());
        assertTrue(plan.finalSummary().contains("**Zero-Mock Policy Enforced**: false"));
    }
}
