package com.oenexa.infrastructure.persistence.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JPA Entities Zero-Mock Invariant & Mutator Tests")
class EntityModelTest {

    @Test
    @DisplayName("AgentTaskEntity getters and setters")
    void testAgentTaskEntity() {
        AgentTaskEntity task = new AgentTaskEntity("task-1", "CODING", "{\"goal\":\"build\"}");
        AgentTaskEntity noArg = new AgentTaskEntity();
        assertNotNull(noArg);

        assertEquals("task-1", task.getId());
        assertEquals("CODING", task.getAgentType());
        assertEquals("RUNNING", task.getStatus());
        assertEquals("{\"goal\":\"build\"}", task.getInputPayload());
        assertNotNull(task.getStartedAt());
        assertNull(task.getOutputPayload());
        assertNull(task.getErrorMessage());
        assertNull(task.getCompletedAt());

        task.setStatus("COMPLETED");
        task.setInputPayload("{\"goal\":\"updated\"}");
        task.setOutputPayload("{\"result\":\"ok\"}");
        task.setErrorMessage("none");
        Instant now = Instant.now();
        task.setCompletedAt(now);

        assertEquals("COMPLETED", task.getStatus());
        assertEquals("{\"goal\":\"updated\"}", task.getInputPayload());
        assertEquals("{\"result\":\"ok\"}", task.getOutputPayload());
        assertEquals("none", task.getErrorMessage());
        assertEquals(now, task.getCompletedAt());
    }

    @Test
    @DisplayName("DeploymentEntity getters and setters")
    void testDeploymentEntity() {
        DeploymentEntity entity = new DeploymentEntity("dep-1", "proj-1", "prod", "v1.0.0");
        DeploymentEntity noArg = new DeploymentEntity();
        assertNotNull(noArg);

        assertEquals("dep-1", entity.getId());
        assertEquals("proj-1", entity.getProjectId());
        assertEquals("prod", entity.getEnvironment());
        assertEquals("v1.0.0", entity.getVersion());
        assertEquals("DEPLOYED", entity.getStatus());
        assertNotNull(entity.getDeployedAt());

        entity.setStatus("SUCCESS");
        assertEquals("SUCCESS", entity.getStatus());

        DeploymentEntity defaultEnv = new DeploymentEntity("dep-2", "proj-1", null, "v1.0.1");
        assertEquals("PRODUCTION", defaultEnv.getEnvironment());
    }

    @Test
    @DisplayName("FixEntity getters and setters")
    void testFixEntity() {
        FixEntity fix = new FixEntity("fix-1", "iss-1", "--- a +++ b", "fix: resolve NPE");
        FixEntity noArg = new FixEntity();
        assertNotNull(noArg);

        assertEquals("fix-1", fix.getId());
        assertEquals("iss-1", fix.getIssueId());
        assertEquals("--- a +++ b", fix.getPatchDiff());
        assertEquals("fix: resolve NPE", fix.getCommitMessage());
        assertEquals("GENERATED", fix.getStatus());
        assertFalse(fix.isValidated());
        assertNotNull(fix.getCreatedAt());
        assertNotNull(fix.getUpdatedAt());

        fix.setStatus("APPLIED");
        fix.setPatchDiff("diff2");
        fix.setCommitMessage("msg2");
        fix.setValidated(true);

        assertEquals("APPLIED", fix.getStatus());
        assertEquals("diff2", fix.getPatchDiff());
        assertEquals("msg2", fix.getCommitMessage());
        assertTrue(fix.isValidated());
    }

