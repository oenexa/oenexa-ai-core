package ai.oenexa.pipeline;

import ai.oenexa.common.module.PlannedModule;
import java.util.List;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PipelineModule extends PlannedModule {
    public PipelineModule() {
        super("pipeline", "CI/CD Engine", "Failed-pipeline root cause analysis for GitHub Actions, Jenkins, GitLab CI, Azure.",
                List.of("failure-analysis", "pipeline-repair"));
    }
}
