package ai.oenexa.workspace.domain;

import java.util.Objects;

/**
 * Isolated project managed within a team workspace.
 */
public record WorkspaceProject(
        String projectId,
        String name,
        String repositoryUrl,
        boolean isolated
) {
    public WorkspaceProject {
        Objects.requireNonNull(projectId, "projectId must not be null");
        Objects.requireNonNull(name, "name must not be null");
        if (repositoryUrl == null) {
            repositoryUrl = "";
        }
    }
}
