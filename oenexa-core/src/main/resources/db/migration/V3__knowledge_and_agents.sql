-- RAG metadata and agent task tracking. Vectors themselves live in Qdrant; `embeddings`
-- keeps the pointer + provenance so deletes and re-indexing stay consistent.

CREATE TABLE knowledge_base (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id  UUID         NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    project_id       UUID REFERENCES projects (id) ON DELETE CASCADE,
    name             VARCHAR(200) NOT NULL,
    source_type      VARCHAR(30)  NOT NULL
                     CHECK (source_type IN ('UPLOAD', 'REPOSITORY', 'CONFLUENCE', 'NOTION')),
    qdrant_collection VARCHAR(200) NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE (organization_id, name)
);

CREATE TABLE documents (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id   UUID         NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    knowledge_base_id UUID REFERENCES knowledge_base (id) ON DELETE CASCADE,
    uploaded_by       UUID REFERENCES users (id) ON DELETE SET NULL,
    filename          VARCHAR(500) NOT NULL,
    media_type        VARCHAR(150) NOT NULL,
    size_bytes        BIGINT       NOT NULL CHECK (size_bytes >= 0),
    sha256            CHAR(64)     NOT NULL,
    storage_uri       VARCHAR(1000) NOT NULL,
    status            VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                      CHECK (status IN ('PENDING', 'INDEXED', 'FAILED')),
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE (organization_id, sha256)
);

CREATE TABLE embeddings (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id  UUID         NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    document_id      UUID         NOT NULL REFERENCES documents (id) ON DELETE CASCADE,
    chunk_index      INTEGER      NOT NULL,
    qdrant_point_id  UUID         NOT NULL,
    model            VARCHAR(100) NOT NULL,
    dimensions       INTEGER      NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE (document_id, chunk_index, model)
);

CREATE TABLE agent_tasks (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id  UUID         NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    run_id           UUID         NOT NULL,
    parent_task_id   UUID REFERENCES agent_tasks (id) ON DELETE CASCADE,
    project_id       UUID REFERENCES projects (id) ON DELETE SET NULL,
    agent_type       VARCHAR(30)  NOT NULL,
    objective        TEXT         NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                     CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'CANCELLED')),
    result           TEXT,
    error            TEXT,
    started_at       TIMESTAMPTZ,
    finished_at      TIMESTAMPTZ,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX ix_agent_tasks_run ON agent_tasks (organization_id, run_id);
