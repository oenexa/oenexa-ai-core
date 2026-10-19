package ai.oenexa.core.web;

import ai.oenexa.core.domain.CoreUser;
import ai.oenexa.core.domain.Organization;
import ai.oenexa.core.domain.Project;
import ai.oenexa.core.domain.Team;
import ai.oenexa.core.service.CoreTenancyService;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoint exposing multi-tenant organizational hierarchy and core entities.
 */
@RestController
@RequestMapping("/api/v1/core")
public class CoreController {

    private final CoreTenancyService tenancyService;

    public CoreController(CoreTenancyService tenancyService) {
        this.tenancyService = Objects.requireNonNull(tenancyService, "tenancyService must not be null");
    }

    public record CreateOrgRequest(String name, String slug, String plan) {}
    public record CreateTeamRequest(String name) {}
    public record CreateProjectRequest(UUID teamId, String name, String description, UUID createdBy) {}
    public record RegisterUserRequest(String email, String displayName, String password) {}

    @PostMapping("/organizations")
    public ResponseEntity<Organization> createOrganization(@RequestBody CreateOrgRequest request) {
        Organization org = tenancyService.createOrganization(request.name(), request.slug(), request.plan());
        return ResponseEntity.ok(org);
    }

    @GetMapping("/organizations/{id}")
    public ResponseEntity<Organization> getOrganization(@PathVariable UUID id) {
        return ResponseEntity.ok(tenancyService.getOrganization(id));
    }

    @GetMapping("/organizations/slug/{slug}")
    public ResponseEntity<Organization> getOrganizationBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(tenancyService.getOrganizationBySlug(slug));
    }

    @GetMapping("/organizations")
    public ResponseEntity<List<Organization>> listOrganizations() {
        return ResponseEntity.ok(tenancyService.listOrganizations());
    }

    @PostMapping("/organizations/{id}/teams")
    public ResponseEntity<Team> createTeam(@PathVariable UUID id, @RequestBody CreateTeamRequest request) {
        return ResponseEntity.ok(tenancyService.createTeam(id, request.name()));
    }

    @GetMapping("/organizations/{id}/teams")
    public ResponseEntity<List<Team>> listTeams(@PathVariable UUID id) {
        return ResponseEntity.ok(tenancyService.listTeams(id));
    }

    @PostMapping("/organizations/{id}/projects")
    public ResponseEntity<Project> createProject(@PathVariable UUID id, @RequestBody CreateProjectRequest request) {
        Project project = tenancyService.createProject(id, request.teamId(), request.name(), request.description(), request.createdBy());
        return ResponseEntity.ok(project);
    }

    @GetMapping("/organizations/{id}/projects")
    public ResponseEntity<List<Project>> listProjects(@PathVariable UUID id) {
        return ResponseEntity.ok(tenancyService.listProjects(id));
    }

    @PostMapping("/organizations/{id}/users")
    public ResponseEntity<CoreUser> registerUser(@PathVariable UUID id, @RequestBody RegisterUserRequest request) {
        CoreUser user = tenancyService.registerUser(id, request.email(), request.displayName(), request.password());
        return ResponseEntity.ok(user);
    }

    @GetMapping("/organizations/{id}/users/{userId}")
    public ResponseEntity<CoreUser> getUser(@PathVariable UUID id, @PathVariable UUID userId) {
        CoreUser user = tenancyService.getUser(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/organizations/{id}/users")
    public ResponseEntity<List<CoreUser>> listUsers(@PathVariable UUID id) {
        return ResponseEntity.ok(tenancyService.listUsers(id));
    }
}