    @Test
    @DisplayName("IssueEntity getters and setters")
    void testIssueEntity() {
        IssueEntity issue = new IssueEntity("iss-1", "repo-1", "scan-1", "NPE in controller", "HIGH", "src/Main.java", 42);
        IssueEntity noArg = new IssueEntity();
        assertNotNull(noArg);

        assertEquals("iss-1", issue.getId());
        assertEquals("repo-1", issue.getRepositoryId());
        assertEquals("scan-1", issue.getScanId());
        assertEquals("NPE in controller", issue.getTitle());
        assertEquals("HIGH", issue.getSeverity());
        assertEquals("src/Main.java", issue.getFilePath());
        assertEquals(42, issue.getLineNumber());
        assertEquals("OPEN", issue.getStatus());
        assertNotNull(issue.getCreatedAt());
        assertNotNull(issue.getUpdatedAt());

        issue.setTitle("Updated Title");
        issue.setDescription("Detailed description");
        issue.setSeverity("CRITICAL");
        issue.setStatus("CLOSED");
        issue.setRootCause("Missing null check");

        assertEquals("Updated Title", issue.getTitle());
        assertEquals("Detailed description", issue.getDescription());
        assertEquals("CRITICAL", issue.getSeverity());
        assertEquals("CLOSED", issue.getStatus());
        assertEquals("Missing null check", issue.getRootCause());

        IssueEntity defaultIssue = new IssueEntity("iss-2", "repo-1", "scan-1", "Warning", null, "src/Test.java", 10);
        assertEquals("MEDIUM", defaultIssue.getSeverity());
    }

    @Test
    @DisplayName("LogEntryEntity getters and setters")
    void testLogEntryEntity() {
        LogEntryEntity log = new LogEntryEntity("log-1", "repo-1", "scan-1", "ERROR", "Compiler", "Build failed");
        LogEntryEntity noArg = new LogEntryEntity();
        assertNotNull(noArg);

        assertEquals("log-1", log.getId());
        assertEquals("repo-1", log.getRepositoryId());
        assertEquals("scan-1", log.getScanId());
        assertEquals("ERROR", log.getLogLevel());
        assertEquals("Compiler", log.getSource());
        assertEquals("Build failed", log.getMessage());
        assertNull(log.getStackTrace());
        assertNotNull(log.getTimestamp());

        log.setStackTrace("java.lang.RuntimeException: boom");
        assertEquals("java.lang.RuntimeException: boom", log.getStackTrace());

        LogEntryEntity defaultLog = new LogEntryEntity("log-2", "repo-1", "scan-1", null, "Runner", "Job started");
        assertEquals("INFO", defaultLog.getLogLevel());
    }

    @Test
    @DisplayName("PermissionEntity getters and constructor")
    void testPermissionEntity() {
        PermissionEntity perm = new PermissionEntity("perm-1", "read:code", "repository", "read", "Allows reading source code");
        PermissionEntity noArg = new PermissionEntity();
        assertNotNull(noArg);

        assertEquals("perm-1", perm.getId());
        assertEquals("read:code", perm.getName());
        assertEquals("repository", perm.getResource());
        assertEquals("read", perm.getAction());
        assertEquals("Allows reading source code", perm.getDescription());
    }

    @Test
    @DisplayName("ProjectEntity getters and setters")
    void testProjectEntity() {
        ProjectEntity proj = new ProjectEntity("proj-1", "Core Platform", "core-platform", "Platform description", 100L);
        ProjectEntity noArg = new ProjectEntity();
        assertNotNull(noArg);

        assertEquals("proj-1", proj.getId());
        assertEquals("Core Platform", proj.getName());
        assertEquals("core-platform", proj.getSlug());
        assertEquals("Platform description", proj.getDescription());
        assertEquals(100L, proj.getOwnerId());
        assertNotNull(proj.getCreatedAt());
        assertNotNull(proj.getUpdatedAt());

        proj.setName("New Name");
        proj.setSlug("new-slug");
        proj.setDescription("New Desc");
        proj.setOwnerId(200L);
        Instant now = Instant.now();
        proj.setUpdatedAt(now);

        assertEquals("New Name", proj.getName());
        assertEquals("new-slug", proj.getSlug());
        assertEquals("New Desc", proj.getDescription());
        assertEquals(200L, proj.getOwnerId());
        assertEquals(now, proj.getUpdatedAt());
    }

    @Test
    @DisplayName("PullRequestEntity getters and setters")
    void testPullRequestEntity() {
        PullRequestEntity pr = new PullRequestEntity("pr-1", "repo-1", 42, "feat: add RAG", "feature/rag", "main");
        PullRequestEntity noArg = new PullRequestEntity();
        assertNotNull(noArg);

        assertEquals("pr-1", pr.getId());
        assertEquals("repo-1", pr.getRepositoryId());
        assertEquals(42, pr.getPrNumber());
        assertEquals("feat: add RAG", pr.getTitle());
        assertEquals("feature/rag", pr.getSourceBranch());
        assertEquals("main", pr.getTargetBranch());
        assertEquals("OPEN", pr.getStatus());
        assertNotNull(pr.getCreatedAt());
        assertNotNull(pr.getUpdatedAt());

        pr.setTitle("New PR Title");
        pr.setStatus("MERGED");

        assertEquals("New PR Title", pr.getTitle());
        assertEquals("MERGED", pr.getStatus());
    }

