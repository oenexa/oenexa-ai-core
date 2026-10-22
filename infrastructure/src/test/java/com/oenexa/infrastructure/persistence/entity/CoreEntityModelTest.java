package com.oenexa.infrastructure.persistence.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Core Platform JPA Entities Zero-Mock Invariant & Mutator Tests")
class CoreEntityModelTest {

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
}
