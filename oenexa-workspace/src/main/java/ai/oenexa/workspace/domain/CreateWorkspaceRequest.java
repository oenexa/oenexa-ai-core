package ai.oenexa.workspace.domain;

import java.util.List;
import java.util.Objects;

/**
 * Payload to initialize a new tenant workspace.
 */
public record CreateWorkspaceRequest(
        String tenantId,
        String orgName,
        String teamName,
        List<String> initialProjectNames
) {
    public CreateWorkspaceRequest {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(orgName, "orgName must not be null");
        if (teamName == null || teamName.isBlank()) {
            teamName = "engineering";
        }
        if (initialProjectNames == null) {
            initialProjectNames = List.of();
        }
    }
}
