package com.oenexa.infrastructure.persistence.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Autonomous Agent JPA Entities Zero-Mock Invariant & Mutator Tests")
class AutonomousEntityModelTest {

    @Test
    @DisplayName("ProductRequirementsDocumentEntity getters and setters")
    void testProductRequirementsDocumentEntity() {
        ProductRequirementsDocumentEntity entity = new ProductRequirementsDocumentEntity(
                "prd-1", "AI Platform", "Engineers", "Chatbot lacks context",
                "[\"Autonomous DAG\"]", "[\"Hexagonal\"]", "v1.0.0"
        );
        ProductRequirementsDocumentEntity noArg = new ProductRequirementsDocumentEntity();
        assertNotNull(noArg);

        assertEquals("prd-1", entity.getId());
        assertEquals("AI Platform", entity.getTitle());
        assertEquals("Engineers", entity.getTargetAudience());
        assertEquals("Chatbot lacks context", entity.getProblemStatement());
        assertEquals("[\"Autonomous DAG\"]", entity.getKeyFeatures());
        assertEquals("[\"Hexagonal\"]", entity.getTechnicalConstraints());
        assertEquals("v1.0.0", entity.getReleaseRoadmap());
        assertNotNull(entity.getCreatedAt());

        entity.setId("prd-2");
        entity.setTitle("New PRD");
        entity.setTargetAudience("Architects");
        entity.setProblemStatement("New Problem");
        entity.setKeyFeatures("Features");
        entity.setTechnicalConstraints("Constraints");
        entity.setReleaseRoadmap("v2.0.0");
        Instant now = Instant.now();
        entity.setCreatedAt(now);

        assertEquals("prd-2", entity.getId());
        assertEquals("New PRD", entity.getTitle());
        assertEquals("Architects", entity.getTargetAudience());
        assertEquals("New Problem", entity.getProblemStatement());
        assertEquals("Features", entity.getKeyFeatures());
        assertEquals("Constraints", entity.getTechnicalConstraints());
        assertEquals("v2.0.0", entity.getReleaseRoadmap());
        assertEquals(now, entity.getCreatedAt());
    }

    @Test
    @DisplayName("UserStoryEntity getters and setters")
    void testUserStoryEntity() {
        UserStoryEntity entity = new UserStoryEntity(
                "story-1", "prd-1", "Authentication", "User", "Log in", "Access dashboard", "Given credentials, When valid, Then 200 OK", 5, "BACKLOG"
        );
        UserStoryEntity noArg = new UserStoryEntity();
        assertNotNull(noArg);

        assertEquals("story-1", entity.getId());
        assertEquals("prd-1", entity.getPrdId());
        assertEquals("Authentication", entity.getTitle());
        assertEquals("User", entity.getAsA());
        assertEquals("Log in", entity.getIWant());
        assertEquals("Access dashboard", entity.getSoThat());
        assertEquals("Given credentials, When valid, Then 200 OK", entity.getAcceptanceCriteria());
        assertEquals(5, entity.getStoryPoints());
        assertEquals("BACKLOG", entity.getStatus());
        assertNotNull(entity.getCreatedAt());

        entity.setId("story-2");
        entity.setPrdId("prd-2");
        entity.setTitle("Logout");
        entity.setAsA("Admin");
        entity.setIWant("Log out");
        entity.setSoThat("Invalidate session");
        entity.setAcceptanceCriteria("Session terminated");
        entity.setStoryPoints(2);
        entity.setStatus("DONE");
        Instant now = Instant.now();
        entity.setCreatedAt(now);

        assertEquals("story-2", entity.getId());
        assertEquals("prd-2", entity.getPrdId());
        assertEquals("Logout", entity.getTitle());
        assertEquals("Admin", entity.getAsA());
        assertEquals("Log out", entity.getIWant());
        assertEquals("Invalidate session", entity.getSoThat());
        assertEquals("Session terminated", entity.getAcceptanceCriteria());
        assertEquals(2, entity.getStoryPoints());
        assertEquals("DONE", entity.getStatus());
        assertEquals(now, entity.getCreatedAt());
    }

