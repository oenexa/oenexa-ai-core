package ai.oenexa.search;

import ai.oenexa.common.module.PlannedModule;
import java.util.List;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SearchModule extends PlannedModule {
    public SearchModule() {
        super("search", "OENEXA Search", "Elasticsearch full-text search plus web research with citations.",
                List.of("full-text", "hybrid-search", "web-search"));
    }
}
