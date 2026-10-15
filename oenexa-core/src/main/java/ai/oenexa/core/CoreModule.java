package ai.oenexa.core;

import ai.oenexa.common.module.PlatformModule;
import ai.oenexa.core.domain.OrganizationPort;
import ai.oenexa.core.domain.ProjectPort;
import ai.oenexa.core.domain.TeamPort;
import ai.oenexa.core.domain.UserPort;
import ai.oenexa.core.persistence.InMemoryCorePersistenceAdapter;
import ai.oenexa.core.service.CoreTenancyService;
import ai.oenexa.core.service.DefaultCoreTenancyService;
import ai.oenexa.core.service.PasswordHasher;
import ai.oenexa.core.web.CoreController;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Core Relational Kernel Platform Module definition.
 */
@Configuration
@Import(CoreController.class)
public class CoreModule implements PlatformModule {

    @Bean
    @ConditionalOnMissingBean(InMemoryCorePersistenceAdapter.class)
    public InMemoryCorePersistenceAdapter inMemoryCorePersistenceAdapter() {
        return new InMemoryCorePersistenceAdapter();
    }

    @Bean
    @ConditionalOnMissingBean(OrganizationPort.class)
    public OrganizationPort organizationPort(InMemoryCorePersistenceAdapter adapter) {
        return adapter;
    }

    @Bean
    @ConditionalOnMissingBean(TeamPort.class)
    public TeamPort teamPort(InMemoryCorePersistenceAdapter adapter) {
        return adapter;
    }

    @Bean
    @ConditionalOnMissingBean(ProjectPort.class)
    public ProjectPort projectPort(InMemoryCorePersistenceAdapter adapter) {
        return adapter;
    }

    @Bean
    @ConditionalOnMissingBean(UserPort.class)
    public UserPort userPort(InMemoryCorePersistenceAdapter adapter) {
        return adapter;
    }

    @Bean
    @ConditionalOnMissingBean(PasswordHasher.class)
    public PasswordHasher passwordHasher() {
        return raw -> "hash_" + raw;
    }

    @Bean
    @ConditionalOnMissingBean(CoreTenancyService.class)
    public CoreTenancyService coreTenancyService(
            OrganizationPort organizationPort,
            TeamPort teamPort,
            ProjectPort projectPort,
            UserPort userPort,
            PasswordHasher passwordHasher
    ) {
        return new DefaultCoreTenancyService(organizationPort, teamPort, projectPort, userPort, passwordHasher);
    }

    @Override
    public String id() {
        return "core";
    }

    @Override
    public String displayName() {
        return "Core Domain Kernel";
    }

    @Override
    public String description() {
        return "Relational tenancy kernel: organizations, teams, projects, users and schema migrations.";
    }

    @Override
    public List<String> capabilities() {
        return List.of("organizations", "teams", "projects", "users", "relational-persistence");
    }

    @Override
    public ModuleStatus status() {
        return ModuleStatus.ACTIVE;
    }
}
