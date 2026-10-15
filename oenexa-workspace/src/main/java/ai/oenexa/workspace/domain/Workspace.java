package ai.oenexa.workspace.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Multi-tenant organization and team project workspace container.
 */
public record Workspace(
        String id,
        String tenantId,
        String orgName,
        String teamName,
        WorkspaceStatus status,
        List<WorkspaceProject> projects,
        Instant createdAt
) {
    public Workspace {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(orgName, "orgName must not be null");
        if (teamName == null || teamName.isBlank()) {
            teamName = "default";
        }
        if (status == null) {
            status = WorkspaceStatus.ACTIVE;
        }
        if (projects == null) {
            projects = List.of();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public int projectCount() {
        return projects.size();
    }
}
