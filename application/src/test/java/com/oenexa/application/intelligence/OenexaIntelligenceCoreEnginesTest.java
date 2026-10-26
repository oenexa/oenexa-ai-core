package com.oenexa.application.intelligence;

import com.oenexa.domain.model.intelligence.OenexaIntelligenceDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OenexaIntelligenceCoreEngines 12-Engine Unit Tests")
class OenexaIntelligenceCoreEnginesTest {

    private OenexaIntelligenceCoreEngines.ContextEngine contextEngine;
    private OenexaIntelligenceCoreEngines.ReasoningEngine reasoningEngine;
    private OenexaIntelligenceCoreEngines.MemoryEngine memoryEngine;
    private OenexaIntelligenceCoreEngines.KnowledgeEngine knowledgeEngine;
    private OenexaIntelligenceCoreEngines.TaskEngine taskEngine;
    private OenexaIntelligenceCoreEngines.WorkflowEngine workflowEngine;
    private OenexaIntelligenceCoreEngines.ProjectEngine projectEngine;
    private OenexaIntelligenceCoreEngines.CommunicationEngine communicationEngine;
    private OenexaIntelligenceCoreEngines.SecurityEngine securityEngine;
    private OenexaIntelligenceCoreEngines.MonitoringEngine monitoringEngine;
    private OenexaIntelligenceCoreEngines.LearningEngine learningEngine;
    private OenexaIntelligenceCoreEngines.IntelligenceEngine intelligenceEngine;

    @BeforeEach
    void setUp() {
        contextEngine = new OenexaIntelligenceCoreEngines.ContextEngine();
        reasoningEngine = new OenexaIntelligenceCoreEngines.ReasoningEngine();
        memoryEngine = new OenexaIntelligenceCoreEngines.MemoryEngine();
        knowledgeEngine = new OenexaIntelligenceCoreEngines.KnowledgeEngine();
        taskEngine = new OenexaIntelligenceCoreEngines.TaskEngine();
        workflowEngine = new OenexaIntelligenceCoreEngines.WorkflowEngine();
        projectEngine = new OenexaIntelligenceCoreEngines.ProjectEngine();
        communicationEngine = new OenexaIntelligenceCoreEngines.CommunicationEngine();
        securityEngine = new OenexaIntelligenceCoreEngines.SecurityEngine();
        monitoringEngine = new OenexaIntelligenceCoreEngines.MonitoringEngine();
        learningEngine = new OenexaIntelligenceCoreEngines.LearningEngine();

        intelligenceEngine = new OenexaIntelligenceCoreEngines.IntelligenceEngine(
                contextEngine,
                reasoningEngine,
                memoryEngine,
                knowledgeEngine,
                taskEngine,
                workflowEngine,
                projectEngine,
                communicationEngine,
                securityEngine,
                monitoringEngine,
                learningEngine
        );
    }

    @Test
    @DisplayName("Should execute sovereign 7-step cycle: Understand -> Analyze -> Reason -> Validate -> Respond -> Learn -> Improve")
    void shouldExecute7StepCognitiveCycle() {
        OenexaPrompt prompt = new OenexaPrompt(
                UUID.randomUUID(),
                "engineer-sovereign",
                "oenexa-ai-core",
                "Fix NullPointerException in User registration domain logic",
                List.of("UserService.java"),
                "OENEXA-CNE-32B",
                null
        );

        OenexaIntelligenceResponse response = intelligenceEngine.process(prompt);

        assertNotNull(response);
        assertTrue(response.directAnswer().startsWith("Direct Answer:"));
        assertTrue(response.directAnswer().contains("preconditions"));
        assertEquals(4, response.reasoningTrace().analyticalSteps().size());
        assertTrue(response.validationReport().compliant());
        assertEquals(100, response.validationReport().invariantScore());
        assertEquals(3, response.actionableRecommendations().size());
        assertFalse(response.artifacts().isEmpty());
        assertEquals("DomainInvariantFix.java", response.artifacts().get(0).title());

        // Verify Memory Engine recorded interaction
        List<OenexaIntelligenceResponse> history = memoryEngine.retrieveHistory("engineer-sovereign", "oenexa-ai-core");
        assertEquals(1, history.size());

        // Verify Learning Engine recorded outcome
        assertEquals(1, learningEngine.getLearnedPatternsCount());

        // Verify Monitoring Engine tracked telemetry
        Map<String, Object> health = monitoringEngine.getSystemHealth();
        assertEquals("HEALTHY", health.get("status"));
        assertEquals("v3.0.0-SOVEREIGN", health.get("version"));
        assertEquals(1L, health.get("totalRequests"));
    }

    @Test
    @DisplayName("Should execute task via Task Engine and verify zero-mock status")
    void shouldExecuteTaskViaTaskEngine() {
        OenexaTaskExecution execution = intelligenceEngine.executeTask("OENEXA-TaskEngine-v1", "./gradlew test");

        assertNotNull(execution);
        assertTrue(execution.isSuccessful());
        assertEquals(0, execution.statusExitCode());
        assertTrue(execution.consoleOutput().contains("Zero-Mock Verified"));
    }

    @Test
    @DisplayName("Should list registered OENEXA workspaces")
    void shouldListRegisteredWorkspaces() {
        List<String> workspaces = projectEngine.listRegisteredWorkspaces();
        assertTrue(workspaces.contains("oenexa-ai-core"));
        assertTrue(workspaces.contains("oenexa-node"));
        assertTrue(workspaces.contains("oenexa-ai-web"));
    }
}