    @Test
    @DisplayName("MultimodalAttachmentEntity getters and setters")
    void testMultimodalAttachmentEntity() {
        MultimodalAttachmentEntity entity = new MultimodalAttachmentEntity(
                "att-1", "goal-1", "IMAGE", "diagram.png", "image/png", "base64data", "C4 Context"
        );
        MultimodalAttachmentEntity noArg = new MultimodalAttachmentEntity();
        assertNotNull(noArg);

        assertEquals("att-1", entity.getId());
        assertEquals("goal-1", entity.getGoalId());
        assertEquals("IMAGE", entity.getAttachmentType());
        assertEquals("diagram.png", entity.getFileName());
        assertEquals("image/png", entity.getMimeType());
        assertEquals("base64data", entity.getBase64DataOrUrl());
        assertEquals("C4 Context", entity.getExtractedTextOrTranscript());
        assertNotNull(entity.getUploadedAt());

        entity.setId("att-2");
        entity.setGoalId("goal-2");
        entity.setAttachmentType("AUDIO_VOICE");
        entity.setFileName("voice.wav");
        entity.setMimeType("audio/wav");
        entity.setBase64DataOrUrl("audiodata");
        entity.setExtractedTextOrTranscript("Voice transcript");
        Instant now = Instant.now();
        entity.setUploadedAt(now);

        assertEquals("att-2", entity.getId());
        assertEquals("goal-2", entity.getGoalId());
        assertEquals("AUDIO_VOICE", entity.getAttachmentType());
        assertEquals("voice.wav", entity.getFileName());
        assertEquals("audio/wav", entity.getMimeType());
        assertEquals("audiodata", entity.getBase64DataOrUrl());
        assertEquals("Voice transcript", entity.getExtractedTextOrTranscript());
        assertEquals(now, entity.getUploadedAt());
    }

    @Test
    @DisplayName("LocalAgentAuditLogEntity getters and setters")
    void testLocalAgentAuditLogEntity() {
        LocalAgentAuditLogEntity entity = new LocalAgentAuditLogEntity(
                "log-1", "goal-1", true, true, "http://localhost:11434", "llama3.3", 8192, true
        );
        LocalAgentAuditLogEntity noArg = new LocalAgentAuditLogEntity();
        assertNotNull(noArg);

        assertEquals("log-1", entity.getId());
        assertEquals("goal-1", entity.getGoalId());
        assertTrue(entity.isLocalOnlyMode());
        assertTrue(entity.isTelemetryDisabled());
        assertEquals("http://localhost:11434", entity.getLocalModelEndpoint());
        assertEquals("llama3.3", entity.getSelectedLocalModel());
        assertEquals(8192, entity.getMaxVramAllocationMb());
        assertTrue(entity.isAirGappedVerification());
        assertNotNull(entity.getCreatedAt());

        entity.setId("log-2");
        entity.setGoalId("goal-2");
        entity.setLocalOnlyMode(false);
        entity.setTelemetryDisabled(false);
        entity.setLocalModelEndpoint("http://remote:11434");
        entity.setSelectedLocalModel("deepseek-r1");
        entity.setMaxVramAllocationMb(16384);
        entity.setAirGappedVerification(false);
        Instant now = Instant.now();
        entity.setCreatedAt(now);

        assertEquals("log-2", entity.getId());
        assertEquals("goal-2", entity.getGoalId());
        assertFalse(entity.isLocalOnlyMode());
        assertFalse(entity.isTelemetryDisabled());
        assertEquals("http://remote:11434", entity.getLocalModelEndpoint());
        assertEquals("deepseek-r1", entity.getSelectedLocalModel());
        assertEquals(16384, entity.getMaxVramAllocationMb());
        assertFalse(entity.isAirGappedVerification());
        assertEquals(now, entity.getCreatedAt());
    }

