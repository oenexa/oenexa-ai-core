package com.oenexa.application.engine;

import com.oenexa.domain.TargetCapabilitiesDomain.OperatorActionType;
import com.oenexa.domain.TargetCapabilitiesDomain.OperatorWorkflow;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OperatorWorkflowEngineTest {

    private final OperatorWorkflowEngine engine = new OperatorWorkflowEngine();

    @Test
    void shouldThrowExceptionWhenObjectiveIsNull() {
        assertThrows(NullPointerException.class, () -> engine.executeOperatorWorkflow(null, null));
    }

    @Test
    void shouldExecuteDefaultOperatorWorkflowWhenActionsNullOrEmpty() {
        OperatorWorkflow wfNull = engine.executeOperatorWorkflow("Deploy Cluster", null);
        assertNotNull(wfNull);
        assertEquals(4, wfNull.actions().size());
        assertEquals("Deploy Cluster", wfNull.objective());
        assertEquals("COMPLETED", wfNull.status());
        assertNotNull(wfNull.workflowId());
        assertNotNull(wfNull.startedAt());
        assertNotNull(wfNull.completedAt());

        OperatorWorkflow wfEmpty = engine.executeOperatorWorkflow("Deploy Cluster", List.of());
        assertNotNull(wfEmpty);
        assertEquals(4, wfEmpty.actions().size());
    }

    @Test
    void shouldExecuteAllOperatorActionTypes() {
        List<OperatorActionType> allTypes = List.of(
                OperatorActionType.DOCKER_BUILD,
                OperatorActionType.DB_MIGRATION,
                OperatorActionType.TEST_VERIFICATION,
                OperatorActionType.K8S_DEPLOY,
                OperatorActionType.SAST_SECURITY_SCAN,
                OperatorActionType.CLI_COMMAND,
                OperatorActionType.API_CALL
        );

        OperatorWorkflow wf = engine.executeOperatorWorkflow("Comprehensive Enterprise Pipeline", allTypes);

        assertNotNull(wf);
        assertEquals(7, wf.actions().size());

        assertTrue(wf.actions().get(0).executionOutput().contains("Built multi-stage Docker image"));
        assertTrue(wf.actions().get(1).executionOutput().contains("Applied Flyway database migrations"));
        assertTrue(wf.actions().get(2).executionOutput().contains("Executed 100% Zero-Mock test suites"));
        assertTrue(wf.actions().get(3).executionOutput().contains("Applied Kubernetes manifests"));
        assertTrue(wf.actions().get(4).executionOutput().contains("SAST scan passed"));
        assertTrue(wf.actions().get(5).executionOutput().contains("CLI operation executed"));
        assertTrue(wf.actions().get(6).executionOutput().contains("REST API health check probe"));

        for (int i = 0; i < 7; i++) {
            assertEquals(allTypes.get(i), wf.actions().get(i).actionType());
            assertEquals("SUCCESS", wf.actions().get(i).resultStatus());
            assertNotNull(wf.actions().get(i).actionId());
            assertNotNull(wf.actions().get(i).timestamp());
        }
    }
}
