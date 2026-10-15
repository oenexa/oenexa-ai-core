package ai.oenexa.admin;

import ai.oenexa.common.module.PlannedModule;
import java.util.List;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminModule extends PlannedModule {
    public AdminModule() {
        super("admin", "OENEXA Admin", "Tenant administration, usage analytics, audit logs and billing.",
                List.of("audit-logs", "usage-analytics", "tenant-admin"));
    }
}