    @Test
    @DisplayName("OenexaGoalEntity getters and setters")
    void testOenexaGoalEntity() {
        OenexaGoalEntity goal = new OenexaGoalEntity("goal-1", "user-1", "repo-1", "Build autonomous agent");
        OenexaGoalEntity noArg = new OenexaGoalEntity();
        assertNotNull(noArg);

        assertEquals("goal-1", goal.getId());
        assertEquals("user-1", goal.getUserId());
        assertEquals("repo-1", goal.getRepositoryId());
        assertEquals("Build autonomous agent", goal.getGoalDescription());
        assertEquals("PLANNING", goal.getCurrentPhase());
        assertTrue(goal.isAutonomousSelfHealing());
        assertTrue(goal.isExtendedThinking());
        assertEquals(1.0, goal.getConfidenceScore(), 0.001);
        assertNull(goal.getFinalSummary());
        assertEquals("RUNNING", goal.getStatus());
        assertNotNull(goal.getCreatedAt());
        assertNull(goal.getCompletedAt());

        goal.setId("goal-2");
        goal.setUserId("user-2");
        goal.setRepositoryId("repo-2");
        goal.setGoalDescription("Refactor database");
        goal.setCurrentPhase("COMPLETED");
        goal.setAutonomousSelfHealing(false);
        goal.setExtendedThinking(false);
        goal.setConfidenceScore(0.95);
        goal.setFinalSummary("Refactored successfully");
        goal.setStatus("SUCCESS");
        Instant now = Instant.now();
        goal.setCreatedAt(now);
        goal.setCompletedAt(now);

        assertEquals("goal-2", goal.getId());
        assertEquals("user-2", goal.getUserId());
        assertEquals("repo-2", goal.getRepositoryId());
        assertEquals("Refactor database", goal.getGoalDescription());
        assertEquals("COMPLETED", goal.getCurrentPhase());
        assertFalse(goal.isAutonomousSelfHealing());
        assertFalse(goal.isExtendedThinking());
        assertEquals(0.95, goal.getConfidenceScore(), 0.001);
        assertEquals("Refactored successfully", goal.getFinalSummary());
        assertEquals("SUCCESS", goal.getStatus());
        assertEquals(now, goal.getCreatedAt());
        assertEquals(now, goal.getCompletedAt());
    }

    @Test
    @DisplayName("OenexaPlanStepEntity getters and setters")
    void testOenexaPlanStepEntity() {
        OenexaPlanStepEntity step = new OenexaPlanStepEntity("step-1", "goal-1", 1, "ARCHITECTURE_AGENT", "Design DAG", "DESIGN_TOOL");
        OenexaPlanStepEntity noArg = new OenexaPlanStepEntity();
        assertNotNull(noArg);

        assertEquals("step-1", step.getId());
        assertEquals("goal-1", step.getGoalId());
        assertEquals(1, step.getStepIndex());
        assertEquals("ARCHITECTURE_AGENT", step.getAgentPersona());
        assertEquals("Design DAG", step.getDescription());
        assertEquals("DESIGN_TOOL", step.getToolToInvoke());
        assertEquals("PENDING", step.getStatus());
        assertNull(step.getOutput());
        assertEquals(0, step.getRetryCount());
        assertNotNull(step.getCreatedAt());

        step.setId("step-2");
        step.setGoalId("goal-2");
        step.setStepIndex(2);
        step.setAgentPersona("CODE_AGENT");
        step.setDescription("Write code");
        step.setToolToInvoke("CODE_TOOL");
        step.setStatus("COMPLETED");
        step.setOutput("Code generated");
        step.setRetryCount(1);
        Instant now = Instant.now();
        step.setCreatedAt(now);

        assertEquals("step-2", step.getId());
        assertEquals("goal-2", step.getGoalId());
        assertEquals(2, step.getStepIndex());
        assertEquals("CODE_AGENT", step.getAgentPersona());
        assertEquals("Write code", step.getDescription());
        assertEquals("CODE_TOOL", step.getToolToInvoke());
        assertEquals("COMPLETED", step.getStatus());
        assertEquals("Code generated", step.getOutput());
        assertEquals(1, step.getRetryCount());
        assertEquals(now, step.getCreatedAt());
    }

