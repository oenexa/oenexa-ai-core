package ai.oenexa.workspace;

import ai.oenexa.common.module.PlannedModule;
import java.util.List;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkspaceModule extends PlannedModule {
    public WorkspaceModule() {
        super("workspace", "OENEXA Workspace", "Organizations, teams, projects and isolated workspaces.",
                List.of("organizations", "teams", "projects", "workspace-isolation"));
    }
}
