package ai.oenexa.testing;

import ai.oenexa.common.module.PlannedModule;
import java.util.List;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestingModule extends PlannedModule {
    public TestingModule() {
        super("testing", "Automated Test Engineer", "Generates unit, integration, contract, E2E and load tests.",
                List.of("junit5", "mockito", "testcontainers", "wiremock"));
    }
}
