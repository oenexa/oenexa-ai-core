package com.oenexa.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Autonomous Agent & Multimodal Domain Models Zero-Mock Tests")
class AutonomousDomainModelTest {

    @Test
    void testPhase2AndPhase3DomainModels() {
        assertEquals(11, ChatDomain.ModelProvider.values().length);
        assertEquals(ChatDomain.ModelProvider.LOCAL_OLLAMA, ChatDomain.ModelProvider.valueOf("LOCAL_OLLAMA"));
        assertEquals(ChatDomain.ModelProvider.LOCAL_PRIVATE, ChatDomain.ModelProvider.valueOf("LOCAL_PRIVATE"));
        assertEquals(ChatDomain.ModelProvider.QWEN, ChatDomain.ModelProvider.valueOf("QWEN"));
        assertEquals(ChatDomain.ModelProvider.OENEXA_GPT, ChatDomain.ModelProvider.valueOf("OENEXA_GPT"));

        assertEquals(9, ChatDomain.TaskType.values().length);
        assertEquals(ChatDomain.TaskType.PRODUCT_MANAGEMENT, ChatDomain.TaskType.valueOf("PRODUCT_MANAGEMENT"));
        assertEquals(ChatDomain.TaskType.MULTIMODAL_ANALYSIS, ChatDomain.TaskType.valueOf("MULTIMODAL_ANALYSIS"));

        assertEquals(9, ChatDomain.ConversationCategory.values().length);
        assertEquals(ChatDomain.ConversationCategory.PRODUCT_MANAGEMENT, ChatDomain.ConversationCategory.valueOf("PRODUCT_MANAGEMENT"));
        assertEquals(ChatDomain.ConversationCategory.MULTIMODAL, ChatDomain.ConversationCategory.valueOf("MULTIMODAL"));

        assertEquals(8, ChatDomain.SoftwareEngineerAgentType.values().length);
        assertEquals(ChatDomain.SoftwareEngineerAgentType.PRODUCT_MANAGER_AGENT, ChatDomain.SoftwareEngineerAgentType.valueOf("PRODUCT_MANAGER_AGENT"));

        ChatDomain.UserMemoryRecord memory = new ChatDomain.UserMemoryRecord("user-1", "preferred_style", "concise");
        assertEquals("user-1", memory.userId());
        assertEquals("preferred_style", memory.memoryKey());
        assertEquals("concise", memory.memoryValue());
        assertEquals("USER_PREFERENCE", memory.category());
        assertEquals(0.95, memory.confidence());

        Instant now = Instant.now();
        ChatDomain.UserMemoryRecord fullMemory = new ChatDomain.UserMemoryRecord(
                "mem-1", "user-1", "tenant-1", "key", "val", "CATEGORY", 0.99, now
        );
        assertEquals("mem-1", fullMemory.memoryId());
        assertEquals("tenant-1", fullMemory.tenantId());

        ChatDomain.ProjectKnowledgeChunk chunk = new ChatDomain.ProjectKnowledgeChunk("repo-1", "src/Main.java", "java", "public class Main {}");
        assertEquals("repo-1", chunk.repositoryId());
        assertEquals("src/Main.java", chunk.filePath());
        assertEquals("java", chunk.language());
        assertEquals(0, chunk.chunkIndex());
        assertEquals("public class Main {}", chunk.content());

        ChatDomain.ProjectKnowledgeChunk fullChunk = new ChatDomain.ProjectKnowledgeChunk(
                "c-1", "repo-1", "src/App.java", "java", 2, "class App {}", List.of(0.1, 0.2), Map.of("App", "class"), now
        );
        assertEquals("c-1", fullChunk.chunkId());
        assertEquals(2, fullChunk.chunkIndex());
        assertEquals(2, fullChunk.embedding().size());
        assertEquals("class", fullChunk.symbols().get("App"));

        ChatDomain.ModelRoutingDecision decision = new ChatDomain.ModelRoutingDecision(
                ChatDomain.ModelProvider.ANTHROPIC_CLAUDE, "claude-3-7-sonnet", ChatDomain.TaskType.CODING, "High coding fidelity"
        );
        assertEquals(ChatDomain.ModelProvider.ANTHROPIC_CLAUDE, decision.provider());
        assertEquals("claude-3-7-sonnet", decision.modelName());
        assertEquals(ChatDomain.TaskType.CODING, decision.taskType());

        ChatDomain.AgentCollaborationTask collab = new ChatDomain.AgentCollaborationTask(
                ChatDomain.SoftwareEngineerAgentType.BUG_FIX_AGENT, "Fix null pointer in service"
        );
        assertEquals(ChatDomain.SoftwareEngineerAgentType.BUG_FIX_AGENT, collab.agentType());
        assertEquals("Fix null pointer in service", collab.taskDescription());
        assertEquals("PENDING", collab.status());

        ChatDomain.AgentCollaborationTask fullCollab = new ChatDomain.AgentCollaborationTask(
                "t-1", ChatDomain.SoftwareEngineerAgentType.TEST_AGENT, "Run tests", "input", "output", "COMPLETED", now
        );
        assertEquals("t-1", fullCollab.taskId());
        assertEquals("COMPLETED", fullCollab.status());

        ChatDomain.RepositoryKnowledgeMap map = new ChatDomain.RepositoryKnowledgeMap("repo-1", "Java / Spring Boot");
        assertEquals("repo-1", map.repositoryId());
        assertEquals("Java / Spring Boot", map.primaryStack());
        assertEquals(1, map.detectedTechnologies().size());

        ChatDomain.RepositoryKnowledgeMap fullMap = new ChatDomain.RepositoryKnowledgeMap(
                "repo-2", "Polyglot", List.of("Java", "React", "Docker", "Terraform"), List.of("backend", "frontend"),
                List.of("src/", "package.json"), Map.of("Solution", "class"), 50
        );
        assertEquals(4, fullMap.detectedTechnologies().size());
        assertEquals(2, fullMap.modules().size());
        assertEquals(50, fullMap.vectorChunksCount());

        ChatDomain.IntelligentChatRequest req = new ChatDomain.IntelligentChatRequest("How to refactor?", "conv-1");
        assertEquals("How to refactor?", req.message());
        assertEquals("conv-1", req.conversationId());
        assertEquals("default-user", req.userId());
        assertTrue(req.includeThinking());

        ChatDomain.IntelligentChatRequest fullReq = new ChatDomain.IntelligentChatRequest(
                "Build API", "conv-2", "user-2", "repo-2", "claude-3-7-sonnet", true, List.of()
        );
        assertEquals("user-2", fullReq.userId());
        assertEquals("claude-3-7-sonnet", fullReq.preferredModel());

        ChatDomain.IntelligentChatResponse resp = new ChatDomain.IntelligentChatResponse(
                "Here is the API", "conv-1", "claude-3-7-sonnet", ChatDomain.ModelProvider.ANTHROPIC_CLAUDE,
                ChatDomain.TaskType.CODING, "<thought>Synthesize</thought>", "Summary",
                List.of("snippet-1"), List.of(memory), List.of(chunk), List.of(), List.of(),
                List.of("Add tests"), 0.98, 0.99
        );
        assertEquals("Here is the API", resp.reply());
        assertEquals("claude-3-7-sonnet", resp.modelUsed());
        assertEquals(0.98, resp.rankingScore());
        assertEquals(0.99, resp.confidenceScore());
        assertEquals(1, resp.relevantUserMemories().size());
        assertEquals(1, resp.repositoryCitations().size());
    }