    @Test
    @DisplayName("OenexaExecutionEventEntity getters and setters")
    void testOenexaExecutionEventEntity() {
        OenexaExecutionEventEntity event = new OenexaExecutionEventEntity(
                "evt-1", "goal-1", "TOOL_EXECUTION", "CODE_AGENT",
                "Analyzing syntax error", "COMPILE_TEST", "Exit code 1", "SUCCESS"
        );
        OenexaExecutionEventEntity noArg = new OenexaExecutionEventEntity();
        assertNotNull(noArg);

        assertEquals("evt-1", event.getId());
        assertEquals("goal-1", event.getGoalId());
        assertEquals("TOOL_EXECUTION", event.getPhase());
        assertEquals("CODE_AGENT", event.getAgentPersona());
        assertEquals("Analyzing syntax error", event.getThoughtProcess());
        assertEquals("COMPILE_TEST", event.getToolAction());
        assertEquals("Exit code 1", event.getOutputSnippet());
        assertEquals("SUCCESS", event.getStatus());
        assertNotNull(event.getTimestamp());

        event.setId("evt-2");
        event.setGoalId("goal-2");
        event.setPhase("SELF_HEALING");
        event.setAgentPersona("BUG_FIX_AGENT");
        event.setThoughtProcess("Patch applied");
        event.setToolAction("APPLY_PATCH");
        event.setOutputSnippet("Fixed");
        event.setStatus("RESOLVED");
        Instant now = Instant.now();
        event.setTimestamp(now);

        assertEquals("evt-2", event.getId());
        assertEquals("goal-2", event.getGoalId());
        assertEquals("SELF_HEALING", event.getPhase());
        assertEquals("BUG_FIX_AGENT", event.getAgentPersona());
        assertEquals("Patch applied", event.getThoughtProcess());
        assertEquals("APPLY_PATCH", event.getToolAction());
        assertEquals("Fixed", event.getOutputSnippet());
        assertEquals("RESOLVED", event.getStatus());
        assertEquals(now, event.getTimestamp());
    }

    @Test
    @DisplayName("OenexaSelfHealingCycleEntity getters and setters")
    void testOenexaSelfHealingCycleEntity() {
        OenexaSelfHealingCycleEntity cycle = new OenexaSelfHealingCycleEntity(
                "cycle-1", "goal-1", "NullPointerException at line 42",
                "Unchecked optional dereference", "+ if (opt.isPresent()) ...", true
        );
        OenexaSelfHealingCycleEntity noArg = new OenexaSelfHealingCycleEntity();
        assertNotNull(noArg);

        assertEquals("cycle-1", cycle.getId());
        assertEquals("goal-1", cycle.getGoalId());
        assertEquals("NullPointerException at line 42", cycle.getIssueDetected());
        assertEquals("Unchecked optional dereference", cycle.getRootCause());
        assertEquals("+ if (opt.isPresent()) ...", cycle.getPatchDiff());
        assertTrue(cycle.isResolved());
        assertNotNull(cycle.getTimestamp());

        cycle.setId("cycle-2");
        cycle.setGoalId("goal-2");
        cycle.setIssueDetected("Compilation failure");
        cycle.setRootCause("Missing import");
        cycle.setPatchDiff("+ import java.util.List;");
        cycle.setResolved(false);
        Instant now = Instant.now();
        cycle.setTimestamp(now);

        assertEquals("cycle-2", cycle.getId());
        assertEquals("goal-2", cycle.getGoalId());
        assertEquals("Compilation failure", cycle.getIssueDetected());
        assertEquals("Missing import", cycle.getRootCause());
        assertEquals("+ import java.util.List;", cycle.getPatchDiff());
        assertFalse(cycle.isResolved());
        assertEquals(now, cycle.getTimestamp());
    }
}
