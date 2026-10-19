package ai.oenexa.documentation;

import ai.oenexa.common.module.PlannedModule;
import java.util.List;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentationModule extends PlannedModule {
    public DocumentationModule() {
        super("documentation", "OENEXA Documentation", "Generated technical docs, diagrams and API references.",
                List.of("architecture-docs", "diagram-generation", "api-docs"));
    }
}
