package ai.oenexa.workspace;

import ai.oenexa.workspace.domain.CreateWorkspaceRequest;
import ai.oenexa.workspace.domain.Workspace;
import ai.oenexa.workspace.domain.WorkspaceProject;
import ai.oenexa.workspace.domain.WorkspaceRole;
import ai.oenexa.workspace.domain.WorkspaceStatus;
import ai.oenexa.workspace.service.DefaultWorkspaceService;
import ai.oenexa.workspace.web.WorkspaceController;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OENEXA Workspace Service & Controller Tests (Zero-Mock Policy)")
class WorkspaceServiceTest {

    private DefaultWorkspaceService workspaceService;
    private WorkspaceController workspaceController;

    @BeforeEach
    void setUp() {
        workspaceService = new DefaultWorkspaceService();
        workspaceController = new WorkspaceController(workspaceService);
    }

    @Test
    @DisplayName("WorkspaceModule metadata and initialization")
    void testWorkspaceModuleMetadata() {
        WorkspaceModule module = new WorkspaceModule();
        assertEquals("workspace", module.id());
        assertEquals("OENEXA Workspace", module.displayName());
        assertTrue(module.capabilities().contains("workspace-isolation"));
    }

    @Test
    @DisplayName("WorkspaceRole and WorkspaceStatus enum invariants")
    void testEnumInvariants() {
        assertEquals(4, WorkspaceRole.values().length);
        assertEquals(WorkspaceRole.OWNER, WorkspaceRole.valueOf("OWNER"));

        assertEquals(3, WorkspaceStatus.values().length);
        assertEquals(WorkspaceStatus.ACTIVE, WorkspaceStatus.valueOf("ACTIVE"));
    }

    @Test
    @DisplayName("Domain records null safety and fallback invariants")
    void testDomainRecordsEdgeCases() {
        WorkspaceProject proj = new WorkspaceProject("p-1", "core", null, true);
        assertEquals("", proj.repositoryUrl());
        assertTrue(proj.isolated());

        assertThrows(NullPointerException.class, () -> new WorkspaceProject(null, "core", "", true));
        assertThrows(NullPointerException.class, () -> new WorkspaceProject("p-1", null, "", true));

        Workspace ws = new Workspace("ws-1", "tenant-1", "OenexaOrg", null, null, null, null);
        assertEquals("default", ws.teamName());
        assertEquals(WorkspaceStatus.ACTIVE, ws.status());
        assertTrue(ws.projects().isEmpty());
        assertNotNull(ws.createdAt());
        assertEquals(0, ws.projectCount());

        assertThrows(NullPointerException.class, () -> new Workspace(null, "t", "o", "tm", WorkspaceStatus.ACTIVE, List.of(), null));
        assertThrows(NullPointerException.class, () -> new Workspace("w", null, "o", "tm", WorkspaceStatus.ACTIVE, List.of(), null));
        assertThrows(NullPointerException.class, () -> new Workspace("w", "t", null, "tm", WorkspaceStatus.ACTIVE, List.of(), null));

        CreateWorkspaceRequest req = new CreateWorkspaceRequest("tenant-1", "OenexaOrg", null, null);
        assertEquals("engineering", req.teamName());
        assertTrue(req.initialProjectNames().isEmpty());

        assertThrows(NullPointerException.class, () -> new CreateWorkspaceRequest(null, "org", "team", List.of()));
        assertThrows(NullPointerException.class, () -> new CreateWorkspaceRequest("t", null, "team", List.of()));
    }

