-- Projects, repositories, chat and notifications.

CREATE TABLE projects (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id  UUID         NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    team_id          UUID REFERENCES teams (id) ON DELETE SET NULL,
    name             VARCHAR(200) NOT NULL,
    description      TEXT,
    created_by       UUID REFERENCES users (id) ON DELETE SET NULL,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE (organization_id, name)
);

CREATE TABLE repositories (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id  UUID         NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    project_id       UUID         NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    provider         VARCHAR(20)  NOT NULL CHECK (provider IN ('GITHUB', 'GITLAB', 'BITBUCKET')),
    external_id      VARCHAR(255) NOT NULL,
    full_name        VARCHAR(500) NOT NULL,
    clone_url        VARCHAR(1000) NOT NULL,
    default_branch   VARCHAR(255) NOT NULL DEFAULT 'main',
    credential_ref   VARCHAR(255),                  -- pointer into the secret store, never the token
    last_analyzed_at TIMESTAMPTZ,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE (organization_id, provider, external_id)
);
CREATE INDEX ix_repositories_project ON repositories (project_id);

CREATE TABLE chats (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id  UUID         NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    owner_id         UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    project_id       UUID REFERENCES projects (id) ON DELETE SET NULL,
    folder           VARCHAR(200),
    title            VARCHAR(300),
    system_prompt    TEXT,
    shared           BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX ix_chats_owner ON chats (organization_id, owner_id, updated_at DESC);

CREATE TABLE messages (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id  UUID         NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    chat_id          UUID         NOT NULL REFERENCES chats (id) ON DELETE CASCADE,
    role             VARCHAR(20)  NOT NULL CHECK (role IN ('SYSTEM', 'USER', 'ASSISTANT', 'TOOL')),
    content          TEXT         NOT NULL,
    token_count      INTEGER,
    model            VARCHAR(100),
    attachments      JSONB        NOT NULL DEFAULT '[]'::jsonb,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX ix_messages_chat ON messages (chat_id, created_at);

CREATE TABLE notifications (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id  UUID         NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    user_id          UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    type             VARCHAR(60)  NOT NULL,
    title            VARCHAR(300) NOT NULL,
    body             TEXT,
    read_at          TIMESTAMPTZ,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX ix_notifications_unread ON notifications (user_id, created_at DESC) WHERE read_at IS NULL;
