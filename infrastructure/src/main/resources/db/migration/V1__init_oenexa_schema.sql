-- =====================================================================
-- OENEXA AI ENGINEER - Master Schema Migration V1
-- Enterprise-grade schema for AI Software Engineering Platform
-- =====================================================================

-- 1. Roles & Permissions (RBAC)
CREATE TABLE IF NOT EXISTS roles (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS permissions (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    resource VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id VARCHAR(36) NOT NULL,
    permission_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- 2. Users & Session Tracking
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150),
    mfa_enabled BOOLEAN DEFAULT FALSE NOT NULL,
    mfa_secret VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    enabled BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- 3. Projects & Repositories
CREATE TABLE IF NOT EXISTS projects (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    slug VARCHAR(150) NOT NULL UNIQUE,
    description TEXT,
    owner_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_projects_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS repositories (
    id VARCHAR(36) PRIMARY KEY,
    project_id VARCHAR(36) NOT NULL,
    name VARCHAR(150) NOT NULL,
    clone_url VARCHAR(500) NOT NULL,
    provider VARCHAR(50) DEFAULT 'GITHUB' NOT NULL,
    default_branch VARCHAR(100) DEFAULT 'main' NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE' NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_repositories_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

-- 4. Scans & Static/Architectural Analysis
CREATE TABLE IF NOT EXISTS scans (
    id VARCHAR(36) PRIMARY KEY,
    repository_id VARCHAR(36) NOT NULL,
    branch VARCHAR(100) NOT NULL,
    commit_hash VARCHAR(100),
    scan_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING' NOT NULL,
    findings_count INT DEFAULT 0 NOT NULL,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    CONSTRAINT fk_scans_repository FOREIGN KEY (repository_id) REFERENCES repositories(id) ON DELETE CASCADE
);

-- 5. Issues & Fixes
CREATE TABLE IF NOT EXISTS issues (
    id VARCHAR(36) PRIMARY KEY,
    repository_id VARCHAR(36) NOT NULL,
    scan_id VARCHAR(36),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    severity VARCHAR(30) DEFAULT 'MEDIUM' NOT NULL,
    status VARCHAR(50) DEFAULT 'OPEN' NOT NULL,
    file_path VARCHAR(500),
    line_number INT,
    root_cause TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_issues_repository FOREIGN KEY (repository_id) REFERENCES repositories(id) ON DELETE CASCADE,
    CONSTRAINT fk_issues_scan FOREIGN KEY (scan_id) REFERENCES scans(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS fixes (
    id VARCHAR(36) PRIMARY KEY,
    issue_id VARCHAR(36) NOT NULL,
    status VARCHAR(50) DEFAULT 'GENERATED' NOT NULL,
    patch_diff TEXT,
    commit_message VARCHAR(255),
    validated BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_fixes_issue FOREIGN KEY (issue_id) REFERENCES issues(id) ON DELETE CASCADE
);

-- 6. Pull Requests & AI Reviews
CREATE TABLE IF NOT EXISTS pull_requests (
    id VARCHAR(36) PRIMARY KEY,
    repository_id VARCHAR(36) NOT NULL,
    pr_number INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    source_branch VARCHAR(100) NOT NULL,
    target_branch VARCHAR(100) NOT NULL,
    status VARCHAR(50) DEFAULT 'OPEN' NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_pull_requests_repository FOREIGN KEY (repository_id) REFERENCES repositories(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reviews (
    id VARCHAR(36) PRIMARY KEY,
    pull_request_id VARCHAR(36) NOT NULL,
    reviewer_type VARCHAR(50) DEFAULT 'AI_AGENT' NOT NULL,
    quality_score INT DEFAULT 100,
    risk_score INT DEFAULT 0,
    summary TEXT,
    status VARCHAR(50) DEFAULT 'APPROVED' NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_reviews_pull_request FOREIGN KEY (pull_request_id) REFERENCES pull_requests(id) ON DELETE CASCADE
);

-- 7. Automated Test Executions
CREATE TABLE IF NOT EXISTS tests (
    id VARCHAR(36) PRIMARY KEY,
    repository_id VARCHAR(36) NOT NULL,
    test_framework VARCHAR(50) DEFAULT 'JUNIT5' NOT NULL,
    test_count INT DEFAULT 0 NOT NULL,
    passed_count INT DEFAULT 0 NOT NULL,
    failed_count INT DEFAULT 0 NOT NULL,
    coverage_pct NUMERIC(5,2) DEFAULT 0.00 NOT NULL,
    run_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_tests_repository FOREIGN KEY (repository_id) REFERENCES repositories(id) ON DELETE CASCADE
);

-- 8. Security Findings (SAST / CVE)
CREATE TABLE IF NOT EXISTS security_findings (
    id VARCHAR(36) PRIMARY KEY,
    repository_id VARCHAR(36) NOT NULL,
    scan_id VARCHAR(36),
    cve_id VARCHAR(50),
    vulnerability_type VARCHAR(100) NOT NULL,
    severity VARCHAR(30) DEFAULT 'HIGH' NOT NULL,
    description TEXT NOT NULL,
    remediation TEXT,
    status VARCHAR(50) DEFAULT 'UNRESOLVED' NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_sec_findings_repository FOREIGN KEY (repository_id) REFERENCES repositories(id) ON DELETE CASCADE,
    CONSTRAINT fk_sec_findings_scan FOREIGN KEY (scan_id) REFERENCES scans(id) ON DELETE SET NULL
);

-- 9. Deployments (DevOps & GitOps)
CREATE TABLE IF NOT EXISTS deployments (
    id VARCHAR(36) PRIMARY KEY,
    project_id VARCHAR(36) NOT NULL,
    environment VARCHAR(50) DEFAULT 'PRODUCTION' NOT NULL,
    version VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'DEPLOYED' NOT NULL,
    deployed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_deployments_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

-- 10. Audit & Application Logs
CREATE TABLE IF NOT EXISTS logs (
    id VARCHAR(36) PRIMARY KEY,
    repository_id VARCHAR(36),
    scan_id VARCHAR(36),
    log_level VARCHAR(20) DEFAULT 'INFO' NOT NULL,
    source VARCHAR(100) NOT NULL,
    message TEXT NOT NULL,
    stack_trace TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 11. AI Agent Orchestration Tasks
CREATE TABLE IF NOT EXISTS agent_tasks (
    id VARCHAR(36) PRIMARY KEY,
    agent_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'RUNNING' NOT NULL,
    input_payload TEXT,
    output_payload TEXT,
    error_message TEXT,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    completed_at TIMESTAMP
);

-- Indexes for high-frequency queries
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_repositories_project ON repositories(project_id);
CREATE INDEX IF NOT EXISTS idx_scans_repo ON scans(repository_id);
CREATE INDEX IF NOT EXISTS idx_issues_repo ON issues(repository_id);
CREATE INDEX IF NOT EXISTS idx_issues_status ON issues(status);
CREATE INDEX IF NOT EXISTS idx_fixes_issue ON fixes(issue_id);
CREATE INDEX IF NOT EXISTS idx_pull_requests_repo ON pull_requests(repository_id);
CREATE INDEX IF NOT EXISTS idx_security_findings_repo ON security_findings(repository_id);
CREATE INDEX IF NOT EXISTS idx_agent_tasks_status ON agent_tasks(status);

-- Seed Initial Roles
INSERT INTO roles (id, name, description) VALUES
('r-admin', 'ROLE_ADMIN', 'Platform Administrator with unrestricted privileges'),
('r-engineer', 'ROLE_ENGINEER', 'Software Engineer with repository, scan, and fix privileges'),
('r-viewer', 'ROLE_VIEWER', 'Auditor with read-only access to architecture and reports');

-- Seed Default Admin User (password: Admin@Oenexa2026!)
INSERT INTO users (username, email, password_hash, full_name, is_active, enabled) VALUES
('admin', 'admin@oenexa.com', '$2a$12$e8k8Wk6oXkYQWk6oXkYQW.4Qj7R2Y1z9w9lJgK9fK9d9c9b9a9Z9', 'Platform Administrator', TRUE, TRUE);

INSERT INTO user_roles (user_id, role_id) VALUES
(1, 'r-admin');
