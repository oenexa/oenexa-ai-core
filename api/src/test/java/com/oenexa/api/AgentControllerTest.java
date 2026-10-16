package com.oenexa.api;

import com.oenexa.application.AgentCoordinatorService;
import com.oenexa.domain.AgentContract.WorkflowRun;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AgentController Zero-Mock Unit Tests")
class AgentControllerTest {

    private final AgentCoordinatorService coordinator = new AgentCoordinatorService();
    private final AgentController controller = new AgentController(coordinator);

    @Test
    @DisplayName("Should trigger workflow run and return completed run")
    void shouldTriggerWorkflowRun() {
        AgentController.RunRequest request = new AgentController.RunRequest("PR_REVIEW", "Review auth patch");
        ResponseEntity<WorkflowRun> response = controller.triggerRun(request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("COMPLETED", response.getBody().status());
    }

    @Test
    @DisplayName("Should reject null workflow or null objective with 400 Bad Request")
    void shouldRejectInvalidTriggerRequest() {
        ResponseEntity<WorkflowRun> nullWf = controller.triggerRun(new AgentController.RunRequest(null, "objective"));
        assertEquals(HttpStatus.BAD_REQUEST, nullWf.getStatusCode());

        ResponseEntity<WorkflowRun> nullObj = controller.triggerRun(new AgentController.RunRequest("PR_REVIEW", null));
        assertEquals(HttpStatus.BAD_REQUEST, nullObj.getStatusCode());
    }

    @Test
    @DisplayName("Should reject unknown workflow name with 400 Bad Request")
    void shouldRejectUnknownWorkflow() {
        ResponseEntity<WorkflowRun> response = controller.triggerRun(new AgentController.RunRequest("UNKNOWN_PIPELINE", "objective"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Should get existing run and return 404 for missing run")
    void shouldGetRun() {
        WorkflowRun run = controller.triggerRun(new AgentController.RunRequest("REPOSITORY_ANALYSIS", "analyze")).getBody();
        assertNotNull(run);

        ResponseEntity<WorkflowRun> found = controller.getRun(run.id());
        assertEquals(HttpStatus.OK, found.getStatusCode());
        assertEquals(run.id(), found.getBody().id());

        ResponseEntity<WorkflowRun> notFound = controller.getRun("non-existent-run-id");
        assertEquals(HttpStatus.NOT_FOUND, notFound.getStatusCode());
    }

    @Test
    @DisplayName("Should list runs and list predefined workflows")
    void shouldListRunsAndWorkflows() {
        controller.triggerRun(new AgentController.RunRequest("INCIDENT_RESPONSE", "resolve alert"));

        ResponseEntity<List<WorkflowRun>> runs = controller.listRuns();
        assertEquals(HttpStatus.OK, runs.getStatusCode());
        assertFalse(runs.getBody().isEmpty());

        ResponseEntity<List<Map<String, Object>>> workflows = controller.listWorkflows();
        assertEquals(HttpStatus.OK, workflows.getStatusCode());
        assertEquals(4, workflows.getBody().size());
    }
}
