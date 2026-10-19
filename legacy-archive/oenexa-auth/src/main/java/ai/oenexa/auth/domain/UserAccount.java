package ai.oenexa.auth.domain;

import java.util.UUID;

public record UserAccount(UUID id, UUID tenantId, String email, String passwordHash, Role role, boolean enabled) {
}
