-- =====================================================================
-- OENEXA AI ENGINEER - Master Schema Migration V5
-- OENEXA Native Autonomous AI Agent System Schema (Pure Own AI Module)
-- =====================================================================

CREATE TABLE IF NOT EXISTS oenexa_goals (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    repository_id VARCHAR(100) NOT NULL,
    goal_description TEXT NOT NULL,
    current_phase VARCHAR(50) DEFAULT 'PLANNING' NOT NULL,
    autonomous_self_healing BOOLEAN DEFAULT TRUE NOT NULL,
    extended_thinking BOOLEAN DEFAULT TRUE NOT NULL,
    confidence_score DOUBLE PRECISION DEFAULT 1.0 NOT NULL,
    final_summary TEXT,
    status VARCHAR(50) DEFAULT 'RUNNING' NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    completed_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS oenexa_plan_steps (
    id VARCHAR(36) PRIMARY KEY,
    goal_id VARCHAR(36) NOT NULL,
    step_index INT DEFAULT 0 NOT NULL,
    agent_persona VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    tool_to_invoke VARCHAR(100),
    status VARCHAR(50) DEFAULT 'PENDING' NOT NULL,
    output TEXT,
    retry_count INT DEFAULT 0 NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_oenexa_plan_steps_goal FOREIGN KEY (goal_id) REFERENCES oenexa_goals(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS oenexa_execution_events (
    id VARCHAR(36) PRIMARY KEY,
    goal_id VARCHAR(36) NOT NULL,
    phase VARCHAR(50) NOT NULL,
    agent_persona VARCHAR(50) NOT NULL,
    thought_process TEXT,
    tool_action VARCHAR(100),
    output_snippet TEXT,
    status VARCHAR(50) DEFAULT 'SUCCESS' NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_oenexa_events_goal FOREIGN KEY (goal_id) REFERENCES oenexa_goals(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS oenexa_self_healing_cycles (
    id VARCHAR(36) PRIMARY KEY,
    goal_id VARCHAR(36) NOT NULL,
    issue_detected TEXT NOT NULL,
    root_cause TEXT NOT NULL,
    patch_diff TEXT NOT NULL,
    resolved BOOLEAN DEFAULT TRUE NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_oenexa_healing_goal FOREIGN KEY (goal_id) REFERENCES oenexa_goals(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_oenexa_goals_user ON oenexa_goals(user_id);
CREATE INDEX IF NOT EXISTS idx_oenexa_steps_goal ON oenexa_plan_steps(goal_id);
CREATE INDEX IF NOT EXISTS idx_oenexa_events_goal ON oenexa_execution_events(goal_id);