    @Test
    void testLocalFirstMultimodalAndProductManagerModels() {
        assertEquals(3, ChatDomain.MultimodalType.values().length);
        assertEquals(ChatDomain.MultimodalType.IMAGE, ChatDomain.MultimodalType.valueOf("IMAGE"));
        assertEquals(ChatDomain.MultimodalType.AUDIO_VOICE, ChatDomain.MultimodalType.valueOf("AUDIO_VOICE"));
        assertEquals(ChatDomain.MultimodalType.FILE_DOCUMENT, ChatDomain.MultimodalType.valueOf("FILE_DOCUMENT"));

        ChatDomain.MultimodalAttachment att = new ChatDomain.MultimodalAttachment(
                ChatDomain.MultimodalType.IMAGE, "arch.png", "image/png", "base64data", "Extracted diagram OCR"
        );
        assertNotNull(att.id());
        assertEquals(ChatDomain.MultimodalType.IMAGE, att.type());
        assertEquals("arch.png", att.fileName());
        assertEquals("image/png", att.mimeType());
        assertEquals("base64data", att.base64DataOrUrl());
        assertEquals("Extracted diagram OCR", att.extractedTextOrTranscript());
        assertNotNull(att.uploadedAt());

        Instant now = Instant.now();
        ChatDomain.MultimodalAttachment fullAtt = new ChatDomain.MultimodalAttachment(
                "att-1", ChatDomain.MultimodalType.AUDIO_VOICE, "voice.wav", "audio/wav", "audiodata", "Deploy to prod", now
        );
        assertEquals("att-1", fullAtt.id());
        assertEquals(now, fullAtt.uploadedAt());

        ChatDomain.UserStory story = new ChatDomain.UserStory(
                "OAuth2 Login", "developer", "authenticate via GitHub", "access repositories securely",
                List.of("AC1: JWT issued", "AC2: Refresh token supported")
        );
        assertNotNull(story.id());
        assertEquals("OAuth2 Login", story.title());
        assertEquals("developer", story.asA());
        assertEquals("authenticate via GitHub", story.iWant());
        assertEquals("access repositories securely", story.soThat());
        assertEquals(2, story.acceptanceCriteria().size());
        assertEquals(3, story.storyPoints());
        assertEquals("BACKLOG", story.status());

        ChatDomain.UserStory fullStory = new ChatDomain.UserStory(
                "story-1", "Billing API", "finance user", "pay invoice", "keep service active",
                List.of("Stripe webhook received"), 5, "IN_PROGRESS"
        );
        assertEquals("story-1", fullStory.id());
        assertEquals(5, fullStory.storyPoints());
        assertEquals("IN_PROGRESS", fullStory.status());

        ChatDomain.ProductRequirementsDocument prd = new ChatDomain.ProductRequirementsDocument(
                "Autonomous AI Agent PRD", "Software Engineers & Architects",
                "Chatbots are static and lack autonomous context",
                List.of("Autonomous ReAct Loop", "Air-gapped privacy mode"),
                List.of(story)
        );
        assertNotNull(prd.prdId());
        assertEquals("Autonomous AI Agent PRD", prd.title());
        assertEquals("Software Engineers & Architects", prd.targetAudience());
        assertEquals("Chatbots are static and lack autonomous context", prd.problemStatement());
        assertEquals(2, prd.keyFeatures().size());
        assertEquals(1, prd.userStories().size());
        assertEquals(2, prd.technicalConstraints().size());
        assertEquals("v1.0.0 Sprint Delivery", prd.releaseRoadmap());
        assertNotNull(prd.createdAt());

        ChatDomain.ProductRequirementsDocument fullPrd = new ChatDomain.ProductRequirementsDocument(
                "prd-100", "Custom PRD", "Enterprises", "Problem", List.of("Feature"),
                List.of(fullStory), List.of("Constraint"), "Q3 2026", now
        );
        assertEquals("prd-100", fullPrd.prdId());
        assertEquals("Q3 2026", fullPrd.releaseRoadmap());
        assertEquals(now, fullPrd.createdAt());

        ChatDomain.LocalPrivacyConfig defaultPrivacy = new ChatDomain.LocalPrivacyConfig();
        assertTrue(defaultPrivacy.localOnlyMode());
        assertEquals("in-process-native-neural-engine", defaultPrivacy.localModelEndpoint());
        assertEquals("oenexa-neo-gpt-o3-max", defaultPrivacy.selectedLocalModel());
        assertEquals(8192, defaultPrivacy.maxVramAllocationMb());
        assertTrue(defaultPrivacy.airGappedVerification());

        ChatDomain.LocalPrivacyConfig customPrivacy = new ChatDomain.LocalPrivacyConfig(
                false, false, "http://remote:11434", "deepseek-r1:latest", 16384, false
        );
        assertFalse(customPrivacy.localOnlyMode());
        assertEquals("deepseek-r1:latest", customPrivacy.selectedLocalModel());

        ChatDomain.OenexaAutonomousGoalRequest req = new ChatDomain.OenexaAutonomousGoalRequest("Goal 1");
        assertTrue(req.localFirstMode());
        assertTrue(req.multimodalAttachments().isEmpty());

        ChatDomain.OenexaAutonomousGoalRequest reqWithAttachments = new ChatDomain.OenexaAutonomousGoalRequest(
                "Goal 2", "repo-1", "user-1", true, true, List.of(), true, List.of(att)
        );
        assertEquals(1, reqWithAttachments.multimodalAttachments().size());
        assertTrue(reqWithAttachments.localFirstMode());

        ChatDomain.OenexaAutonomousGoalResponse resp = new ChatDomain.OenexaAutonomousGoalResponse(
                "goal-1", "Goal 1", null, List.of(), List.of(), List.of(),
                List.of(story), prd, defaultPrivacy, 0.99, "Finished successfully", now
        );
        assertEquals(1, resp.userStories().size());
        assertEquals(prd, resp.prd());
        assertEquals(defaultPrivacy, resp.privacyAudit());
        assertEquals(0.99, resp.confidenceScore());
    }

