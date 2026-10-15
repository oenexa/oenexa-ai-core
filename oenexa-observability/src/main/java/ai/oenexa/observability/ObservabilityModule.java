package ai.oenexa.observability;

import ai.oenexa.common.module.PlannedModule;
import java.util.List;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObservabilityModule extends PlannedModule {
    public ObservabilityModule() {
        super("observability", "Log Analytics", "Error grouping, incident detection and root cause analysis over logs.",
                List.of("error-grouping", "incident-timeline", "trend-analysis"));
    }
}
