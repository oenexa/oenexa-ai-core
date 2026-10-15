package ai.oenexa.repository;

import ai.oenexa.common.module.PlannedModule;
import java.util.List;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryModule extends PlannedModule {
    public RepositoryModule() {
        super("repository", "Repository Intelligence", "GitHub/GitLab/Bitbucket connectors, architecture and dependency maps.",
                List.of("clone", "service-map", "dependency-graph", "architecture-report"));
    }
}
