package ai.oenexa.core.service;

import ai.oenexa.common.error.ErrorCode;
import ai.oenexa.common.error.OenexaException;
import ai.oenexa.core.domain.CoreUser;
import ai.oenexa.core.domain.Organization;
import ai.oenexa.core.domain.OrganizationPort;
import ai.oenexa.core.domain.Project;
import ai.oenexa.core.domain.ProjectPort;
import ai.oenexa.core.domain.Team;
import ai.oenexa.core.domain.TeamPort;
import ai.oenexa.core.domain.UserPort;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Core tenancy implementation managing organizations, teams, projects, and users.
 */
@Service
public class DefaultCoreTenancyService implements CoreTenancyService {

    private final OrganizationPort organizationPort;
    private final TeamPort teamPort;
    private final ProjectPort projectPort;
    private final UserPort userPort;
    private final PasswordHasher passwordHasher;

    public DefaultCoreTenancyService(
            OrganizationPort organizationPort,
            TeamPort teamPort,
            ProjectPort projectPort,
            UserPort userPort,
            PasswordHasher passwordHasher
    ) {
        this.organizationPort = Objects.requireNonNull(organizationPort, "organizationPort must not be null");
        this.teamPort = Objects.requireNonNull(teamPort, "teamPort must not be null");
        this.projectPort = Objects.requireNonNull(projectPort, "projectPort must not be null");
        this.userPort = Objects.requireNonNull(userPort, "userPort must not be null");
        this.passwordHasher = Objects.requireNonNull(passwordHasher, "passwordHasher must not be null");
    }

    @Override
    public Organization createOrganization(String name, String slug, String plan) {
        if (name == null || name.isBlank()) {
            throw new OenexaException(ErrorCode.VALIDATION, "Organization name cannot be blank");
        }
        if (slug == null || slug.isBlank()) {
            throw new OenexaException(ErrorCode.VALIDATION, "Organization slug cannot be blank");
        }
        String cleanSlug = slug.trim().toLowerCase();
        if (organizationPort.findOrganizationBySlug(cleanSlug).isPresent()) {
            throw new OenexaException(ErrorCode.CONFLICT, "Organization slug already exists: " + cleanSlug);
        }

        Organization org = Organization.create(name.trim(), cleanSlug, plan);
        return organizationPort.saveOrganization(org);
    }

    @Override
    public Organization getOrganization(UUID id) {
        Objects.requireNonNull(id, "Organization id must not be null");
        return organizationPort.findOrganizationById(id)
                .orElseThrow(() -> new OenexaException(ErrorCode.NOT_FOUND, "Organization not found: " + id));
    }

    @Override
    public Organization getOrganizationBySlug(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new OenexaException(ErrorCode.VALIDATION, "Slug cannot be blank");
        }
        return organizationPort.findOrganizationBySlug(slug.trim().toLowerCase())
                .orElseThrow(() -> new OenexaException(ErrorCode.NOT_FOUND, "Organization not found for slug: " + slug));
    }

    @Override
    public List<Organization> listOrganizations() {
        return organizationPort.findAllOrganizations();
    }

    @Override
    public Team createTeam(UUID organizationId, String name) {
        getOrganization(organizationId); // verifies existence
        if (name == null || name.isBlank()) {
            throw new OenexaException(ErrorCode.VALIDATION, "Team name cannot be blank");
        }
        Team team = Team.create(organizationId, name.trim());
        return teamPort.saveTeam(team);
    }

    @Override
    public List<Team> listTeams(UUID organizationId) {
        getOrganization(organizationId);
        return teamPort.findTeamsByOrganizationId(organizationId);
    }

    @Override
    public Project createProject(UUID organizationId, UUID teamId, String name, String description, UUID createdBy) {
        getOrganization(organizationId);
        if (name == null || name.isBlank()) {
            throw new OenexaException(ErrorCode.VALIDATION, "Project name cannot be blank");
        }
        if (teamId != null) {
            teamPort.findTeamById(teamId)
                    .orElseThrow(() -> new OenexaException(ErrorCode.NOT_FOUND, "Team not found: " + teamId));
        }

        Project project = Project.create(organizationId, teamId, name.trim(), description, createdBy);
        return projectPort.saveProject(project);
    }

    @Override
    public List<Project> listProjects(UUID organizationId) {
        getOrganization(organizationId);
        return projectPort.findProjectsByOrganizationId(organizationId);
    }

    @Override
    public CoreUser registerUser(UUID organizationId, String email, String displayName, String password) {
        getOrganization(organizationId);
        if (email == null || !email.contains("@")) {
            throw new OenexaException(ErrorCode.VALIDATION, "Invalid email address: " + email);
        }
        String cleanEmail = email.trim().toLowerCase();
        if (userPort.findUserByEmail(cleanEmail).isPresent()) {
            throw new OenexaException(ErrorCode.CONFLICT, "User email already registered: " + cleanEmail);
        }
        if (password == null || password.length() < 8) {
            throw new OenexaException(ErrorCode.VALIDATION, "Password must be at least 8 characters long");
        }

        String hash = hashPassword(password);
        CoreUser user = CoreUser.create(organizationId, cleanEmail, displayName);
        return userPort.saveUser(user, hash);
    }

    @Override
    public CoreUser getUser(UUID userId) {
        Objects.requireNonNull(userId, "User id must not be null");
        return userPort.findUserById(userId)
                .orElseThrow(() -> new OenexaException(ErrorCode.NOT_FOUND, "User not found: " + userId));
    }

    @Override
    public CoreUser getUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new OenexaException(ErrorCode.VALIDATION, "Email cannot be blank");
        }
        return userPort.findUserByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new OenexaException(ErrorCode.NOT_FOUND, "User not found for email: " + email));
    }

    @Override
    public List<CoreUser> listUsers(UUID organizationId) {
        getOrganization(organizationId);
        return userPort.findUsersByOrganizationId(organizationId);
    }

    private String hashPassword(String raw) {
        return passwordHasher.hash(raw);
    }
}
