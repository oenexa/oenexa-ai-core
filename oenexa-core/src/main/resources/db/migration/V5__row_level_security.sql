-- Defence in depth for tenant isolation: PostgreSQL row-level security.
-- The application sets the tenant per transaction:  SET LOCAL app.tenant_id = '<uuid>';
-- If it is unset, current_setting(..., true) is NULL and NO rows match (fail closed).
-- FORCE applies the policy to the table owner too. Migrations run as a role with BYPASSRLS
-- (or the migration user must be a superuser); the runtime role must not have BYPASSRLS.
-- `users` is intentionally excluded: sign-in must resolve an account by email before the tenant
-- is known. Access to it is limited to the auth module's queries (email / id lookups only).

DO $$
DECLARE
    t TEXT;
BEGIN
    FOREACH t IN ARRAY ARRAY[
        'teams', 'projects', 'repositories', 'chats', 'messages', 'notifications',
        'knowledge_base', 'documents', 'embeddings', 'agent_tasks', 'pull_requests', 'reviews',
        'security_findings', 'deployments', 'pipelines', 'logs', 'audit_logs'
    ]
    LOOP
        EXECUTE format('ALTER TABLE %I ENABLE ROW LEVEL SECURITY', t);
        EXECUTE format('ALTER TABLE %I FORCE ROW LEVEL SECURITY', t);
        EXECUTE format(
            'CREATE POLICY tenant_isolation ON %I USING (organization_id = NULLIF(current_setting(''app.tenant_id'', true), '''')::uuid) '
            'WITH CHECK (organization_id = NULLIF(current_setting(''app.tenant_id'', true), '''')::uuid)', t);
    END LOOP;
END $$;
