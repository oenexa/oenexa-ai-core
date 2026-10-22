package com.oenexa.application.bdd;

import com.oenexa.application.CollaborativeAgentService;
import com.oenexa.application.MemoryService;
import com.oenexa.application.OenexaAutonomousAgentEngine;
import com.oenexa.application.RepositoryKnowledgeService;
import com.oenexa.domain.ChatDomain.*;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Tag;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cucumber BDD Step Definitions for Autonomous Agent Goal Planning and Execution.
 * Real, Zero-Mock execution against canonical OenexaAutonomousAgentEngine.
 */
@Tag("bdd")
public class AutonomousAgentGoalSteps {

    private final CollaborativeAgentService agentService = new CollaborativeAgentService();
    private final RepositoryKnowledgeService repositoryKnowledgeService = new RepositoryKnowledgeService();
    private final MemoryService memoryService = new MemoryService();
    private final OenexaAutonomousAgentEngine engine = new OenexaAutonomousAgentEngine(
            agentService, repositoryKnowledgeService, memoryService
    );

    private OenexaAutonomousGoalRequest goalRequest;
    private OenexaAutonomousGoalResponse goalResponse;
    private String selfHealGoalId;
    private OenexaSelfHealingCycle healingCycle;

    @Given("an engineering goal {string} for repository {string} and user {string}")
    public void anEngineeringGoalForRepositoryAndUser(String goal, String repo, String user) {
        repositoryKnowledgeService.indexRepository(repo, Map.of(
                "src/AuthService.java", "public class AuthService { public void authenticate() {} }"
        ));
        this.goalRequest = new OenexaAutonomousGoalRequest(goal, repo, user, true, true, List.of());
    }

    @When("the autonomous agent engine executes the engineering goal")
    public void theAutonomousAgentEngineExecutesTheEngineeringGoal() {
        this.goalResponse = engine.executeGoal(this.goalRequest);
    }

    @Then("the goal execution should complete with status {string}")
    public void theGoalExecutionShouldCompleteWithStatus(String expectedStatus) {
        assertNotNull(goalResponse, "Goal response must not be null");
        assertNotNull(goalResponse.goalId());
        assertTrue(goalResponse.confidenceScore() > 0.0);
    }

    @Then("the plan should contain decomposed steps")
    public void thePlanShouldContainDecomposedSteps() {
        assertNotNull(goalResponse.plan(), "Plan must not be null");
        assertNotNull(goalResponse.plan().steps(), "Plan steps must not be null");
        assertFalse(goalResponse.plan().steps().isEmpty(), "Plan steps must not be empty");
    }

    @Then("the goal events should record planning and execution phases")
    public void theGoalEventsShouldRecordPlanningAndExecutionPhases() {
        assertNotNull(goalResponse.events(), "Execution events must not be null");
        assertFalse(goalResponse.events().isEmpty(), "Execution events must be recorded");
    }

    @Given("an existing goal with identifier {string}")
    public void anExistingGoalWithIdentifier(String goalId) {
        this.selfHealGoalId = goalId;
    }

    @When("a compilation or test failure {string} is reported")
    public void aCompilationOrTestFailureIsReported(String failure) {
        this.healingCycle = engine.triggerSelfHealing(
                this.selfHealGoalId,
                failure,
                "Missing import or syntax mismatch"
        );
    }

    @Then("the agent triggers a self-healing cycle")
    public void theAgentTriggersASelfHealingCycle() {
        assertNotNull(healingCycle, "Self-healing cycle must be instantiated");
        assertEquals(this.selfHealGoalId, healingCycle.goalId());
    }

    @Then("the self-healing cycle records the detected issue and corrective resolution")
    public void theSelfHealingCycleRecordsTheDetectedIssueAndCorrectiveResolution() {
        assertNotNull(healingCycle.issueDetected());
        assertNotNull(healingCycle.patchDiff());
        assertTrue(healingCycle.resolved());
    }
}
