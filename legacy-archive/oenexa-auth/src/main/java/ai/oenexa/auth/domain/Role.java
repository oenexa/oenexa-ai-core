package ai.oenexa.auth.domain;

import java.util.Set;

/** Coarse RBAC roles. Fine-grained ABAC checks build on the permissions below. */
public enum Role {
    OWNER(Set.of("org:manage", "billing:manage", "member:manage", "project:write", "project:read", "agent:run")),
    ADMIN(Set.of("member:manage", "project:write", "project:read", "agent:run")),
    DEVELOPER(Set.of("project:write", "project:read", "agent:run")),
    VIEWER(Set.of("project:read"));

    private final Set<String> permissions;

    Role(Set<String> permissions) {
        this.permissions = permissions;
    }

    public Set<String> permissions() {
        return permissions;
    }

    public boolean allows(String permission) {
        return permissions.contains(permission);
    }
}