    @Test
    void testOenexaAutonomousDomainModels() {
        assertEquals(7, ChatDomain.OenexaExecutionPhase.values().length);

        ChatDomain.OenexaPlanStep step = new ChatDomain.OenexaPlanStep(
                "step-1", ChatDomain.SoftwareEngineerAgentType.ARCHITECTURE_AGENT,
                "Design system", "architecture_diagram"
        );
        assertEquals("step-1", step.stepId());
        assertEquals(ChatDomain.SoftwareEngineerAgentType.ARCHITECTURE_AGENT, step.agentPersona());
        assertEquals("Design system", step.description());
        assertEquals("architecture_diagram", step.toolToInvoke());
        assertEquals("PENDING", step.status());
        assertEquals("", step.output());
        assertEquals(0, step.retryCount());

        ChatDomain.OenexaPlanStep fullStep = new ChatDomain.OenexaPlanStep(
                "step-2", ChatDomain.SoftwareEngineerAgentType.CODE_AGENT,
                "Implement service", "code_generator", "COMPLETED", "Service created", 1
        );
        assertEquals("COMPLETED", fullStep.status());
        assertEquals(1, fullStep.retryCount());

        Instant now = Instant.now();
        ChatDomain.OenexaExecutionEvent event = new ChatDomain.OenexaExecutionEvent(
                "goal-1", ChatDomain.OenexaExecutionPhase.PLANNING,
                ChatDomain.SoftwareEngineerAgentType.ARCHITECTURE_AGENT,
                "Decomposing goal into tasks", "goal_analyzer", "Created 6 steps"
        );
        assertNotNull(event.eventId());
        assertEquals("goal-1", event.goalId());
        assertEquals(ChatDomain.OenexaExecutionPhase.PLANNING, event.phase());
        assertEquals("SUCCESS", event.status());

        ChatDomain.OenexaExecutionEvent fullEvent = new ChatDomain.OenexaExecutionEvent(
                "evt-1", "goal-1", ChatDomain.OenexaExecutionPhase.TOOL_EXECUTION,
                ChatDomain.SoftwareEngineerAgentType.TEST_AGENT,
                "Executing test runner", "test_runner", "Tests passed", "SUCCESS", now
        );
        assertEquals("evt-1", fullEvent.eventId());
        assertEquals(now, fullEvent.timestamp());

        ChatDomain.OenexaSelfHealingCycle cycle = new ChatDomain.OenexaSelfHealingCycle(
                "goal-1", "NullPointerException in AuthFilter", "Unchecked header", "--- diff +++ patch", true
        );
        assertNotNull(cycle.cycleId());
        assertEquals("goal-1", cycle.goalId());
        assertTrue(cycle.resolved());

        ChatDomain.OenexaSelfHealingCycle fullCycle = new ChatDomain.OenexaSelfHealingCycle(
                "cyc-1", "goal-1", "SyntaxError", "Missing semicolon", "patch", true, now
        );
        assertEquals("cyc-1", fullCycle.cycleId());
        assertEquals(now, fullCycle.timestamp());

        ChatDomain.OenexaAgentPlan plan = new ChatDomain.OenexaAgentPlan("plan-1", "goal-1", "Upgrade Auth", List.of(step, fullStep));
        assertEquals("plan-1", plan.planId());
        assertEquals("goal-1", plan.goalId());
        assertEquals(ChatDomain.OenexaExecutionPhase.PLANNING, plan.currentPhase());
        assertEquals(2, plan.steps().size());

        ChatDomain.OenexaAgentPlan fullPlan = new ChatDomain.OenexaAgentPlan(
                "plan-2", "goal-1", "Upgrade Auth", List.of(fullStep), ChatDomain.OenexaExecutionPhase.COMPLETED, now, now
        );
        assertEquals(ChatDomain.OenexaExecutionPhase.COMPLETED, fullPlan.currentPhase());

        ChatDomain.OenexaAutonomousGoalRequest req = new ChatDomain.OenexaAutonomousGoalRequest("Build OAuth2 Microservice");
        assertEquals("Build OAuth2 Microservice", req.goal());
        assertEquals("default-repo", req.repositoryId());
        assertTrue(req.autonomousSelfHealing());
        assertTrue(req.extendedThinking());

        ChatDomain.OenexaAutonomousGoalRequest fullReq = new ChatDomain.OenexaAutonomousGoalRequest(
                "Build OAuth2", "repo-x", "user-x", true, false, List.of()
        );
        assertEquals("user-x", fullReq.userId());
        assertFalse(fullReq.extendedThinking());

        ChatDomain.OenexaAutonomousGoalResponse resp = new ChatDomain.OenexaAutonomousGoalResponse(
                "goal-1", "Build OAuth2", plan, List.of(event), List.of(cycle),
                List.of(new ChatDomain.CodeArtifact("Auth.java", "src/Auth.java", "public class Auth {}")),
                0.99, "Autonomous goal achieved with zero defects.", now
        );
        assertEquals("goal-1", resp.goalId());
        assertEquals(0.99, resp.confidenceScore());
        assertEquals(1, resp.events().size());
        assertEquals(1, resp.selfHealingCycles().size());
        assertEquals(1, resp.artifacts().size());
        assertNotNull(resp.completedAt());
    }
}
