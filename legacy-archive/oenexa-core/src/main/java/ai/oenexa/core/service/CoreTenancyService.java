package ai.oenexa.core.service;

import ai.oenexa.core.domain.CoreUser;
import ai.oenexa.core.domain.Organization;
import ai.oenexa.core.domain.Project;
import ai.oenexa.core.domain.Team;
import java.util.List;
import java.util.UUID;

public interface CoreTenancyService {
    Organization createOrganization(String name, String slug, String plan);
    Organization getOrganization(UUID id);
    Organization getOrganizationBySlug(String slug);
    List<Organization> listOrganizations();

    Team createTeam(UUID organizationId, String name);
    List<Team> listTeams(UUID organizationId);

    Project createProject(UUID organizationId, UUID teamId, String name, String description, UUID createdBy);
    List<Project> listProjects(UUID organizationId);

    CoreUser registerUser(UUID organizationId, String email, String displayName, String password);
    CoreUser getUser(UUID userId);
    CoreUser getUserByEmail(String email);
    List<CoreUser> listUsers(UUID organizationId);
}