    @Test
    @DisplayName("RepositoryEntity getters and setters")
    void testRepositoryEntity() {
        RepositoryEntity repo = new RepositoryEntity("repo-1", "proj-1", "backend-svc", "https://git/backend.git", "GITHUB", "main");
        RepositoryEntity noArg = new RepositoryEntity();
        assertNotNull(noArg);

        assertEquals("repo-1", repo.getId());
        assertEquals("proj-1", repo.getProjectId());
        assertEquals("backend-svc", repo.getName());
        assertEquals("https://git/backend.git", repo.getCloneUrl());
        assertEquals("GITHUB", repo.getProvider());
        assertEquals("main", repo.getDefaultBranch());
        assertEquals("ACTIVE", repo.getStatus());
        assertNotNull(repo.getCreatedAt());
        assertNotNull(repo.getUpdatedAt());

        repo.setProjectId("proj-2");
        repo.setName("renamed");
        repo.setCloneUrl("https://git/renamed.git");
        repo.setProvider("GITLAB");
        repo.setDefaultBranch("master");
        repo.setStatus("ARCHIVED");
        Instant now = Instant.now();
        repo.setUpdatedAt(now);

        assertEquals("proj-2", repo.getProjectId());
        assertEquals("renamed", repo.getName());
        assertEquals("https://git/renamed.git", repo.getCloneUrl());
        assertEquals("GITLAB", repo.getProvider());
        assertEquals("master", repo.getDefaultBranch());
        assertEquals("ARCHIVED", repo.getStatus());
        assertEquals(now, repo.getUpdatedAt());

        RepositoryEntity defaultRepo = new RepositoryEntity("repo-2", "proj-1", "frontend-svc", "https://git/frontend.git", null, null);
        assertEquals("GITHUB", defaultRepo.getProvider());
        assertEquals("main", defaultRepo.getDefaultBranch());
    }

    @Test
    @DisplayName("ReviewEntity getters and setters")
    void testReviewEntity() {
        ReviewEntity review = new ReviewEntity("rev-1", "pr-1", "PR is clean", 95, 10);
        ReviewEntity noArg = new ReviewEntity();
        assertNotNull(noArg);

        assertEquals("rev-1", review.getId());
        assertEquals("pr-1", review.getPullRequestId());
        assertEquals("PR is clean", review.getSummary());
        assertEquals("AI_AGENT", review.getReviewerType());
        assertEquals(95, review.getQualityScore());
        assertEquals(10, review.getRiskScore());
        assertEquals("APPROVED", review.getStatus());
        assertNotNull(review.getCreatedAt());

        review.setStatus("REJECTED");
        assertEquals("REJECTED", review.getStatus());
    }

    @Test
    @DisplayName("RoleEntity getters and constructor")
    void testRoleEntity() {
        RoleEntity role = new RoleEntity("role-1", "DEVELOPER", "Standard developer role");
        RoleEntity noArg = new RoleEntity();
        assertNotNull(noArg);

        assertEquals("role-1", role.getId());
        assertEquals("DEVELOPER", role.getName());
        assertEquals("Standard developer role", role.getDescription());
        assertNotNull(role.getCreatedAt());
    }

    @Test
    @DisplayName("ScanEntity getters and setters")
    void testScanEntity() {
        ScanEntity scan = new ScanEntity("scan-1", "repo-1", "main", "abc1234", "SAST");
        ScanEntity noArg = new ScanEntity();
        assertNotNull(noArg);

        assertEquals("scan-1", scan.getId());
        assertEquals("repo-1", scan.getRepositoryId());
        assertEquals("main", scan.getBranch());
        assertEquals("abc1234", scan.getCommitHash());
        assertEquals("SAST", scan.getScanType());
        assertEquals("PENDING", scan.getStatus());
        assertEquals(0, scan.getFindingsCount());
        assertNotNull(scan.getStartedAt());
        assertNull(scan.getCompletedAt());

        scan.setStatus("COMPLETED");
        scan.setFindingsCount(3);
        Instant now = Instant.now();
        scan.setCompletedAt(now);

        assertEquals("COMPLETED", scan.getStatus());
        assertEquals(3, scan.getFindingsCount());
        assertEquals(now, scan.getCompletedAt());
    }

