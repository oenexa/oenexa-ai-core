-- =====================================================================
-- OENEXA AI ENGINEER - Master Schema Migration V4
-- Local-First, Multimodal & Product Manager Agent System Schema
-- =====================================================================

CREATE TABLE IF NOT EXISTS product_requirements_documents (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    target_audience VARCHAR(255) NOT NULL,
    problem_statement TEXT NOT NULL,
    key_features TEXT NOT NULL,
    technical_constraints TEXT NOT NULL,
    release_roadmap VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS user_stories (
    id VARCHAR(36) PRIMARY KEY,
    prd_id VARCHAR(36),
    title VARCHAR(255) NOT NULL,
    as_a VARCHAR(255) NOT NULL,
    i_want TEXT NOT NULL,
    so_that TEXT NOT NULL,
    acceptance_criteria TEXT NOT NULL,
    story_points INT DEFAULT 3 NOT NULL,
    status VARCHAR(50) DEFAULT 'BACKLOG' NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_user_stories_prd FOREIGN KEY (prd_id) REFERENCES product_requirements_documents(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS multimodal_attachments (
    id VARCHAR(36) PRIMARY KEY,
    goal_id VARCHAR(36),
    attachment_type VARCHAR(50) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    base64_data_or_url TEXT NOT NULL,
    extracted_text_or_transcript TEXT,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS local_agent_audit_logs (
    id VARCHAR(36) PRIMARY KEY,
    goal_id VARCHAR(36),
    local_only_mode BOOLEAN DEFAULT TRUE NOT NULL,
    telemetry_disabled BOOLEAN DEFAULT TRUE NOT NULL,
    local_model_endpoint VARCHAR(255) NOT NULL,
    selected_local_model VARCHAR(100) NOT NULL,
    max_vram_allocation_mb INT DEFAULT 8192 NOT NULL,
    air_gapped_verification BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_user_stories_prd ON user_stories(prd_id);
CREATE INDEX IF NOT EXISTS idx_multimodal_goal ON multimodal_attachments(goal_id);
CREATE INDEX IF NOT EXISTS idx_local_audit_goal ON local_agent_audit_logs(goal_id);
