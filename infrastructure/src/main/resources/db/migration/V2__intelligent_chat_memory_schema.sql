-- =====================================================================
-- OENEXA AI ENGINEER - Master Schema Migration V2
-- Phase 2 Intelligent Chat, Multi-Turn Memory & Phase 3 Repo Knowledge
-- =====================================================================

CREATE TABLE IF NOT EXISTS conversation_sessions (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    category VARCHAR(50) DEFAULT 'GENERAL' NOT NULL,
    summary TEXT,
    model_provider VARCHAR(50) DEFAULT 'GEMINI' NOT NULL,
    message_count INT DEFAULT 0 NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS chat_messages (
    id VARCHAR(36) PRIMARY KEY,
    session_id VARCHAR(36) NOT NULL,
    role VARCHAR(30) NOT NULL,
    content TEXT NOT NULL,
    token_count INT DEFAULT 0 NOT NULL,
    model_used VARCHAR(50),
    agent_type VARCHAR(50),
    ranking_score DOUBLE PRECISION DEFAULT 1.0 NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_chat_messages_session FOREIGN KEY (session_id) REFERENCES conversation_sessions(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_memories (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    memory_type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    confidence_score DOUBLE PRECISION DEFAULT 1.0 NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS project_knowledge_chunks (
    id VARCHAR(36) PRIMARY KEY,
    repository_id VARCHAR(100) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    language VARCHAR(50) NOT NULL,
    symbol_type VARCHAR(50) NOT NULL,
    symbol_name VARCHAR(255),
    content TEXT NOT NULL,
    embedding_vector_json TEXT NOT NULL,
    chunk_index INT DEFAULT 0 NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_sessions_user ON conversation_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_messages_session ON chat_messages(session_id);
CREATE INDEX IF NOT EXISTS idx_memories_user ON user_memories(user_id);
CREATE INDEX IF NOT EXISTS idx_knowledge_repo ON project_knowledge_chunks(repository_id);
CREATE INDEX IF NOT EXISTS idx_knowledge_path ON project_knowledge_chunks(file_path);
