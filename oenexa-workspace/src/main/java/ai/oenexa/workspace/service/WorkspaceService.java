package ai.oenexa.workspace.service;

import ai.oenexa.workspace.domain.CreateWorkspaceRequest;
import ai.oenexa.workspace.domain.Workspace;
import java.util.List;

/**
 * Service port for managing multi-tenant workspaces and projects.
 */
public interface WorkspaceService {

    Workspace createWorkspace(CreateWorkspaceRequest request);

    Workspace getWorkspace(String workspaceId);

    Workspace addProject(String workspaceId, String projectName, String repositoryUrl);

    boolean isolateWorkspace(String workspaceId);

    List<Workspace> listWorkspacesForTenant(String tenantId);
}
