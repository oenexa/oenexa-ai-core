-- Identity, tenancy and access control.
-- Tenant = organization. Every tenant-scoped table below carries organization_id.

CREATE TABLE organizations (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(200) NOT NULL,
    slug        VARCHAR(100) NOT NULL UNIQUE,
    plan        VARCHAR(30)  NOT NULL DEFAULT 'FREE'
                CHECK (plan IN ('FREE', 'STARTUP', 'SME', 'ENTERPRISE')),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE users (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id  UUID         NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    email            VARCHAR(320) NOT NULL,
    password_hash    VARCHAR(100),                -- NULL for pure SSO accounts
    display_name     VARCHAR(200),
    enabled          BOOLEAN      NOT NULL DEFAULT TRUE,
    mfa_enabled      BOOLEAN      NOT NULL DEFAULT FALSE,
    mfa_secret_enc   BYTEA,                       -- encrypted at application level
    oauth_provider   VARCHAR(30),                 -- GITHUB | GOOGLE | MICROSOFT
    oauth_subject    VARCHAR(255),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX ux_users_email ON users (lower(email));
CREATE UNIQUE INDEX ux_users_oauth ON users (oauth_provider, oauth_subject) WHERE oauth_provider IS NOT NULL;
CREATE INDEX ix_users_org ON users (organization_id);

CREATE TABLE permissions (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code         VARCHAR(100) NOT NULL UNIQUE,     -- e.g. project:write
    description  VARCHAR(500)
);

CREATE TABLE roles (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id  UUID REFERENCES organizations (id) ON DELETE CASCADE,  -- NULL = built-in role
    name             VARCHAR(100) NOT NULL,
    description      VARCHAR(500),
    UNIQUE (organization_id, name)
);

CREATE TABLE role_permissions (
    role_id        UUID NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    permission_id  UUID NOT NULL REFERENCES permissions (id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE user_roles (
    user_id  UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id  UUID NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE teams (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id  UUID         NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    name             VARCHAR(200) NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE (organization_id, name)
);

CREATE TABLE team_members (
    team_id  UUID NOT NULL REFERENCES teams (id) ON DELETE CASCADE,
    user_id  UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    PRIMARY KEY (team_id, user_id)
);

CREATE TABLE sessions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    refresh_token_hash  VARCHAR(128) NOT NULL UNIQUE,   -- store only a hash, never the token
    device_name         VARCHAR(200),
    user_agent          VARCHAR(500),
    ip_address          INET,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at          TIMESTAMPTZ NOT NULL,
    revoked_at          TIMESTAMPTZ
);
CREATE INDEX ix_sessions_user ON sessions (user_id) WHERE revoked_at IS NULL;

CREATE TABLE audit_logs (
    id               BIGSERIAL PRIMARY KEY,
    organization_id  UUID        NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    actor_id         UUID,
    action           VARCHAR(100) NOT NULL,
    resource_type    VARCHAR(100),
    resource_id      VARCHAR(100),
    details          JSONB       NOT NULL DEFAULT '{}'::jsonb,
    ip_address       INET,
    device           VARCHAR(500),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ix_audit_org_time ON audit_logs (organization_id, created_at DESC);

INSERT INTO permissions (code, description) VALUES
    ('org:manage', 'Manage organization settings'),
    ('billing:manage', 'Manage billing'),
    ('member:manage', 'Invite and remove members'),
    ('project:write', 'Create and modify projects'),
    ('project:read', 'Read projects'),
    ('agent:run', 'Start agent workflows');

INSERT INTO roles (name, description) VALUES
    ('OWNER', 'Full control'), ('ADMIN', 'Administration without billing'),
    ('DEVELOPER', 'Build and run agents'), ('VIEWER', 'Read-only');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON
    (r.name = 'OWNER') OR
    (r.name = 'ADMIN' AND p.code IN ('member:manage', 'project:write', 'project:read', 'agent:run')) OR
    (r.name = 'DEVELOPER' AND p.code IN ('project:write', 'project:read', 'agent:run')) OR
    (r.name = 'VIEWER' AND p.code = 'project:read')
WHERE r.organization_id IS NULL;
