package ai.oenexa.workspace.service;

import ai.oenexa.workspace.domain.CreateWorkspaceRequest;
import ai.oenexa.workspace.domain.Workspace;
import ai.oenexa.workspace.domain.WorkspaceProject;
import ai.oenexa.workspace.domain.WorkspaceStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class DefaultWorkspaceService implements WorkspaceService {

    private final Map<String, Workspace> workspaceStore = new ConcurrentHashMap<>();

    @Override
    public Workspace createWorkspace(CreateWorkspaceRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        String id = "ws-" + UUID.randomUUID().toString().substring(0, 8);
        List<WorkspaceProject> projects = new ArrayList<>();
        for (String projName : request.initialProjectNames()) {
            projects.add(new WorkspaceProject(
                    "proj-" + UUID.randomUUID().toString().substring(0, 6),
                    projName,
                    "https://github.com/" + request.orgName() + "/" + projName,
                    true
            ));
        }

        Workspace workspace = new Workspace(
                id,
                request.tenantId(),
                request.orgName(),
                request.teamName(),
                WorkspaceStatus.ACTIVE,
                projects,
                Instant.now()
        );

        workspaceStore.put(id, workspace);
        return workspace;
    }

    @Override
    public Workspace getWorkspace(String workspaceId) {
        Objects.requireNonNull(workspaceId, "workspaceId must not be null");
        Workspace ws = workspaceStore.get(workspaceId);
        if (ws == null) {
            throw new IllegalArgumentException("Workspace not found: " + workspaceId);
        }
        return ws;
    }

    @Override
    public Workspace addProject(String workspaceId, String projectName, String repositoryUrl) {
        Workspace ws = getWorkspace(workspaceId);
        List<WorkspaceProject> updated = new ArrayList<>(ws.projects());
        updated.add(new WorkspaceProject(
                "proj-" + UUID.randomUUID().toString().substring(0, 6),
                projectName,
                repositoryUrl,
                true
        ));

        Workspace updatedWs = new Workspace(
                ws.id(),
                ws.tenantId(),
                ws.orgName(),
                ws.teamName(),
                ws.status(),
                updated,
                ws.createdAt()
        );

        workspaceStore.put(workspaceId, updatedWs);
        return updatedWs;
    }

    @Override
    public boolean isolateWorkspace(String workspaceId) {
        if (!workspaceStore.containsKey(workspaceId)) {
            return false;
        }
        Workspace ws = workspaceStore.get(workspaceId);
        List<WorkspaceProject> isolatedProjects = ws.projects().stream()
                .map(p -> new WorkspaceProject(p.projectId(), p.name(), p.repositoryUrl(), true))
                .toList();

        workspaceStore.put(workspaceId, new Workspace(
                ws.id(),
                ws.tenantId(),
                ws.orgName(),
                ws.teamName(),
                ws.status(),
                isolatedProjects,
                ws.createdAt()
        ));
        return true;
    }

    @Override
    public List<Workspace> listWorkspacesForTenant(String tenantId) {
        if (tenantId == null) {
            return List.of();
        }
        return workspaceStore.values().stream()
                .filter(w -> tenantId.equals(w.tenantId()))
                .toList();
    }
}
