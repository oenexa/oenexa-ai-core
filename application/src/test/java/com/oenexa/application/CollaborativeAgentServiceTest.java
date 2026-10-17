package com.oenexa.application;

import com.oenexa.domain.ChatDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CollaborativeAgentService Zero-Mock Unit Tests")
class CollaborativeAgentServiceTest {

    private final CollaborativeAgentService service = new CollaborativeAgentService();

    @ParameterizedTest(name = "Should execute task for agent: {0}")
    @EnumSource(SoftwareEngineerAgentType.class)
    void shouldExecuteIndividualAgentTasks(SoftwareEngineerAgentType agentType) {
        AgentCollaborationTask task = service.executeAgentTask(agentType, "Task for " + agentType, "sample payload");

        assertNotNull(task);
        assertNotNull(task.taskId());
        assertEquals(agentType, task.agentType());
        assertEquals("COMPLETED", task.status());
        assertFalse(task.outputResult().isBlank());
        assertNotNull(task.timestamp());
    }

    @Test
    @DisplayName("Should execute end-to-end collaborative multi-agent pipeline")
    void shouldExecuteCollaborativePipeline() {
        String pipelineId = "pipe-100";
        String goal = "Implement User Authentication Microservice with JWT and Spring Security";

        List<AgentCollaborationTask> tasks = service.runCollaborativePipeline(pipelineId, goal);
        assertEquals(6, tasks.size());

        assertEquals(SoftwareEngineerAgentType.ARCHITECTURE_AGENT, tasks.get(0).agentType());
        assertEquals(SoftwareEngineerAgentType.CODE_AGENT, tasks.get(1).agentType());
        assertEquals(SoftwareEngineerAgentType.TEST_AGENT, tasks.get(2).agentType());
        assertEquals(SoftwareEngineerAgentType.SECURITY_AGENT, tasks.get(3).agentType());
        assertEquals(SoftwareEngineerAgentType.DEVOPS_AGENT, tasks.get(4).agentType());
        assertEquals(SoftwareEngineerAgentType.DOCUMENTATION_AGENT, tasks.get(5).agentType());

        // Verify retrieval of pipeline
        List<AgentCollaborationTask> fetched = service.getPipelineTasks(pipelineId);
        assertEquals(6, fetched.size());

        // Unknown pipeline
        assertTrue(service.getPipelineTasks("unknown-pipeline").isEmpty());
    }

    @Test
    @DisplayName("Should generate pipeline ID when null is passed")
    void shouldGeneratePipelineIdWhenNull() {
        List<AgentCollaborationTask> tasks = service.runCollaborativePipeline(null, "Refactor Controller Layer");
        assertEquals(6, tasks.size());
    }

    @Test
    @DisplayName("Should enforce null safety checks")
    void shouldEnforceInvariants() {
        assertThrows(NullPointerException.class, () -> service.executeAgentTask(null, "desc", null));
        assertThrows(NullPointerException.class, () -> service.executeAgentTask(SoftwareEngineerAgentType.CODE_AGENT, null, null));
        assertThrows(NullPointerException.class, () -> service.runCollaborativePipeline("pipe", null));
    }
}
