-- Pull requests, reviews, security findings, deployments, pipelines and logs.

CREATE TABLE pull_requests (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id  UUID         NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    repository_id    UUID         NOT NULL REFERENCES repositories (id) ON DELETE CASCADE,
    external_number  INTEGER      NOT NULL,
    title            VARCHAR(500) NOT NULL,
    author           VARCHAR(200),
    state            VARCHAR(20)  NOT NULL CHECK (state IN ('OPEN', 'MERGED', 'CLOSED')),
    head_sha         VARCHAR(64),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE (repository_id, external_number)
);

CREATE TABLE reviews (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id  UUID         NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    pull_request_id  UUID         NOT NULL REFERENCES pull_requests (id) ON DELETE CASCADE,
    risk_score       SMALLINT     NOT NULL CHECK (risk_score BETWEEN 0 AND 100),
    quality_score    SMALLINT     NOT NULL CHECK (quality_score BETWEEN 0 AND 100),
    security_score   SMALLINT     NOT NULL CHECK (security_score BETWEEN 0 AND 100),
    summary          TEXT,
    comments         JSONB        NOT NULL DEFAULT '[]'::jsonb,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE security_findings (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id  UUID         NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    repository_id    UUID REFERENCES repositories (id) ON DELETE CASCADE,
    scanner          VARCHAR(20)  NOT NULL CHECK (scanner IN ('SAST', 'DAST', 'DEPENDENCY', 'SECRETS')),
    category         VARCHAR(100) NOT NULL,        -- e.g. SQL_INJECTION, SSRF, XSS
    severity         VARCHAR(10)  NOT NULL CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    file_path        VARCHAR(1000),
    line_number      INTEGER,
    description      TEXT         NOT NULL,
    suggested_fix    TEXT,
    status           VARCHAR(20)  NOT NULL DEFAULT 'OPEN'
                     CHECK (status IN ('OPEN', 'FIXED', 'ACCEPTED', 'FALSE_POSITIVE')),
    detected_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX ix_findings_open ON security_findings (organization_id, severity) WHERE status = 'OPEN';

CREATE TABLE deployments (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id  UUID         NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    project_id       UUID         NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    environment      VARCHAR(50)  NOT NULL,
    version          VARCHAR(100) NOT NULL,
    status           VARCHAR(20)  NOT NULL
                     CHECK (status IN ('PENDING', 'IN_PROGRESS', 'SUCCEEDED', 'FAILED', 'ROLLED_BACK')),
    triggered_by     UUID REFERENCES users (id) ON DELETE SET NULL,
    started_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    finished_at      TIMESTAMPTZ
);

CREATE TABLE pipelines (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id  UUID         NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    repository_id    UUID         NOT NULL REFERENCES repositories (id) ON DELETE CASCADE,
    provider         VARCHAR(30)  NOT NULL
                     CHECK (provider IN ('GITHUB_ACTIONS', 'JENKINS', 'GITLAB_CI', 'AZURE_PIPELINES')),
    external_run_id  VARCHAR(255) NOT NULL,
    status           VARCHAR(20)  NOT NULL,
    failure_analysis TEXT,
    corrected_config TEXT,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE (repository_id, provider, external_run_id)
);

CREATE TABLE logs (
    id               BIGSERIAL PRIMARY KEY,
    organization_id  UUID         NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    source           VARCHAR(200) NOT NULL,
    level            VARCHAR(10)  NOT NULL,
    message          TEXT         NOT NULL,
    fingerprint      CHAR(64),                      -- error-grouping key
    attributes       JSONB        NOT NULL DEFAULT '{}'::jsonb,
    logged_at        TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX ix_logs_org_time ON logs (organization_id, logged_at DESC);
CREATE INDEX ix_logs_fingerprint ON logs (organization_id, fingerprint) WHERE fingerprint IS NOT NULL;
