package com.oenexa.application.service.agent;

import com.oenexa.application.port.*;
import com.oenexa.application.service.agent.*;
import com.oenexa.application.service.auth.*;
import com.oenexa.application.service.chat.*;
import com.oenexa.application.service.code.*;
import com.oenexa.application.service.knowledge.*;
import com.oenexa.application.service.memory.*;
import com.oenexa.application.service.orchestration.*;

import com.oenexa.domain.model.agent.AgentContract.*;
import com.oenexa.domain.model.agent.AgentType;
import com.oenexa.domain.model.common.ExecutionContext;
import com.oenexa.domain.model.code.Workflow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AgentCoordinatorService Zero-Mock Unit Tests")
class AgentCoordinatorServiceTest {

    private final AgentCoordinatorService coordinator = new AgentCoordinatorService();

    @Test
    @DisplayName("Should execute workflow successfully across multiple agents")
    void shouldExecuteWorkflowSuccessfully() {
        WorkflowRun run = coordinator.executeWorkflow(Workflow.PR_REVIEW, "Review pull request #42");

        assertNotNull(run);
        assertNotNull(run.id());
        assertEquals("COMPLETED", run.status());
        assertNull(run.error());
        assertEquals(4, run.steps().size());
        assertTrue(run.report().contains("### Agent: REPOSITORY"));
        assertTrue(run.report().contains("### Agent: REVIEW"));
        assertTrue(run.report().contains("### Agent: SECURITY"));
        assertTrue(run.report().contains("### Agent: TESTING"));

        // Verify retrieval by ID
        Optional<WorkflowRun> retrieved = coordinator.getRun(run.id());
        assertTrue(retrieved.isPresent());
        assertEquals(run.id(), retrieved.get().id());

        // Verify listing
        List<WorkflowRun> allRuns = coordinator.listRuns();
        assertTrue(allRuns.stream().anyMatch(r -> r.id().equals(run.id())));
    }

    @Test
    @DisplayName("Should trigger automatic delegation when SECURITY agent detects vulnerability")
    void shouldDelegateToBugFixWhenVulnerabilityDetected() {
        WorkflowRun run = coordinator.executeWorkflow(Workflow.REPOSITORY_ANALYSIS, "Analyze CVE-2026-9999 vulnerability in auth module");

        assertEquals("COMPLETED", run.status());
        assertTrue(run.steps().stream().anyMatch(s -> s.agent() == AgentType.SECURITY));
        assertTrue(run.steps().stream().anyMatch(s -> s.agent() == AgentType.BUG_FIX && s.depth() == 1));
    }

    @Test
    @DisplayName("Should execute all 4 predefined workflows successfully")
    void shouldExecuteAllPredefinedWorkflows() {
        for (Workflow wf : Workflow.values()) {
            WorkflowRun run = coordinator.executeWorkflow(wf, "Execute standard workflow " + wf.name());
            assertEquals("COMPLETED", run.status());
            assertFalse(run.steps().isEmpty());
        }
    }

    @Test
    @DisplayName("Should record failed workflow run when an exception occurs")
    void shouldHandleWorkflowExecutionFailure() {
        WorkflowRun failedRun = coordinator.executeWorkflow(Workflow.PR_REVIEW, "FAIL_SIMULATION");

        assertNotNull(failedRun);
        assertEquals("FAILED", failedRun.status());
        assertEquals("Simulated execution failure", failedRun.error());
        assertEquals("", failedRun.report());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when delegation depth exceeds maximum")
    void shouldThrowWhenMaxDelegationDepthExceeded() {
        ExecutionContext ctx = new ExecutionContext("Test deep delegation");
        List<Step> steps = new ArrayList<>();

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> coordinator.executeStep(AgentType.CODING, "Deep delegation", ctx, steps, 4));
        assertTrue(ex.getMessage().contains("Max delegation depth exceeded"));
    }

    @Test
    @DisplayName("Should reject null workflow or null objective")
    void shouldRejectNullWorkflowOrObjective() {
        assertThrows(NullPointerException.class, () -> coordinator.executeWorkflow(null, "Test"));
        assertThrows(NullPointerException.class, () -> coordinator.executeWorkflow(Workflow.FEATURE_DELIVERY, null));
    }
}


