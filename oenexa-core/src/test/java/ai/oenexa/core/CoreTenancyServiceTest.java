package ai.oenexa.core;

import ai.oenexa.common.error.ErrorCode;
import ai.oenexa.common.error.OenexaException;
import ai.oenexa.common.module.PlatformModule;
import ai.oenexa.core.domain.CoreUser;
import ai.oenexa.core.domain.Organization;
import ai.oenexa.core.domain.Project;
import ai.oenexa.core.domain.Team;
import ai.oenexa.core.persistence.InMemoryCorePersistenceAdapter;
import ai.oenexa.core.persistence.JpaCorePersistenceAdapter;
import ai.oenexa.core.persistence.OrganizationEntity;
import ai.oenexa.core.persistence.OrganizationJpaRepository;
import ai.oenexa.core.persistence.ProjectEntity;
import ai.oenexa.core.persistence.ProjectJpaRepository;
import ai.oenexa.core.persistence.TeamEntity;
import ai.oenexa.core.persistence.TeamJpaRepository;
import ai.oenexa.core.persistence.UserEntity;
import ai.oenexa.core.persistence.UserJpaRepository;
import ai.oenexa.core.service.DefaultCoreTenancyService;
import ai.oenexa.core.service.PasswordHasher;
import ai.oenexa.core.web.CoreController;
import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("oenexa-core | 100% Zero-Mock Tenancy Kernel Tests")
class CoreTenancyServiceTest {

    private InMemoryCorePersistenceAdapter adapter;
    private DefaultCoreTenancyService service;
    private CoreController controller;

    @BeforeEach
    void setUp() {
        adapter = new InMemoryCorePersistenceAdapter();
        PasswordHasher hasher = raw -> "hashed_" + raw;
        service = new DefaultCoreTenancyService(adapter, adapter, adapter, adapter, hasher);
        controller = new CoreController(service);
    }

    @Test
    @DisplayName("Domain Record Invariants & Factory Methods")
    void domainRecordInvariants() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        // Organization
        Organization org = new Organization(id, "Oenexa Inc", "oenexa", "ENTERPRISE", now, now);
        assertEquals(id, org.id());
        assertEquals("Oenexa Inc", org.name());
        assertEquals("oenexa", org.slug());
        assertEquals("ENTERPRISE", org.plan());
        assertEquals(now, org.createdAt());
        assertEquals(now, org.updatedAt());

        Organization defaultOrg = Organization.create("Acme", "acme", null);
        assertNotNull(defaultOrg.id());
        assertEquals("FREE", defaultOrg.plan());

        assertThrows(NullPointerException.class, () -> new Organization(null, null, "slug", "FREE", now, now));
        assertThrows(NullPointerException.class, () -> new Organization(null, "Name", null, "FREE", now, now));

        // Team
        Team team = new Team(id, org.id(), "Platform Team", now);
        assertEquals(id, team.id());
        assertEquals(org.id(), team.organizationId());
        assertEquals("Platform Team", team.name());
        assertEquals(now, team.createdAt());

        Team createdTeam = Team.create(org.id(), "DevOps");
        assertNotNull(createdTeam.id());
        assertEquals(org.id(), createdTeam.organizationId());

        assertThrows(NullPointerException.class, () -> new Team(null, null, "Name", now));
        assertThrows(NullPointerException.class, () -> new Team(null, org.id(), null, now));

        // Project
        Project project = new Project(id, org.id(), team.id(), "Kernel", "Core subsystem", id, now, now);
        assertEquals(id, project.id());
        assertEquals(org.id(), project.organizationId());
        assertEquals(team.id(), project.teamId());
        assertEquals("Kernel", project.name());
        assertEquals("Core subsystem", project.description());

        Project defaultProj = Project.create(org.id(), null, "AI", null, id);
        assertNotNull(defaultProj.id());
        assertEquals("", defaultProj.description());

        assertThrows(NullPointerException.class, () -> new Project(null, null, null, "P", "", id, now, now));
        assertThrows(NullPointerException.class, () -> new Project(null, org.id(), null, null, "", id, now, now));

        // CoreUser
        CoreUser user = new CoreUser(id, org.id(), "dev@oenexa.com", "Dev User", true, false, now, now);
        assertEquals(id, user.id());
        assertEquals("dev@oenexa.com", user.email());
        assertEquals("Dev User", user.displayName());
        assertTrue(user.enabled());
        assertFalse(user.mfaEnabled());

