package ai.oenexa.core.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Engineering project within an organization and optional team.
 */
public record Project(
        UUID id,
        UUID organizationId,
        UUID teamId,
        String name,
        String description,
        UUID createdBy,
        Instant createdAt,
        Instant updatedAt
) {
    public Project {
        if (id == null) id = UUID.randomUUID();
        Objects.requireNonNull(organizationId, "organizationId must not be null");
        Objects.requireNonNull(name, "name must not be null");
        if (description == null) description = "";
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
    }

    public static Project create(UUID organizationId, UUID teamId, String name, String description, UUID createdBy) {
        Instant now = Instant.now();
        return new Project(UUID.randomUUID(), organizationId, teamId, name, description, createdBy, now, now);
    }
}