    @Test
    @DisplayName("SecurityFindingEntity getters and setters")
    void testSecurityFindingEntity() {
        SecurityFindingEntity sec = new SecurityFindingEntity("sec-1", "repo-1", "scan-1", "CVE-2026-001", "SQL_INJECTION", "CRITICAL", "Unsanitized parameter", "Use parameterized queries");
        SecurityFindingEntity noArg = new SecurityFindingEntity();
        assertNotNull(noArg);

        assertEquals("sec-1", sec.getId());
        assertEquals("repo-1", sec.getRepositoryId());
        assertEquals("scan-1", sec.getScanId());
        assertEquals("CVE-2026-001", sec.getCveId());
        assertEquals("SQL_INJECTION", sec.getVulnerabilityType());
        assertEquals("CRITICAL", sec.getSeverity());
        assertEquals("Unsanitized parameter", sec.getDescription());
        assertEquals("Use parameterized queries", sec.getRemediation());
        assertEquals("UNRESOLVED", sec.getStatus());
        assertNotNull(sec.getCreatedAt());

        sec.setStatus("RESOLVED");
        assertEquals("RESOLVED", sec.getStatus());

        SecurityFindingEntity defaultSec = new SecurityFindingEntity("sec-2", "repo-1", "scan-1", "CVE-2026-002", "XSS", null, "Reflected XSS", "Sanitize HTML");
        assertEquals("HIGH", defaultSec.getSeverity());
    }

    @Test
    @DisplayName("TestExecutionEntity getters and constructor")
    void testTestExecutionEntity() {
        BigDecimal cov = new BigDecimal("100.00");
        TestExecutionEntity test = new TestExecutionEntity("t-1", "repo-1", "JUNIT_5", 50, 50, 0, cov);
        TestExecutionEntity noArg = new TestExecutionEntity();
        assertNotNull(noArg);

        assertEquals("t-1", test.getId());
        assertEquals("repo-1", test.getRepositoryId());
        assertEquals("JUNIT_5", test.getTestFramework());
        assertEquals(50, test.getTestCount());
        assertEquals(50, test.getPassedCount());
        assertEquals(0, test.getFailedCount());
        assertEquals(cov, test.getCoveragePct());
        assertNotNull(test.getRunAt());

        TestExecutionEntity defaultTest = new TestExecutionEntity("t-2", "repo-1", null, 10, 10, 0, null);
        assertEquals("JUNIT5", defaultTest.getTestFramework());
        assertEquals(BigDecimal.ZERO, defaultTest.getCoveragePct());
    }

    @Test
    @DisplayName("ConversationSessionEntity getters, setters, and defaults")
    void testConversationSessionEntity() {
        ConversationSessionEntity session = new ConversationSessionEntity("sess-1", "user-1", "Build Auth Service", "CODE_ENGINEERING", "ANTHROPIC_CLAUDE");
        ConversationSessionEntity noArg = new ConversationSessionEntity();
        assertNotNull(noArg);

        assertEquals("sess-1", session.getId());
        assertEquals("user-1", session.getUserId());
        assertEquals("Build Auth Service", session.getTitle());
        assertEquals("CODE_ENGINEERING", session.getCategory());
        assertEquals("ANTHROPIC_CLAUDE", session.getModelProvider());
        assertEquals(0, session.getMessageCount());
        assertNull(session.getSummary());
        assertNotNull(session.getCreatedAt());
        assertNotNull(session.getUpdatedAt());

        session.setId("sess-updated");
        session.setUserId("user-2");
        session.setTitle("New Title");
        session.setCategory("ARCHITECTURE");
        session.setSummary("Architecture review summary");
        session.setModelProvider("GEMINI");
        session.setMessageCount(15);
        Instant now = Instant.now();
        session.setCreatedAt(now);
        session.setUpdatedAt(now);

        assertEquals("sess-updated", session.getId());
        assertEquals("user-2", session.getUserId());
        assertEquals("New Title", session.getTitle());
        assertEquals("ARCHITECTURE", session.getCategory());
        assertEquals("Architecture review summary", session.getSummary());
        assertEquals("GEMINI", session.getModelProvider());
        assertEquals(15, session.getMessageCount());
        assertEquals(now, session.getCreatedAt());
        assertEquals(now, session.getUpdatedAt());

        ConversationSessionEntity defaultSession = new ConversationSessionEntity("sess-2", "user-1", "Default", null, null);
        assertEquals("GENERAL", defaultSession.getCategory());
        assertEquals("GEMINI", defaultSession.getModelProvider());
    }

