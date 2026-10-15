package ai.oenexa.admin.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * Immutable audit record tracking administrative and tenant activity.
 */
public record AuditLog(
        String id,
        String tenantId,
        String actor,
        AuditAction action,
        String resource,
        Instant timestamp,
        String ipAddress,
        boolean successful
) {
    public AuditLog {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(actor, "actor must not be null");
        Objects.requireNonNull(action, "action must not be null");
        if (resource == null) {
            resource = "general";
        }
        if (timestamp == null) {
            timestamp = Instant.now();
        }
        if (ipAddress == null) {
            ipAddress = "127.0.0.1";
        }
    }
}
