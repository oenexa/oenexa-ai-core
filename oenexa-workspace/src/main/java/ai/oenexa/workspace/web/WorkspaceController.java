package ai.oenexa.workspace.web;

import ai.oenexa.workspace.domain.CreateWorkspaceRequest;
import ai.oenexa.workspace.domain.Workspace;
import ai.oenexa.workspace.service.WorkspaceService;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = Objects.requireNonNull(workspaceService, "workspaceService must not be null");
    }

    @PostMapping
    public ResponseEntity<Workspace> create(@RequestBody CreateWorkspaceRequest request) {
        return ResponseEntity.ok(workspaceService.createWorkspace(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Workspace> get(@PathVariable String id) {
        return ResponseEntity.ok(workspaceService.getWorkspace(id));
    }

    @PostMapping("/{id}/projects")
    public ResponseEntity<Workspace> addProject(@PathVariable String id, @RequestBody Map<String, String> payload) {
        String name = payload.getOrDefault("name", "unnamed");
        String repo = payload.getOrDefault("repositoryUrl", "");
        return ResponseEntity.ok(workspaceService.addProject(id, name, repo));
    }

    @PostMapping("/{id}/isolate")
    public ResponseEntity<Map<String, Boolean>> isolate(@PathVariable String id) {
        boolean isolated = workspaceService.isolateWorkspace(id);
        return ResponseEntity.ok(Map.of("isolated", isolated));
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<List<Workspace>> listByTenant(@PathVariable String tenantId) {
        return ResponseEntity.ok(workspaceService.listWorkspacesForTenant(tenantId));
    }
}
