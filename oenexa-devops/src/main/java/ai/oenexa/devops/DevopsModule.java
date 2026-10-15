package ai.oenexa.devops;

import ai.oenexa.common.module.PlannedModule;
import java.util.List;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DevopsModule extends PlannedModule {
    public DevopsModule() {
        super("devops", "DevOps Center", "Docker, Compose, Kubernetes and Helm analysis with proposed fixes.",
                List.of("dockerfile-analysis", "k8s-diagnosis", "helm-analysis"));
    }
}