        CoreUser createdUser = CoreUser.create(org.id(), "admin@oenexa.com", null);
        assertNotNull(createdUser.id());
        assertEquals("admin@oenexa.com", createdUser.displayName());

        assertThrows(NullPointerException.class, () -> new CoreUser(null, null, "a@b.com", "D", true, false, now, now));
        assertThrows(NullPointerException.class, () -> new CoreUser(null, org.id(), null, "D", true, false, now, now));
    }

    @Test
    @DisplayName("Organization Service Operations & Error Handlers")
    void organizationServiceOperations() {
        Organization org = service.createOrganization("Oenexa Global", "oenexa-global", "PRO");
        assertNotNull(org.id());
        assertEquals("oenexa-global", org.slug());

        // Slug lookup
        Organization bySlug = service.getOrganizationBySlug("OENEXA-GLOBAL");
        assertEquals(org.id(), bySlug.id());

        // ID lookup
        Organization byId = service.getOrganization(org.id());
        assertEquals(org.id(), byId.id());

        // List
        List<Organization> orgs = service.listOrganizations();
        assertEquals(1, orgs.size());

        // Validation errors
        assertThrows(OenexaException.class, () -> service.createOrganization("", "slug", "FREE"));
        assertThrows(OenexaException.class, () -> service.createOrganization("Name", "", "FREE"));
        assertThrows(OenexaException.class, () -> service.createOrganization("Name", "oenexa-global", "FREE")); // Duplicate
        assertThrows(OenexaException.class, () -> service.getOrganizationBySlug(""));
        assertThrows(OenexaException.class, () -> service.getOrganizationBySlug("missing"));
        assertThrows(OenexaException.class, () -> service.getOrganization(UUID.randomUUID()));
    }

    @Test
    @DisplayName("Team Service Operations & Error Handlers")
    void teamServiceOperations() {
        Organization org = service.createOrganization("Oenexa Org", "org-team-test", "FREE");

        Team team = service.createTeam(org.id(), "AI Engineers");
        assertNotNull(team.id());
        assertEquals("AI Engineers", team.name());

        List<Team> teams = service.listTeams(org.id());
        assertEquals(1, teams.size());
        assertEquals(team.id(), teams.getFirst().id());

        assertThrows(OenexaException.class, () -> service.createTeam(UUID.randomUUID(), "Team"));
        assertThrows(OenexaException.class, () -> service.createTeam(org.id(), ""));
        assertThrows(OenexaException.class, () -> service.listTeams(UUID.randomUUID()));
    }

    @Test
    @DisplayName("Project Service Operations & Error Handlers")
    void projectServiceOperations() {
        Organization org = service.createOrganization("Oenexa Org", "org-proj-test", "FREE");
        Team team = service.createTeam(org.id(), "Engineering");
        UUID userId = UUID.randomUUID();

        Project p1 = service.createProject(org.id(), team.id(), "Subsystem A", "Desc A", userId);
        assertNotNull(p1.id());
        assertEquals("Subsystem A", p1.name());

        Project p2 = service.createProject(org.id(), null, "Subsystem B", null, userId);
        assertNotNull(p2.id());
        assertNull(p2.teamId());

        List<Project> list = service.listProjects(org.id());
        assertEquals(2, list.size());

        assertThrows(OenexaException.class, () -> service.createProject(UUID.randomUUID(), null, "P", "", userId));
        assertThrows(OenexaException.class, () -> service.createProject(org.id(), null, "", "", userId));
        assertThrows(OenexaException.class, () -> service.createProject(org.id(), UUID.randomUUID(), "P", "", userId));
        assertThrows(OenexaException.class, () -> service.listProjects(UUID.randomUUID()));
    }

    @Test
    @DisplayName("User Registration & Authentication Password Hashing")
    void userServiceOperations() {
        Organization org = service.createOrganization("Oenexa Org", "org-user-test", "FREE");

        CoreUser user = service.registerUser(org.id(), "alice@oenexa.com", "Alice", "SuperSecret123!");
        assertNotNull(user.id());
        assertEquals("alice@oenexa.com", user.email());

        // Password hash check
        Optional<String> hash = adapter.getPasswordHash(user.id());
        assertTrue(hash.isPresent());
        assertNotEquals("SuperSecret123!", hash.get());

        // Lookups
        CoreUser byId = service.getUser(user.id());
        assertEquals(user.id(), byId.id());

        CoreUser byEmail = service.getUserByEmail("ALICE@OENEXA.COM");
        assertEquals(user.id(), byEmail.id());

        List<CoreUser> list = service.listUsers(org.id());
        assertEquals(1, list.size());

        // Validation errors
        assertThrows(OenexaException.class, () -> service.registerUser(UUID.randomUUID(), "a@b.com", "A", "pass1234"));
        assertThrows(OenexaException.class, () -> service.registerUser(org.id(), "not-an-email", "A", "pass1234"));
        assertThrows(OenexaException.class, () -> service.registerUser(org.id(), "alice@oenexa.com", "A", "pass1234")); // duplicate
        assertThrows(OenexaException.class, () -> service.registerUser(org.id(), "bob@oenexa.com", "B", "short")); // < 8
        assertThrows(OenexaException.class, () -> service.getUser(UUID.randomUUID()));
        assertThrows(OenexaException.class, () -> service.getUserByEmail(""));
        assertThrows(OenexaException.class, () -> service.getUserByEmail("missing@oenexa.com"));
        assertThrows(OenexaException.class, () -> service.listUsers(UUID.randomUUID()));
    }

    @Test
    @DisplayName("Custom PasswordHasher Integration")
    void customPasswordHasher() {
        PasswordHasher customHasher = rawPassword -> "custom_hashed_" + rawPassword;
        assertTrue(customHasher.verify("test", "custom_hashed_test"));
        assertFalse(customHasher.verify(null, "hash"));
        assertFalse(customHasher.verify("test", null));
        assertFalse(customHasher.verify("test", "wrong"));

        DefaultCoreTenancyService encService = new DefaultCoreTenancyService(adapter, adapter, adapter, adapter, customHasher);
        Organization org = encService.createOrganization("Enc Org", "enc-org", "PRO");
        CoreUser user = encService.registerUser(org.id(), "charlie@oenexa.com", "Charlie", "pass12345");

        assertEquals("custom_hashed_pass12345", adapter.getPasswordHash(user.id()).orElse(""));
    }

    @Test
    @DisplayName("InMemoryCorePersistenceAdapter Full Coverage")
    void inMemoryAdapterCoverage() {
        InMemoryCorePersistenceAdapter mem = new InMemoryCorePersistenceAdapter();
        assertTrue(mem.findOrganizationBySlug(null).isEmpty());
        assertTrue(mem.findOrganizationBySlug("none").isEmpty());
        assertTrue(mem.findUserByEmail(null).isEmpty());
        assertTrue(mem.findUserByEmail("none").isEmpty());
        assertTrue(mem.findOrganizationById(UUID.randomUUID()).isEmpty());
        assertTrue(mem.findTeamById(UUID.randomUUID()).isEmpty());
        assertTrue(mem.findProjectById(UUID.randomUUID()).isEmpty());
        assertTrue(mem.findUserById(UUID.randomUUID()).isEmpty());
        assertTrue(mem.getPasswordHash(UUID.randomUUID()).isEmpty());

        Organization org = mem.saveOrganization(Organization.create("A", "a", "FREE"));
        Team team = mem.saveTeam(Team.create(org.id(), "T"));
        Project proj = mem.saveProject(Project.create(org.id(), team.id(), "P", "D", UUID.randomUUID()));
        CoreUser user = mem.saveUser(CoreUser.create(org.id(), "u@a.com", "U"), null);

        assertTrue(mem.findOrganizationById(org.id()).isPresent());
        assertTrue(mem.findOrganizationBySlug("a").isPresent());
        assertEquals(1, mem.findAllOrganizations().size());

        assertTrue(mem.findTeamById(team.id()).isPresent());
        assertEquals(1, mem.findTeamsByOrganizationId(org.id()).size());

        assertTrue(mem.findProjectById(proj.id()).isPresent());
        assertEquals(1, mem.findProjectsByOrganizationId(org.id()).size());

        assertTrue(mem.findUserById(user.id()).isPresent());
        assertTrue(mem.findUserByEmail("u@a.com").isPresent());
        assertEquals(1, mem.findUsersByOrganizationId(org.id()).size());

        mem.clear();
        assertEquals(0, mem.findAllOrganizations().size());
    }

    @Test
    @DisplayName("JpaCorePersistenceAdapter Dynamic Proxy Coverage")
    void jpaAdapterCoverage() {
        Map<UUID, OrganizationEntity> orgMap = new HashMap<>();
        Map<UUID, TeamEntity> teamMap = new HashMap<>();
        Map<UUID, ProjectEntity> projMap = new HashMap<>();
        Map<UUID, UserEntity> userMap = new HashMap<>();

        OrganizationJpaRepository orgRepo = (OrganizationJpaRepository) Proxy.newProxyInstance(
                OrganizationJpaRepository.class.getClassLoader(),
                new Class<?>[]{OrganizationJpaRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if (name.equals("findById")) return Optional.ofNullable(orgMap.get(args[0]));
                    if (name.equals("findBySlug")) return orgMap.values().stream().filter(e -> e.getSlug().equals(args[0])).findFirst();
                    if (name.equals("findAll")) return new ArrayList<>(orgMap.values());
                    if (name.equals("save")) {
                        OrganizationEntity e = (OrganizationEntity) args[0];
                        orgMap.put(e.getId(), e);
                        return e;
                    }
                    return null;
                }
        );

        TeamJpaRepository teamRepo = (TeamJpaRepository) Proxy.newProxyInstance(
                TeamJpaRepository.class.getClassLoader(),
                new Class<?>[]{TeamJpaRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if (name.equals("findById")) return Optional.ofNullable(teamMap.get(args[0]));
                    if (name.equals("findByOrganizationId")) return teamMap.values().stream().filter(e -> e.getOrganizationId().equals(args[0])).toList();
                    if (name.equals("save")) {
                        TeamEntity e = (TeamEntity) args[0];
                        teamMap.put(e.getId(), e);
                        return e;
                    }
                    return null;
                }
        );

        ProjectJpaRepository projRepo = (ProjectJpaRepository) Proxy.newProxyInstance(
                ProjectJpaRepository.class.getClassLoader(),
                new Class<?>[]{ProjectJpaRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if (name.equals("findById")) return Optional.ofNullable(projMap.get(args[0]));
                    if (name.equals("findByOrganizationId")) return projMap.values().stream().filter(e -> e.getOrganizationId().equals(args[0])).toList();
                    if (name.equals("save")) {
                        ProjectEntity e = (ProjectEntity) args[0];
                        projMap.put(e.getId(), e);
                        return e;
                    }
                    return null;
                }
        );

        UserJpaRepository userRepo = (UserJpaRepository) Proxy.newProxyInstance(
                UserJpaRepository.class.getClassLoader(),
                new Class<?>[]{UserJpaRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if (name.equals("findById")) return Optional.ofNullable(userMap.get(args[0]));
                    if (name.equals("findByEmail")) return userMap.values().stream().filter(e -> e.getEmail().equals(args[0])).findFirst();
                    if (name.equals("findByOrganizationId")) return userMap.values().stream().filter(e -> e.getOrganizationId().equals(args[0])).toList();
                    if (name.equals("save")) {
                        UserEntity e = (UserEntity) args[0];
                        userMap.put(e.getId(), e);
                        return e;
                    }
                    return null;
                }
        );

        JpaCorePersistenceAdapter jpa = new JpaCorePersistenceAdapter(orgRepo, teamRepo, projRepo, userRepo);

        // Save & read Organization
        Organization org = Organization.create("JPA Org", "jpa-org", "ENTERPRISE");
        Organization savedOrg = jpa.saveOrganization(org);
        assertEquals(org.id(), savedOrg.id());
        assertTrue(jpa.findOrganizationById(org.id()).isPresent());
        assertTrue(jpa.findOrganizationBySlug("jpa-org").isPresent());
        assertEquals(1, jpa.findAllOrganizations().size());
        // Update Organization
        jpa.saveOrganization(new Organization(org.id(), "JPA Org Renamed", "jpa-org", "PRO", org.createdAt(), Instant.now()));

        // Save & read Team
        Team team = Team.create(org.id(), "JPA Team");
        Team savedTeam = jpa.saveTeam(team);
        assertEquals(team.id(), savedTeam.id());
        assertTrue(jpa.findTeamById(team.id()).isPresent());
        assertEquals(1, jpa.findTeamsByOrganizationId(org.id()).size());
        jpa.saveTeam(new Team(team.id(), org.id(), "JPA Team Renamed", team.createdAt()));

        // Save & read Project
        Project proj = Project.create(org.id(), team.id(), "JPA Proj", "Desc", UUID.randomUUID());
        Project savedProj = jpa.saveProject(proj);
        assertEquals(proj.id(), savedProj.id());
        assertTrue(jpa.findProjectById(proj.id()).isPresent());
        assertEquals(1, jpa.findProjectsByOrganizationId(org.id()).size());
        jpa.saveProject(new Project(proj.id(), org.id(), team.id(), "JPA Proj Up", "D", proj.createdBy(), proj.createdAt(), Instant.now()));

        // Save & read User
        CoreUser user = CoreUser.create(org.id(), "jpa@oenexa.com", "JPA User");
        CoreUser savedUser = jpa.saveUser(user, "hash123");
        assertEquals(user.id(), savedUser.id());
        assertTrue(jpa.findUserById(user.id()).isPresent());
        assertTrue(jpa.findUserByEmail("jpa@oenexa.com").isPresent());
        assertEquals("hash123", jpa.getPasswordHash(user.id()).orElse(""));
        assertEquals(1, jpa.findUsersByOrganizationId(org.id()).size());
        jpa.saveUser(new CoreUser(user.id(), org.id(), "jpa@oenexa.com", "JPA User Up", true, true, user.createdAt(), Instant.now()), null);
    }

    @Test
    @DisplayName("CoreController REST Endpoints")
    void controllerEndpoints() {
        // Create Org
        ResponseEntity<Organization> r1 = controller.createOrganization(new CoreController.CreateOrgRequest("Rest Org", "rest-org", "FREE"));
        assertEquals(200, r1.getStatusCode().value());
        UUID orgId = r1.getBody().id();

        // Get Org
        ResponseEntity<Organization> r2 = controller.getOrganization(orgId);
        assertEquals(200, r2.getStatusCode().value());
        assertEquals("rest-org", r2.getBody().slug());

        // Get Org by slug
        ResponseEntity<Organization> r3 = controller.getOrganizationBySlug("rest-org");
        assertEquals(200, r3.getStatusCode().value());

        // List Orgs
        ResponseEntity<List<Organization>> r4 = controller.listOrganizations();
        assertEquals(200, r4.getStatusCode().value());
        assertEquals(1, r4.getBody().size());

        // Create Team
        ResponseEntity<Team> r5 = controller.createTeam(orgId, new CoreController.CreateTeamRequest("Dev Team"));
        assertEquals(200, r5.getStatusCode().value());
        UUID teamId = r5.getBody().id();

        // List Teams
        ResponseEntity<List<Team>> r6 = controller.listTeams(orgId);
        assertEquals(200, r6.getStatusCode().value());
        assertEquals(1, r6.getBody().size());

        // Create Project
        ResponseEntity<Project> r7 = controller.createProject(orgId, new CoreController.CreateProjectRequest(teamId, "Rest Proj", "Desc", UUID.randomUUID()));
        assertEquals(200, r7.getStatusCode().value());

        // List Projects
        ResponseEntity<List<Project>> r8 = controller.listProjects(orgId);
        assertEquals(200, r8.getStatusCode().value());
        assertEquals(1, r8.getBody().size());

        // Register User
        ResponseEntity<CoreUser> r9 = controller.registerUser(orgId, new CoreController.RegisterUserRequest("rest@oenexa.com", "Rest User", "Secret123!"));
        assertEquals(200, r9.getStatusCode().value());
        UUID userId = r9.getBody().id();

        // Get User
        ResponseEntity<CoreUser> r10 = controller.getUser(orgId, userId);
        assertEquals(200, r10.getStatusCode().value());
        assertEquals("rest@oenexa.com", r10.getBody().email());

        // List Users
        ResponseEntity<List<CoreUser>> r11 = controller.listUsers(orgId);
        assertEquals(200, r11.getStatusCode().value());
        assertEquals(1, r11.getBody().size());
    }

    @Test
    @DisplayName("CoreModule Definition & Spring Bean Factory Configuration")
    void coreModuleMetadataAndBeans() {
        CoreModule module = new CoreModule();
        assertEquals("core", module.id());
        assertEquals("Core Domain Kernel", module.displayName());
        assertTrue(module.description().contains("Relational tenancy kernel"));
        assertEquals(PlatformModule.ModuleStatus.ACTIVE, module.status());
        assertTrue(module.capabilities().contains("organizations"));
        assertTrue(module.capabilities().contains("relational-persistence"));

        InMemoryCorePersistenceAdapter memAdapter = module.inMemoryCorePersistenceAdapter();
        assertNotNull(memAdapter);
        assertNotNull(module.organizationPort(memAdapter));
        assertNotNull(module.teamPort(memAdapter));
        assertNotNull(module.projectPort(memAdapter));
        assertNotNull(module.userPort(memAdapter));
        PasswordHasher defaultHasher = module.passwordHasher();
        assertNotNull(defaultHasher);
        assertEquals("hash_secret", defaultHasher.hash("secret"));
        assertNotNull(module.coreTenancyService(memAdapter, memAdapter, memAdapter, memAdapter, defaultHasher));
    }
}
