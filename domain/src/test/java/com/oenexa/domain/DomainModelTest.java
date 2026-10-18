package com.oenexa.domain;

import java.lang.reflect.Constructor;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DomainModelTest {

    @Test
    void testAgentContractRecordsAndImplementation() {
        AgentContract.Task task = new AgentContract.Task(AgentType.SECURITY, "scan codebase");
        assertEquals(AgentType.SECURITY, task.agent());
        assertEquals("scan codebase", task.objective());

        AgentContract.Delegation delegation = new AgentContract.Delegation(AgentType.BUG_FIX, "fix finding");
        assertEquals(AgentType.BUG_FIX, delegation.to());
        assertEquals("fix finding", delegation.objective());

        AgentContract.Result result = AgentContract.Result.of("Analysis completed");
        assertEquals("Analysis completed", result.summary());
        assertTrue(result.artifacts().isEmpty());
        assertTrue(result.delegation().isEmpty());

        AgentContract.Result customResult = new AgentContract.Result("Found issues",
                Map.of("report", "critical.md"), Optional.of(delegation));
        assertEquals("Found issues", customResult.summary());
        assertEquals("critical.md", customResult.artifacts().get("report"));
        assertTrue(customResult.delegation().isPresent());

        Instant now = Instant.now();
        AgentContract.Step step = new AgentContract.Step(AgentType.CODING, "write logic", "done", now, 1);
        assertEquals(AgentType.CODING, step.agent());
        assertEquals("write logic", step.objective());
        assertEquals("done", step.summary());
        assertEquals(now, step.at());
        assertEquals(1, step.depth());

        AgentContract.WorkflowRun run = new AgentContract.WorkflowRun("run-1", Workflow.PR_REVIEW, "COMPLETED",
                List.of(step), "report text", null, now, now);
        assertEquals("run-1", run.id());
        assertEquals(Workflow.PR_REVIEW, run.workflow());
        assertEquals("COMPLETED", run.status());
        assertEquals(1, run.steps().size());
        assertEquals("report text", run.report());
        assertNull(run.error());
        assertEquals(now, run.startedAt());
        assertEquals(now, run.finishedAt());

        AgentContract contract = new AgentContract() {
            @Override
            public AgentType type() {
                return AgentType.CODING;
            }

            @Override
            public Result execute(Task t, ExecutionContext context) {
                return Result.of("executed " + t.objective());
            }
        };
        assertEquals(AgentType.CODING, contract.type());
        assertEquals("executed do work", contract.execute(new AgentContract.Task(AgentType.CODING, "do work"),
                new ExecutionContext("ctx")).summary());
    }

    @Test
    void testAgentTypeEnum() {
        assertEquals(15, AgentType.values().length);
        assertEquals(AgentType.REPOSITORY, AgentType.valueOf("REPOSITORY"));
        assertEquals(AgentType.DEPLOYMENT, AgentType.valueOf("DEPLOYMENT"));
    }

    @Test
    void testAuthDomainRecordsAndInstantiation() {
        assertNotNull(new AuthDomain());

        AuthDomain.TokenPair tokenPair = new AuthDomain.TokenPair("acc", "ref", 3600L);
        assertEquals("acc", tokenPair.accessToken());
        assertEquals("ref", tokenPair.refreshToken());
        assertEquals(3600L, tokenPair.expiresInSeconds());

        Instant now = Instant.now();
        AuthDomain.AuthClaims claims = new AuthDomain.AuthClaims("u1", "user", "ADMIN", now, now.plusSeconds(3600));
        assertEquals("u1", claims.userId());
        assertEquals("user", claims.username());
        assertEquals("ADMIN", claims.role());
        assertEquals(now, claims.issuedAt());
        assertNotNull(claims.expiresAt());

        AuthDomain.LoginRequest login = new AuthDomain.LoginRequest("admin", "secret");
        assertEquals("admin", login.usernameOrEmail());
        assertEquals("secret", login.password());

        AuthDomain.RegisterRequest register = new AuthDomain.RegisterRequest("newuser", "new@test.com", "pass123456", "New User");
        assertEquals("newuser", register.username());
        assertEquals("new@test.com", register.email());
        assertEquals("pass123456", register.password());
        assertEquals("New User", register.fullName());

        AuthDomain.RefreshRequest refresh = new AuthDomain.RefreshRequest("refresh_token_123");
        assertEquals("refresh_token_123", refresh.refreshToken());
    }

    @Test
    void testChatDomainRecordsAndInstantiation() {
        assertNotNull(new ChatDomain());
        assertEquals(4, ChatDomain.Role.values().length);
        assertEquals(ChatDomain.Role.TOOL, ChatDomain.Role.valueOf("TOOL"));

        Instant now = Instant.now();
        ChatDomain.Message msg = new ChatDomain.Message(ChatDomain.Role.USER, "Hello", now);
        assertEquals(ChatDomain.Role.USER, msg.role());
        assertEquals("Hello", msg.content());
        assertEquals(now, msg.timestamp());
        assertNull(msg.thoughtProcess());
        assertEquals("gemini-2.5-flash", msg.model());
        assertTrue(msg.attachments().isEmpty());
        assertTrue(msg.toolCalls().isEmpty());
        assertTrue(msg.toolResults().isEmpty());
        assertTrue(msg.artifacts().isEmpty());

        ChatDomain.Message advancedMsg = new ChatDomain.Message(
                ChatDomain.Role.ASSISTANT,
                "Answer",
                now,
                "Thought step",
                "gemini-2.5-pro"
        );
        assertEquals("Thought step", advancedMsg.thoughtProcess());
        assertEquals("gemini-2.5-pro", advancedMsg.model());

        ChatDomain.Conversation conv = new ChatDomain.Conversation("c1", "u1", List.of(msg));
        assertEquals("c1", conv.conversationId());
        assertEquals("u1", conv.userId());
        assertEquals(1, conv.messages().size());

        ChatDomain.ChatRequest req = new ChatDomain.ChatRequest("query", "c1");
        assertEquals("query", req.message());
        assertEquals("c1", req.conversationId());
        assertEquals("gemini-2.5-flash", req.model());
        assertTrue(req.includeThinking());
        assertTrue(req.attachments().isEmpty());

        ChatDomain.ChatRequest advancedReq = new ChatDomain.ChatRequest("query", "c1", "gemini-2.5-pro", false);
        assertEquals("gemini-2.5-pro", advancedReq.model());
        assertFalse(advancedReq.includeThinking());

        ChatDomain.ChatResponse resp = new ChatDomain.ChatResponse("reply", "c1", 42);
        assertEquals("reply", resp.reply());
        assertEquals("c1", resp.conversationId());
        assertEquals(42, resp.contextTokensUsed());
        assertNull(resp.thoughtProcess());
        assertEquals("gemini-2.5-flash", resp.model());
        assertEquals("GENERAL_ASSISTANT", resp.intent());
        assertTrue(resp.suggestedFollowUps().isEmpty());
        assertTrue(resp.toolCalls().isEmpty());
        assertTrue(resp.artifacts().isEmpty());
        assertEquals(0.95, resp.confidenceScore());

        ChatDomain.ChatResponse advancedResp = new ChatDomain.ChatResponse(
                "reply",
                "c1",
                42,
                "Thought",
                "gemini-2.5-pro",
                "ARCHITECTURE_DESIGN",
                List.of("Follow up 1")
        );
        assertEquals("Thought", advancedResp.thoughtProcess());
        assertEquals("gemini-2.5-pro", advancedResp.model());
        assertEquals("ARCHITECTURE_DESIGN", advancedResp.intent());
        assertEquals(1, advancedResp.suggestedFollowUps().size());

        // Pro Chat Additions: ToolCall, ToolResult, CodeContextAttachment, CodeArtifact, ConversationMetadata
        ChatDomain.ToolCall toolCall = new ChatDomain.ToolCall("call-1", "ast_analyzer");
        assertEquals("call-1", toolCall.callId());
        assertEquals("ast_analyzer", toolCall.toolName());
        assertTrue(toolCall.arguments().isEmpty());

        ChatDomain.ToolCall toolCallWithArgs = new ChatDomain.ToolCall("call-2", "security_scan", java.util.Map.of("target", "src/"));
        assertEquals("src/", toolCallWithArgs.arguments().get("target"));

        ChatDomain.ToolResult toolResult = new ChatDomain.ToolResult("call-1", "ast_analyzer", "OK");
        assertEquals("call-1", toolResult.callId());
        assertEquals("ast_analyzer", toolResult.toolName());
        assertEquals("OK", toolResult.output());
        assertFalse(toolResult.isError());

        ChatDomain.ToolResult toolResultError = new ChatDomain.ToolResult("call-2", "security_scan", "Failed", true);
        assertTrue(toolResultError.isError());

        ChatDomain.CodeContextAttachment attachment = new ChatDomain.CodeContextAttachment("src/Main.java", "public class Main {}");
        assertEquals("src/Main.java", attachment.filePath());
        assertEquals("public class Main {}", attachment.content());
        assertEquals(1, attachment.startLine());

        ChatDomain.CodeContextAttachment fullAttachment = new ChatDomain.CodeContextAttachment("src/App.java", "java", 10, 20, "void run() {}");
        assertEquals("java", fullAttachment.language());
        assertEquals(10, fullAttachment.startLine());
        assertEquals(20, fullAttachment.endLine());

        ChatDomain.CodeArtifact artifact = new ChatDomain.CodeArtifact("Main Class", "src/Main.java", "class Main {}");
        assertEquals("Main Class", artifact.title());
        assertEquals("src/Main.java", artifact.filePath());
        assertEquals("CREATE", artifact.action());
        assertNotNull(artifact.artifactId());

        ChatDomain.CodeArtifact customArtifact = new ChatDomain.CodeArtifact("a-1", "Diff", "src/App.java", "diff", "+ line", "PATCH");
        assertEquals("a-1", customArtifact.artifactId());
        assertEquals("PATCH", customArtifact.action());

        ChatDomain.ConversationMetadata meta = new ChatDomain.ConversationMetadata("c-100", "New Chat Session");
        assertEquals("c-100", meta.conversationId());
        assertEquals("New Chat Session", meta.title());
        assertFalse(meta.pinned());
        assertEquals(0, meta.messageCount());

        ChatDomain.ConversationMetadata fullMeta = new ChatDomain.ConversationMetadata(
                "c-200", "tenant-1", "user-1", "Architecture Review", "gemini-2.5-pro",
                now, now, 5, true
        );
        assertEquals("tenant-1", fullMeta.tenantId());
        assertEquals("user-1", fullMeta.userId());
        assertTrue(fullMeta.pinned());
        assertEquals(5, fullMeta.messageCount());

        ChatDomain.Message proMsg = new ChatDomain.Message(
                ChatDomain.Role.TOOL,
                "Tool output",
                now,
                "Thinking",
                "gemini-2.5-pro",
                List.of(attachment),
                List.of(toolCall),
                List.of(toolResult),
                List.of(artifact)
        );
        assertEquals(ChatDomain.Role.TOOL, proMsg.role());
        assertEquals(1, proMsg.attachments().size());
        assertEquals(1, proMsg.toolCalls().size());
        assertEquals(1, proMsg.toolResults().size());
        assertEquals(1, proMsg.artifacts().size());

        ChatDomain.ChatRequest proReq = new ChatDomain.ChatRequest("Analyze", "c-100", "gemini-2.5-pro", true, List.of(attachment));
        assertEquals(1, proReq.attachments().size());

        ChatDomain.ChatResponse proResp = new ChatDomain.ChatResponse(
                "Completed", "c-100", 150, "Thought", "gemini-2.5-pro", "CODE_GENERATION",
                List.of("Run tests"), List.of(toolCall), List.of(artifact), 0.98
        );
        assertEquals(0.98, proResp.confidenceScore());
        assertEquals(1, proResp.toolCalls().size());
        assertEquals(1, proResp.artifacts().size());
    }

    @Test
    void testPhase2AndPhase3DomainModels() {
        // Enums
        assertEquals(9, ChatDomain.ModelProvider.values().length);
        assertEquals(ChatDomain.ModelProvider.LOCAL_OLLAMA, ChatDomain.ModelProvider.valueOf("LOCAL_OLLAMA"));
        assertEquals(ChatDomain.ModelProvider.LOCAL_PRIVATE, ChatDomain.ModelProvider.valueOf("LOCAL_PRIVATE"));

        assertEquals(9, ChatDomain.TaskType.values().length);
        assertEquals(ChatDomain.TaskType.PRODUCT_MANAGEMENT, ChatDomain.TaskType.valueOf("PRODUCT_MANAGEMENT"));
        assertEquals(ChatDomain.TaskType.MULTIMODAL_ANALYSIS, ChatDomain.TaskType.valueOf("MULTIMODAL_ANALYSIS"));

        assertEquals(9, ChatDomain.ConversationCategory.values().length);
        assertEquals(ChatDomain.ConversationCategory.PRODUCT_MANAGEMENT, ChatDomain.ConversationCategory.valueOf("PRODUCT_MANAGEMENT"));
        assertEquals(ChatDomain.ConversationCategory.MULTIMODAL, ChatDomain.ConversationCategory.valueOf("MULTIMODAL"));

        assertEquals(8, ChatDomain.SoftwareEngineerAgentType.values().length);
        assertEquals(ChatDomain.SoftwareEngineerAgentType.PRODUCT_MANAGER_AGENT, ChatDomain.SoftwareEngineerAgentType.valueOf("PRODUCT_MANAGER_AGENT"));

        // UserMemoryRecord
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

        // ProjectKnowledgeChunk
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

        // ModelRoutingDecision
        ChatDomain.ModelRoutingDecision decision = new ChatDomain.ModelRoutingDecision(
                ChatDomain.ModelProvider.ANTHROPIC_CLAUDE, "claude-3-7-sonnet", ChatDomain.TaskType.CODING, "High coding fidelity"
        );
        assertEquals(ChatDomain.ModelProvider.ANTHROPIC_CLAUDE, decision.provider());
        assertEquals("claude-3-7-sonnet", decision.modelName());
        assertEquals(ChatDomain.TaskType.CODING, decision.taskType());

        // AgentCollaborationTask
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

        // RepositoryKnowledgeMap
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

        // IntelligentChatRequest & IntelligentChatResponse
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
        // MultimodalType
        assertEquals(3, ChatDomain.MultimodalType.values().length);
        assertEquals(ChatDomain.MultimodalType.IMAGE, ChatDomain.MultimodalType.valueOf("IMAGE"));
        assertEquals(ChatDomain.MultimodalType.AUDIO_VOICE, ChatDomain.MultimodalType.valueOf("AUDIO_VOICE"));
        assertEquals(ChatDomain.MultimodalType.FILE_DOCUMENT, ChatDomain.MultimodalType.valueOf("FILE_DOCUMENT"));

        // MultimodalAttachment
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

        // UserStory
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

        // ProductRequirementsDocument
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

        // LocalPrivacyConfig
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

        // Extended ArgonAutonomousGoalRequest
        ChatDomain.ArgonAutonomousGoalRequest req = new ChatDomain.ArgonAutonomousGoalRequest("Goal 1");
        assertTrue(req.localFirstMode());
        assertTrue(req.multimodalAttachments().isEmpty());

        ChatDomain.ArgonAutonomousGoalRequest reqWithAttachments = new ChatDomain.ArgonAutonomousGoalRequest(
                "Goal 2", "repo-1", "user-1", true, true, List.of(), true, List.of(att)
        );
        assertEquals(1, reqWithAttachments.multimodalAttachments().size());
        assertTrue(reqWithAttachments.localFirstMode());

        // Extended ArgonAutonomousGoalResponse
        ChatDomain.ArgonAutonomousGoalResponse resp = new ChatDomain.ArgonAutonomousGoalResponse(
                "goal-1", "Goal 1", null, List.of(), List.of(), List.of(),
                List.of(story), prd, defaultPrivacy, 0.99, "Finished successfully", now
        );
        assertEquals(1, resp.userStories().size());
        assertEquals(prd, resp.prd());
        assertEquals(defaultPrivacy, resp.privacyAudit());
        assertEquals(0.99, resp.confidenceScore());
    }

    @Test
    void testDomainEvent() {
        DomainEvent.StandardDomainEvent event = DomainEvent.StandardDomainEvent.of("USER_CREATED", "tenant-1", "payload-data");
        assertNotNull(event.eventId());
        assertEquals("USER_CREATED", event.type());
        assertEquals("tenant-1", event.tenantId());
        assertNotNull(event.occurredAt());
        assertEquals("payload-data", event.payload());

        DomainEvent custom = new DomainEvent() {
            private final UUID id = UUID.randomUUID();
            private final Instant time = Instant.now();

            @Override
            public UUID eventId() { return id; }
            @Override
            public String type() { return "CUSTOM"; }
            @Override
            public String tenantId() { return "tenant-x"; }
            @Override
            public Instant occurredAt() { return time; }
        };
        assertNotNull(custom.eventId());
        assertEquals("CUSTOM", custom.type());
        assertEquals("tenant-x", custom.tenantId());
        assertNotNull(custom.occurredAt());
    }

    @Test
    void testErrorCode() {
        assertEquals(400, ErrorCode.VALIDATION.httpStatus());
        assertEquals(401, ErrorCode.UNAUTHORIZED.httpStatus());
        assertEquals(403, ErrorCode.FORBIDDEN.httpStatus());
        assertEquals(404, ErrorCode.NOT_FOUND.httpStatus());
        assertEquals(409, ErrorCode.CONFLICT.httpStatus());
        assertEquals(429, ErrorCode.RATE_LIMITED.httpStatus());
        assertEquals(502, ErrorCode.UPSTREAM_FAILURE.httpStatus());
        assertEquals(500, ErrorCode.INTERNAL.httpStatus());
    }

    @Test
    void testOenexaException() {
        OenexaException ex = new OenexaException(ErrorCode.NOT_FOUND, "Entity missing");
        assertEquals(ErrorCode.NOT_FOUND, ex.code());
        assertEquals("Entity missing", ex.getMessage());

        Throwable cause = new IllegalArgumentException("invalid id");
        OenexaException exWithCause = new OenexaException(ErrorCode.VALIDATION, "Validation failed", cause);
        assertEquals(ErrorCode.VALIDATION, exWithCause.code());
        assertEquals("Validation failed", exWithCause.getMessage());
        assertEquals(cause, exWithCause.getCause());
    }

    @Test
    void testExecutionContext() {
        assertThrows(NullPointerException.class, () -> new ExecutionContext(null));

        ExecutionContext ctx = new ExecutionContext("root goal");
        assertEquals("root goal", ctx.objective());
        assertNull(ctx.get("missing"));

        ctx.put("k1", "v1");
        assertEquals("v1", ctx.get("k1"));
        assertEquals(Map.of("k1", "v1"), ctx.shared());

        // Null checks in put()
        ctx.put(null, "val");
        ctx.put("key", null);
        assertEquals(1, ctx.shared().size());
    }

    @Test
    void testPlannedModuleAndPlatformModule() {
        List<PlatformModule> modules = PlannedModule.allModules();
        assertEquals(20, modules.size());

        PlannedModule first = (PlannedModule) modules.get(0);
        assertEquals("auth", first.id());
        assertEquals("Authentication & RBAC", first.displayName());
        assertEquals("JWT issuing, validation, roles & permissions", first.description());
        assertEquals(List.of("login", "register", "token-refresh", "rbac"), first.capabilities());
        assertEquals(PlatformModule.ModuleStatus.ACTIVE, first.status());

        PlatformModule custom = new PlatformModule() {
            @Override
            public String id() { return "mod1"; }
            @Override
            public String displayName() { return "Mod One"; }
            @Override
            public String description() { return "Description"; }
            @Override
            public List<String> capabilities() { return List.of("cap1"); }
        };
        assertEquals(PlatformModule.ModuleStatus.ACTIVE, custom.status());
        PlatformModule.ModuleInfo info = custom.info();
        assertEquals("mod1", info.id());
        assertEquals("Mod One", info.displayName());
        assertEquals("Description", info.description());
        assertEquals(List.of("cap1"), info.capabilities());
        assertEquals(PlatformModule.ModuleStatus.ACTIVE, info.status());

        assertEquals(2, PlatformModule.ModuleStatus.values().length);
    }

    @Test
    void testRagDocumentRecordsAndInstantiation() {
        assertNotNull(new RagDocument());

        RagDocument.DocumentInput input = new RagDocument.DocumentInput("Doc", "Text", "repo-1");
        assertEquals("Doc", input.title());
        assertEquals("Text", input.content());
        assertEquals("repo-1", input.repositoryId());

        RagDocument.Chunk chunk = new RagDocument.Chunk("c-1", "Doc", "Snippet", 0);
        assertEquals("c-1", chunk.id());
        assertEquals("Doc", chunk.title());
        assertEquals("Snippet", chunk.text());
        assertEquals(0, chunk.chunkIndex());

        RagDocument.SearchResult search = new RagDocument.SearchResult("c-1", "Doc", "Snippet", 0.95);
        assertEquals("c-1", search.chunkId());
        assertEquals("Doc", search.title());
        assertEquals("Snippet", search.snippet());
        assertEquals(0.95, search.similarityScore());

        RagDocument.IngestResult ingest = new RagDocument.IngestResult(2, List.of("c-1", "c-2"));
        assertEquals(2, ingest.chunksIndexed());
        assertEquals(List.of("c-1", "c-2"), ingest.chunkIds());
    }

    @Test
    void testTenantContext() throws Exception {
        Constructor<TenantContext> constructor = TenantContext.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertNotNull(constructor.newInstance());

        TenantContext.clear();
        assertEquals("default", TenantContext.getTenantId());

        TenantContext.setTenantId("tenant-abc");
        assertEquals("tenant-abc", TenantContext.getTenantId());

        assertThrows(NullPointerException.class, () -> TenantContext.setTenantId(null));

        TenantContext.clear();
        assertEquals("default", TenantContext.getTenantId());
    }

    @Test
    void testWorkflowEnum() {
        assertEquals(4, Workflow.values().length);
        assertEquals(6, Workflow.REPOSITORY_ANALYSIS.steps().size());
        assertEquals(4, Workflow.PR_REVIEW.steps().size());
        assertEquals(4, Workflow.INCIDENT_RESPONSE.steps().size());
        assertEquals(7, Workflow.FEATURE_DELIVERY.steps().size());
    }

    @Test
    void testArgonAutonomousDomainModels() {
        assertEquals(7, ChatDomain.ArgonExecutionPhase.values().length);

        // ArgonPlanStep
        ChatDomain.ArgonPlanStep step = new ChatDomain.ArgonPlanStep(
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

        ChatDomain.ArgonPlanStep fullStep = new ChatDomain.ArgonPlanStep(
                "step-2", ChatDomain.SoftwareEngineerAgentType.CODE_AGENT,
                "Implement service", "code_generator", "COMPLETED", "Service created", 1
        );
        assertEquals("COMPLETED", fullStep.status());
        assertEquals(1, fullStep.retryCount());

        // ArgonExecutionEvent
        Instant now = Instant.now();
        ChatDomain.ArgonExecutionEvent event = new ChatDomain.ArgonExecutionEvent(
                "goal-1", ChatDomain.ArgonExecutionPhase.PLANNING,
                ChatDomain.SoftwareEngineerAgentType.ARCHITECTURE_AGENT,
                "Decomposing goal into tasks", "goal_analyzer", "Created 6 steps"
        );
        assertNotNull(event.eventId());
        assertEquals("goal-1", event.goalId());
        assertEquals(ChatDomain.ArgonExecutionPhase.PLANNING, event.phase());
        assertEquals("SUCCESS", event.status());

        ChatDomain.ArgonExecutionEvent fullEvent = new ChatDomain.ArgonExecutionEvent(
                "evt-1", "goal-1", ChatDomain.ArgonExecutionPhase.TOOL_EXECUTION,
                ChatDomain.SoftwareEngineerAgentType.TEST_AGENT,
                "Executing test runner", "test_runner", "Tests passed", "SUCCESS", now
        );
        assertEquals("evt-1", fullEvent.eventId());
        assertEquals(now, fullEvent.timestamp());

        // ArgonSelfHealingCycle
        ChatDomain.ArgonSelfHealingCycle cycle = new ChatDomain.ArgonSelfHealingCycle(
                "goal-1", "NullPointerException in AuthFilter", "Unchecked header", "--- diff +++ patch", true
        );
        assertNotNull(cycle.cycleId());
        assertEquals("goal-1", cycle.goalId());
        assertTrue(cycle.resolved());

        ChatDomain.ArgonSelfHealingCycle fullCycle = new ChatDomain.ArgonSelfHealingCycle(
                "cyc-1", "goal-1", "SyntaxError", "Missing semicolon", "patch", true, now
        );
        assertEquals("cyc-1", fullCycle.cycleId());
        assertEquals(now, fullCycle.timestamp());

        // ArgonAgentPlan
        ChatDomain.ArgonAgentPlan plan = new ChatDomain.ArgonAgentPlan("plan-1", "goal-1", "Upgrade Auth", List.of(step, fullStep));
        assertEquals("plan-1", plan.planId());
        assertEquals("goal-1", plan.goalId());
        assertEquals(ChatDomain.ArgonExecutionPhase.PLANNING, plan.currentPhase());
        assertEquals(2, plan.steps().size());

        ChatDomain.ArgonAgentPlan fullPlan = new ChatDomain.ArgonAgentPlan(
                "plan-2", "goal-1", "Upgrade Auth", List.of(fullStep), ChatDomain.ArgonExecutionPhase.COMPLETED, now, now
        );
        assertEquals(ChatDomain.ArgonExecutionPhase.COMPLETED, fullPlan.currentPhase());

        // ArgonAutonomousGoalRequest & ArgonAutonomousGoalResponse
        ChatDomain.ArgonAutonomousGoalRequest req = new ChatDomain.ArgonAutonomousGoalRequest("Build OAuth2 Microservice");
        assertEquals("Build OAuth2 Microservice", req.goal());
        assertEquals("default-repo", req.repositoryId());
        assertTrue(req.autonomousSelfHealing());
        assertTrue(req.extendedThinking());

        ChatDomain.ArgonAutonomousGoalRequest fullReq = new ChatDomain.ArgonAutonomousGoalRequest(
                "Build OAuth2", "repo-x", "user-x", true, false, List.of()
        );
        assertEquals("user-x", fullReq.userId());
        assertFalse(fullReq.extendedThinking());

        ChatDomain.ArgonAutonomousGoalResponse resp = new ChatDomain.ArgonAutonomousGoalResponse(
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

    @Test
    void testOenexaAutonomousDomainModels() {
        assertEquals(7, ChatDomain.OenexaExecutionPhase.values().length);

        // OenexaPlanStep
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

        // OenexaExecutionEvent
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

        // OenexaSelfHealingCycle
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

        // OenexaAgentPlan
        ChatDomain.OenexaAgentPlan plan = new ChatDomain.OenexaAgentPlan("plan-1", "goal-1", "Upgrade Auth", List.of(step, fullStep));
        assertEquals("plan-1", plan.planId());
        assertEquals("goal-1", plan.goalId());
        assertEquals(ChatDomain.OenexaExecutionPhase.PLANNING, plan.currentPhase());
        assertEquals(2, plan.steps().size());

        ChatDomain.OenexaAgentPlan fullPlan = new ChatDomain.OenexaAgentPlan(
                "plan-2", "goal-1", "Upgrade Auth", List.of(fullStep), ChatDomain.OenexaExecutionPhase.COMPLETED, now, now
        );
        assertEquals(ChatDomain.OenexaExecutionPhase.COMPLETED, fullPlan.currentPhase());

        // OenexaAutonomousGoalRequest & OenexaAutonomousGoalResponse
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
