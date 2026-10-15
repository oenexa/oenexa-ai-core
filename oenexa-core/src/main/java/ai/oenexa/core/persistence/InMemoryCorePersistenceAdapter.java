package ai.oenexa.core.persistence;

import ai.oenexa.core.domain.CoreUser;
import ai.oenexa.core.domain.Organization;
import ai.oenexa.core.domain.OrganizationPort;
import ai.oenexa.core.domain.Project;
import ai.oenexa.core.domain.ProjectPort;
import ai.oenexa.core.domain.Team;
import ai.oenexa.core.domain.TeamPort;
import ai.oenexa.core.domain.UserPort;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-performance, zero-mock thread-safe in-memory adapter for testing and lightweight local execution.
 */
public class InMemoryCorePersistenceAdapter implements OrganizationPort, TeamPort, ProjectPort, UserPort {

    private final Map<UUID, Organization> organizations = new ConcurrentHashMap<>();
    private final Map<String, UUID> orgSlugIndex = new ConcurrentHashMap<>();

    private final Map<UUID, Team> teams = new ConcurrentHashMap<>();
    private final Map<UUID, Project> projects = new ConcurrentHashMap<>();

    private final Map<UUID, CoreUser> users = new ConcurrentHashMap<>();
    private final Map<String, UUID> userEmailIndex = new ConcurrentHashMap<>();
    private final Map<UUID, String> passwordHashes = new ConcurrentHashMap<>();

    @Override
    public Organization saveOrganization(Organization org) {
        Objects.requireNonNull(org, "Organization must not be null");
        organizations.put(org.id(), org);
        orgSlugIndex.put(org.slug().toLowerCase(), org.id());
        return org;
    }

    @Override
    public Optional<Organization> findOrganizationById(UUID id) {
        return Optional.ofNullable(organizations.get(id));
    }

    @Override
    public Optional<Organization> findOrganizationBySlug(String slug) {
        if (slug == null) return Optional.empty();
        UUID id = orgSlugIndex.get(slug.toLowerCase());
        return id != null ? Optional.ofNullable(organizations.get(id)) : Optional.empty();
    }

    @Override
    public List<Organization> findAllOrganizations() {
        return organizations.values().stream()
                .sorted(Comparator.comparing(Organization::createdAt))
                .toList();
    }

    @Override
    public Team saveTeam(Team team) {
        Objects.requireNonNull(team, "Team must not be null");
        teams.put(team.id(), team);
        return team;
    }

    @Override
    public Optional<Team> findTeamById(UUID id) {
        return Optional.ofNullable(teams.get(id));
    }

    @Override
    public List<Team> findTeamsByOrganizationId(UUID organizationId) {
        return teams.values().stream()
                .filter(t -> t.organizationId().equals(organizationId))
                .sorted(Comparator.comparing(Team::createdAt))
                .toList();
    }

    @Override
    public Project saveProject(Project project) {
        Objects.requireNonNull(project, "Project must not be null");
        projects.put(project.id(), project);
        return project;
    }

    @Override
    public Optional<Project> findProjectById(UUID id) {
        return Optional.ofNullable(projects.get(id));
    }

    @Override
    public List<Project> findProjectsByOrganizationId(UUID organizationId) {
        return projects.values().stream()
                .filter(p -> p.organizationId().equals(organizationId))
                .sorted(Comparator.comparing(Project::createdAt))
                .toList();
    }

    @Override
    public CoreUser saveUser(CoreUser user, String passwordHash) {
        Objects.requireNonNull(user, "User must not be null");
        users.put(user.id(), user);
        userEmailIndex.put(user.email().toLowerCase(), user.id());
        if (passwordHash != null) {
            passwordHashes.put(user.id(), passwordHash);
        }
        return user;
    }

    @Override
    public Optional<CoreUser> findUserById(UUID id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Optional<CoreUser> findUserByEmail(String email) {
        if (email == null) return Optional.empty();
        UUID id = userEmailIndex.get(email.toLowerCase());
        return id != null ? Optional.ofNullable(users.get(id)) : Optional.empty();
    }

    @Override
    public List<CoreUser> findUsersByOrganizationId(UUID organizationId) {
        return users.values().stream()
                .filter(u -> u.organizationId().equals(organizationId))
                .sorted(Comparator.comparing(CoreUser::createdAt))
                .toList();
    }

    @Override
    public Optional<String> getPasswordHash(UUID userId) {
        return Optional.ofNullable(passwordHashes.get(userId));
    }

    public void clear() {
        organizations.clear();
        orgSlugIndex.clear();
        teams.clear();
        projects.clear();
        users.clear();
        userEmailIndex.clear();
        passwordHashes.clear();
    }
}
