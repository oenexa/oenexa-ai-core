package ai.oenexa.core.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * User account aggregate in the relational core.
 */
public record CoreUser(
        UUID id,
        UUID organizationId,
        String email,
        String displayName,
        boolean enabled,
        boolean mfaEnabled,
        Instant createdAt,
        Instant updatedAt
) {
    public CoreUser {
        if (id == null) id = UUID.randomUUID();
        Objects.requireNonNull(organizationId, "organizationId must not be null");
        Objects.requireNonNull(email, "email must not be null");
        if (displayName == null || displayName.isBlank()) displayName = email;
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
    }

    public static CoreUser create(UUID organizationId, String email, String displayName) {
        Instant now = Instant.now();
        return new CoreUser(UUID.randomUUID(), organizationId, email, displayName, true, false, now, now);
    }
}
