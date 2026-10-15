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
        assertEquals(3, ChatDomain.Role.values().length);

        Instant now = Instant.now();
        ChatDomain.Message msg = new ChatDomain.Message(ChatDomain.Role.USER, "Hello", now);
        assertEquals(ChatDomain.Role.USER, msg.role());
        assertEquals("Hello", msg.content());
        assertEquals(now, msg.timestamp());

        ChatDomain.Conversation conv = new ChatDomain.Conversation("c1", "u1", List.of(msg));
        assertEquals("c1", conv.conversationId());
        assertEquals("u1", conv.userId());
        assertEquals(1, conv.messages().size());

        ChatDomain.ChatRequest req = new ChatDomain.ChatRequest("query", "c1");
        assertEquals("query", req.message());
        assertEquals("c1", req.conversationId());

        ChatDomain.ChatResponse resp = new ChatDomain.ChatResponse("reply", "c1", 42);
        assertEquals("reply", resp.reply());
        assertEquals("c1", resp.conversationId());
        assertEquals(42, resp.contextTokensUsed());
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
}
