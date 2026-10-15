package ai.oenexa.admin.domain;

/**
 * Standard security and administrative audit actions.
 */
public enum AuditAction {
    USER_LOGIN,
    WORKSPACE_CREATED,
    TOKEN_ISSUED,
    POLICY_CHANGED,
    SECRET_ROTATED
}
