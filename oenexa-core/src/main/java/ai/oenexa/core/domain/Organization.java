package ai.oenexa.core.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Multi-tenant organization aggregate.
 */
public record Organization(
        UUID id,
        String name,
        String slug,
        String plan,
        Instant createdAt,
        Instant updatedAt
) {
    public Organization {
        if (id == null) id = UUID.randomUUID();
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(slug, "slug must not be null");
        if (plan == null || plan.isBlank()) plan = "FREE";
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
    }

    public static Organization create(String name, String slug, String plan) {
        Instant now = Instant.now();
        return new Organization(UUID.randomUUID(), name, slug, plan, now, now);
    }
}
