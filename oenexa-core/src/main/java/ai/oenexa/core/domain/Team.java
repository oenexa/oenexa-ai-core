package ai.oenexa.core.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Team aggregate within an organization.
 */
public record Team(
        UUID id,
        UUID organizationId,
        String name,
        Instant createdAt
) {
    public Team {
        if (id == null) id = UUID.randomUUID();
        Objects.requireNonNull(organizationId, "organizationId must not be null");
        Objects.requireNonNull(name, "name must not be null");
        if (createdAt == null) createdAt = Instant.now();
    }

    public static Team create(UUID organizationId, String name) {
        return new Team(UUID.randomUUID(), organizationId, name, Instant.now());
    }
}