    @Test
    @DisplayName("Create workspace with initial projects and verify retrieval")
    void testCreateAndGetWorkspace() {
        CreateWorkspaceRequest req = new CreateWorkspaceRequest(
                "tenant-alpha",
                "AcmeCorp",
                "PlatformTeam",
                List.of("backend", "frontend")
        );
        Workspace ws = workspaceService.createWorkspace(req);

        assertNotNull(ws.id());
        assertEquals("tenant-alpha", ws.tenantId());
        assertEquals("AcmeCorp", ws.orgName());
        assertEquals(2, ws.projectCount());

        Workspace retrieved = workspaceService.getWorkspace(ws.id());
        assertEquals(ws.id(), retrieved.id());

        assertThrows(IllegalArgumentException.class, () -> workspaceService.getWorkspace("nonexistent-ws"));
    }

    @Test
    @DisplayName("Add project to workspace")
    void testAddProject() {
        CreateWorkspaceRequest req = new CreateWorkspaceRequest("tenant-beta", "BetaOrg", "DevTeam", List.of());
        Workspace ws = workspaceService.createWorkspace(req);
        assertEquals(0, ws.projectCount());

        Workspace updated = workspaceService.addProject(ws.id(), "new-service", "https://github.com/BetaOrg/new-service");
        assertEquals(1, updated.projectCount());
        assertEquals("new-service", updated.projects().get(0).name());
    }

    @Test
    @DisplayName("Isolate workspace projects")
    void testIsolateWorkspace() {
        CreateWorkspaceRequest req = new CreateWorkspaceRequest("tenant-gamma", "GammaOrg", "SecurityTeam", List.of("microservice"));
        Workspace ws = workspaceService.createWorkspace(req);

        assertTrue(workspaceService.isolateWorkspace(ws.id()));
        assertFalse(workspaceService.isolateWorkspace("nonexistent"));

        Workspace isolated = workspaceService.getWorkspace(ws.id());
        assertTrue(isolated.projects().get(0).isolated());
    }

    @Test
    @DisplayName("List workspaces for tenant and null safety")
    void testListWorkspacesForTenant() {
        workspaceService.createWorkspace(new CreateWorkspaceRequest("tenant-x", "OrgX", "Team1", List.of()));
        workspaceService.createWorkspace(new CreateWorkspaceRequest("tenant-x", "OrgX", "Team2", List.of()));
        workspaceService.createWorkspace(new CreateWorkspaceRequest("tenant-y", "OrgY", "Team1", List.of()));

        List<Workspace> xWorkspaces = workspaceService.listWorkspacesForTenant("tenant-x");
        assertEquals(2, xWorkspaces.size());

        List<Workspace> yWorkspaces = workspaceService.listWorkspacesForTenant("tenant-y");
        assertEquals(1, yWorkspaces.size());

        assertTrue(workspaceService.listWorkspacesForTenant(null).isEmpty());
    }

    @Test
    @DisplayName("WorkspaceController endpoints return valid HTTP responses")
    void testWorkspaceControllerEndpoints() {
        CreateWorkspaceRequest req = new CreateWorkspaceRequest("t-ctrl", "CtrlOrg", "Dev", List.of("p1"));
        ResponseEntity<Workspace> createResp = workspaceController.create(req);
        assertEquals(200, createResp.getStatusCode().value());
        Workspace ws = createResp.getBody();
        assertNotNull(ws);

        ResponseEntity<Workspace> getResp = workspaceController.get(ws.id());
        assertEquals(200, getResp.getStatusCode().value());
        assertEquals(ws.id(), getResp.getBody().id());

        ResponseEntity<Workspace> addResp = workspaceController.addProject(ws.id(), Map.of("name", "p2", "repositoryUrl", "git://repo"));
        assertEquals(200, addResp.getStatusCode().value());
        assertEquals(2, addResp.getBody().projectCount());

        ResponseEntity<Map<String, Boolean>> isoResp = workspaceController.isolate(ws.id());
        assertEquals(200, isoResp.getStatusCode().value());
        assertTrue(isoResp.getBody().get("isolated"));

        ResponseEntity<List<Workspace>> listResp = workspaceController.listByTenant("t-ctrl");
        assertEquals(200, listResp.getStatusCode().value());
        assertEquals(1, listResp.getBody().size());
    }
}
