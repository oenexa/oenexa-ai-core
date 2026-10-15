package ai.oenexa.core.persistence;

import ai.oenexa.core.domain.CoreUser;
import ai.oenexa.core.domain.Organization;
import ai.oenexa.core.domain.OrganizationPort;
import ai.oenexa.core.domain.Project;
import ai.oenexa.core.domain.ProjectPort;
import ai.oenexa.core.domain.Team;
import ai.oenexa.core.domain.TeamPort;
import ai.oenexa.core.domain.UserPort;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data JPA persistence adapter mapping relational entities to immutable domain records.
 */
@Transactional
public class JpaCorePersistenceAdapter implements OrganizationPort, TeamPort, ProjectPort, UserPort {

    private final OrganizationJpaRepository organizationRepo;
    private final TeamJpaRepository teamRepo;
    private final ProjectJpaRepository projectRepo;
    private final UserJpaRepository userRepo;

    public JpaCorePersistenceAdapter(
            OrganizationJpaRepository organizationRepo,
            TeamJpaRepository teamRepo,
            ProjectJpaRepository projectRepo,
            UserJpaRepository userRepo
    ) {
        this.organizationRepo = Objects.requireNonNull(organizationRepo, "organizationRepo must not be null");
        this.teamRepo = Objects.requireNonNull(teamRepo, "teamRepo must not be null");
        this.projectRepo = Objects.requireNonNull(projectRepo, "projectRepo must not be null");
        this.userRepo = Objects.requireNonNull(userRepo, "userRepo must not be null");
    }

    @Override
    public Organization saveOrganization(Organization org) {
        OrganizationEntity entity = organizationRepo.findById(org.id()).orElseGet(OrganizationEntity::new);
        entity.setId(org.id());
        entity.setName(org.name());
        entity.setSlug(org.slug());
        entity.setPlan(org.plan());
        entity.setCreatedAt(org.createdAt());
        entity.setUpdatedAt(org.updatedAt());

        OrganizationEntity saved = organizationRepo.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Organization> findOrganizationById(UUID id) {
        return organizationRepo.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Organization> findOrganizationBySlug(String slug) {
        return organizationRepo.findBySlug(slug).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Organization> findAllOrganizations() {
        return organizationRepo.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public Team saveTeam(Team team) {
        TeamEntity entity = teamRepo.findById(team.id()).orElseGet(TeamEntity::new);
        entity.setId(team.id());
        entity.setOrganizationId(team.organizationId());
        entity.setName(team.name());
        entity.setCreatedAt(team.createdAt());

        TeamEntity saved = teamRepo.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Team> findTeamById(UUID id) {
        return teamRepo.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Team> findTeamsByOrganizationId(UUID organizationId) {
        return teamRepo.findByOrganizationId(organizationId).stream().map(this::toDomain).toList();
    }

    @Override
    public Project saveProject(Project project) {
        ProjectEntity entity = projectRepo.findById(project.id()).orElseGet(ProjectEntity::new);
        entity.setId(project.id());
        entity.setOrganizationId(project.organizationId());
        entity.setTeamId(project.teamId());
        entity.setName(project.name());
        entity.setDescription(project.description());
        entity.setCreatedBy(project.createdBy());
        entity.setCreatedAt(project.createdAt());
        entity.setUpdatedAt(project.updatedAt());

        ProjectEntity saved = projectRepo.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Project> findProjectById(UUID id) {
        return projectRepo.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Project> findProjectsByOrganizationId(UUID organizationId) {
        return projectRepo.findByOrganizationId(organizationId).stream().map(this::toDomain).toList();
    }

    @Override
    public CoreUser saveUser(CoreUser user, String passwordHash) {
        UserEntity entity = userRepo.findById(user.id()).orElseGet(UserEntity::new);
        entity.setId(user.id());
        entity.setOrganizationId(user.organizationId());
        entity.setEmail(user.email());
        entity.setDisplayName(user.displayName());
        entity.setEnabled(user.enabled());
        entity.setMfaEnabled(user.mfaEnabled());
        if (passwordHash != null) {
            entity.setPasswordHash(passwordHash);
        }
        entity.setCreatedAt(user.createdAt());
        entity.setUpdatedAt(user.updatedAt());

        UserEntity saved = userRepo.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CoreUser> findUserById(UUID id) {
        return userRepo.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CoreUser> findUserByEmail(String email) {
        return userRepo.findByEmail(email).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoreUser> findUsersByOrganizationId(UUID organizationId) {
        return userRepo.findByOrganizationId(organizationId).stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> getPasswordHash(UUID userId) {
        return userRepo.findById(userId).map(UserEntity::getPasswordHash);
    }

    private Organization toDomain(OrganizationEntity entity) {
        return new Organization(
                entity.getId(),
                entity.getName(),
                entity.getSlug(),
                entity.getPlan(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private Team toDomain(TeamEntity entity) {
        return new Team(
                entity.getId(),
                entity.getOrganizationId(),
                entity.getName(),
                entity.getCreatedAt()
        );
    }

    private Project toDomain(ProjectEntity entity) {
        return new Project(
                entity.getId(),
                entity.getOrganizationId(),
                entity.getTeamId(),
                entity.getName(),
                entity.getDescription(),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private CoreUser toDomain(UserEntity entity) {
        return new CoreUser(
                entity.getId(),
                entity.getOrganizationId(),
                entity.getEmail(),
                entity.getDisplayName(),
                entity.isEnabled(),
                entity.isMfaEnabled(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
