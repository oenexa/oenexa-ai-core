package ai.oenexa.review;

import ai.oenexa.common.module.PlannedModule;
import java.util.List;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReviewModule extends PlannedModule {
    public ReviewModule() {
        super("review", "PR Review Engine", "Risk, quality and security scoring with inline review comments.",
                List.of("risk-score", "quality-score", "security-score", "review-comments"));
    }
}