    @Test
    @DisplayName("ChatMessageEntity getters and setters")
    void testChatMessageEntity() {
        ChatMessageEntity msg = new ChatMessageEntity("msg-1", "sess-1", "USER", "How to configure Kafka?", 12, "OPENAI_GPT", "CODE_AGENT", 0.95);
        ChatMessageEntity noArg = new ChatMessageEntity();
        assertNotNull(noArg);

        assertEquals("msg-1", msg.getId());
        assertEquals("sess-1", msg.getSessionId());
        assertEquals("USER", msg.getRole());
        assertEquals("How to configure Kafka?", msg.getContent());
        assertEquals(12, msg.getTokenCount());
        assertEquals("OPENAI_GPT", msg.getModelUsed());
        assertEquals("CODE_AGENT", msg.getAgentType());
        assertEquals(0.95, msg.getRankingScore(), 0.001);
        assertNotNull(msg.getCreatedAt());

        msg.setId("msg-2");
        msg.setSessionId("sess-2");
        msg.setRole("ASSISTANT");
        msg.setContent("Config steps...");
        msg.setTokenCount(250);
        msg.setModelUsed("ANTHROPIC_CLAUDE");
        msg.setAgentType("DEVOPS_AGENT");
        msg.setRankingScore(0.99);
        Instant now = Instant.now();
        msg.setCreatedAt(now);

        assertEquals("msg-2", msg.getId());
        assertEquals("sess-2", msg.getSessionId());
        assertEquals("ASSISTANT", msg.getRole());
        assertEquals("Config steps...", msg.getContent());
        assertEquals(250, msg.getTokenCount());
        assertEquals("ANTHROPIC_CLAUDE", msg.getModelUsed());
        assertEquals("DEVOPS_AGENT", msg.getAgentType());
        assertEquals(0.99, msg.getRankingScore(), 0.001);
        assertEquals(now, msg.getCreatedAt());
    }

    @Test
    @DisplayName("UserMemoryEntity getters and setters")
    void testUserMemoryEntity() {
        UserMemoryEntity mem = new UserMemoryEntity("mem-1", "user-1", "PREFERENCE", "Prefers Hexagonal Architecture", 0.98);
        UserMemoryEntity noArg = new UserMemoryEntity();
        assertNotNull(noArg);

        assertEquals("mem-1", mem.getId());
        assertEquals("user-1", mem.getUserId());
        assertEquals("PREFERENCE", mem.getMemoryType());
        assertEquals("Prefers Hexagonal Architecture", mem.getContent());
        assertEquals(0.98, mem.getConfidenceScore(), 0.001);
        assertNotNull(mem.getCreatedAt());
        assertNotNull(mem.getUpdatedAt());

        mem.setId("mem-2");
        mem.setUserId("user-2");
        mem.setMemoryType("FACT");
        mem.setContent("Uses PostgreSQL 16");
        mem.setConfidenceScore(1.0);
        Instant now = Instant.now();
        mem.setCreatedAt(now);
        mem.setUpdatedAt(now);

        assertEquals("mem-2", mem.getId());
        assertEquals("user-2", mem.getUserId());
        assertEquals("FACT", mem.getMemoryType());
        assertEquals("Uses PostgreSQL 16", mem.getContent());
        assertEquals(1.0, mem.getConfidenceScore(), 0.001);
        assertEquals(now, mem.getCreatedAt());
        assertEquals(now, mem.getUpdatedAt());
    }

