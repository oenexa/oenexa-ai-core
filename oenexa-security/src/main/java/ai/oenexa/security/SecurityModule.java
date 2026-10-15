package ai.oenexa.security;

import ai.oenexa.common.module.PlannedModule;
import java.util.List;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityModule extends PlannedModule {
    public SecurityModule() {
        super("security", "OENEXA Security Center", "SAST, DAST, dependency and secrets scanning with auto-fixes.",
                List.of("sast", "dast", "dependency-scan", "secrets-detection", "owasp-top-10"));
    }
}
