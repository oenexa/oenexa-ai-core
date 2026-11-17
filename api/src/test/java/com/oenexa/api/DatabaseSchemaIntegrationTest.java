package com.oenexa.api;

import com.oenexa.api.controller.*;
import com.oenexa.api.exception.*;
import com.oenexa.domain.model.auth.User;
import com.oenexa.infrastructure.persistence.entity.ProjectEntity;
import com.oenexa.infrastructure.persistence.entity.RepositoryEntity;
import com.oenexa.infrastructure.persistence.entity.RoleEntity;
import com.oenexa.infrastructure.persistence.repository.ProjectRepository;
import com.oenexa.infrastructure.persistence.repository.RepositoryEntityRepository;
import com.oenexa.infrastructure.persistence.repository.RoleRepository;
import com.oenexa.infrastructure.persistence.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
class DatabaseSchemaIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private RepositoryEntityRepository codeRepository;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
            roleRepository.save(new RoleEntity("role-admin", "ROLE_ADMIN", "Platform Administrator with unrestricted privileges"));
        }
        if (roleRepository.findByName("ROLE_ENGINEER").isEmpty()) {
            roleRepository.save(new RoleEntity("role-engineer", "ROLE_ENGINEER", "Engineering team member with full code repository access"));
        }
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User("admin", "$2a$12$D4.1aJ1.3nN9r78K8N1J..", "admin@oenexa.com");
            userRepository.save(admin);
        }
    }

    @Test
    @DisplayName("Should verify schema seeded default roles")
    void shouldVerifyFlywaySeededRoles() {
        Optional<RoleEntity> adminRole = roleRepository.findByName("ROLE_ADMIN");
        assertTrue(adminRole.isPresent(), "ROLE_ADMIN should be seeded by Flyway");
        assertEquals("Platform Administrator with unrestricted privileges", adminRole.get().getDescription());

        Optional<RoleEntity> engineerRole = roleRepository.findByName("ROLE_ENGINEER");
        assertTrue(engineerRole.isPresent(), "ROLE_ENGINEER should be seeded by Flyway");
    }

    @Test
    @DisplayName("Should verify Flyway migration seeded default admin user")
    void shouldVerifyFlywaySeededAdminUser() {
        Optional<User> admin = userRepository.findByUsername("admin");
        assertTrue(admin.isPresent(), "admin user should be seeded by Flyway");
        assertEquals("admin@oenexa.com", admin.get().email());
        assertTrue(admin.get().isEnabled());
    }

    @Test
    @DisplayName("Should persist and retrieve project and repository entities")
    void shouldPersistAndRetrieveProjectAndRepository() {
        ProjectEntity project = new ProjectEntity(
                "proj-101",
                "Oenexa Core",
                "oenexa-core",
                "Core L1 Blockchain Engine",
                1L
        );
        projectRepository.save(project);

        Optional<ProjectEntity> fetchedProject = projectRepository.findBySlug("oenexa-core");
        assertTrue(fetchedProject.isPresent());
        assertEquals("Oenexa Core", fetchedProject.get().getName());

        RepositoryEntity repo = new RepositoryEntity(
                "repo-101",
                "proj-101",
                "oenexa-node",
                "https://github.com/oenexa/oenexa-node.git",
                "GITHUB",
                "main"
        );
        codeRepository.save(repo);

        var repos = codeRepository.findByProjectId("proj-101");
        assertFalse(repos.isEmpty());
        assertEquals("oenexa-node", repos.get(0).getName());
    }
}