    @Test
    @DisplayName("ProjectKnowledgeChunkEntity getters, setters, and defaults")
    void testProjectKnowledgeChunkEntity() {
        ProjectKnowledgeChunkEntity chunk = new ProjectKnowledgeChunkEntity(
                "chunk-1", "repo-1", "src/AuthService.java", "JAVA",
                "CLASS", "AuthService", "public class AuthService {}", "[0.12, 0.45, 0.88]", 1
        );
        ProjectKnowledgeChunkEntity noArg = new ProjectKnowledgeChunkEntity();
        assertNotNull(noArg);

        assertEquals("chunk-1", chunk.getId());
        assertEquals("repo-1", chunk.getRepositoryId());
        assertEquals("src/AuthService.java", chunk.getFilePath());
        assertEquals("JAVA", chunk.getLanguage());
        assertEquals("CLASS", chunk.getSymbolType());
        assertEquals("AuthService", chunk.getSymbolName());
        assertEquals("public class AuthService {}", chunk.getContent());
        assertEquals("[0.12, 0.45, 0.88]", chunk.getEmbeddingVectorJson());
        assertEquals(1, chunk.getChunkIndex());
        assertNotNull(chunk.getCreatedAt());

        chunk.setId("chunk-2");
        chunk.setRepositoryId("repo-2");
        chunk.setFilePath("src/App.tsx");
        chunk.setLanguage("TYPESCRIPT");
        chunk.setSymbolType("COMPONENT");
        chunk.setSymbolName("App");
        chunk.setContent("export default function App() {}");
        chunk.setEmbeddingVectorJson("[0.99, 0.01]");
        chunk.setChunkIndex(2);
        Instant now = Instant.now();
        chunk.setCreatedAt(now);

        assertEquals("chunk-2", chunk.getId());
        assertEquals("repo-2", chunk.getRepositoryId());
        assertEquals("src/App.tsx", chunk.getFilePath());
        assertEquals("TYPESCRIPT", chunk.getLanguage());
        assertEquals("COMPONENT", chunk.getSymbolType());
        assertEquals("App", chunk.getSymbolName());
        assertEquals("export default function App() {}", chunk.getContent());
        assertEquals("[0.99, 0.01]", chunk.getEmbeddingVectorJson());
        assertEquals(2, chunk.getChunkIndex());
        assertEquals(now, chunk.getCreatedAt());

        ProjectKnowledgeChunkEntity defaultChunk = new ProjectKnowledgeChunkEntity(
                "chunk-3", "repo-1", "Dockerfile", "DOCKERFILE", "CONFIG", "Docker", "FROM eclipse-temurin:21", null, 0
        );
        assertEquals("[]", defaultChunk.getEmbeddingVectorJson());
    }

    @Test
    @DisplayName("ArgonGoalEntity getters and setters")
    void testArgonGoalEntity() {
        ArgonGoalEntity goal = new ArgonGoalEntity("goal-1", "user-1", "repo-1", "Implement Argon Agent Loop");
        ArgonGoalEntity noArg = new ArgonGoalEntity();
        assertNotNull(noArg);

        assertEquals("goal-1", goal.getId());
        assertEquals("user-1", goal.getUserId());
        assertEquals("repo-1", goal.getRepositoryId());
        assertEquals("Implement Argon Agent Loop", goal.getGoalDescription());
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
    @DisplayName("ArgonPlanStepEntity getters and setters")
    void testArgonPlanStepEntity() {
        ArgonPlanStepEntity step = new ArgonPlanStepEntity("step-1", "goal-1", 1, "ARCHITECTURE_AGENT", "Design DAG", "DESIGN_TOOL");
        ArgonPlanStepEntity noArg = new ArgonPlanStepEntity();
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
    @DisplayName("ArgonExecutionEventEntity getters and setters")
    void testArgonExecutionEventEntity() {
        ArgonExecutionEventEntity event = new ArgonExecutionEventEntity(
                "evt-1", "goal-1", "TOOL_EXECUTION", "CODE_AGENT",
                "Analyzing syntax error", "COMPILE_TEST", "Exit code 1", "SUCCESS"
        );
        ArgonExecutionEventEntity noArg = new ArgonExecutionEventEntity();
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
    @DisplayName("ArgonSelfHealingCycleEntity getters and setters")
    void testArgonSelfHealingCycleEntity() {
        ArgonSelfHealingCycleEntity cycle = new ArgonSelfHealingCycleEntity(
                "cycle-1", "goal-1", "NullPointerException at line 42",
                "Unchecked optional dereference", "+ if (opt.isPresent()) ...", true
        );
        ArgonSelfHealingCycleEntity noArg = new ArgonSelfHealingCycleEntity();
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
