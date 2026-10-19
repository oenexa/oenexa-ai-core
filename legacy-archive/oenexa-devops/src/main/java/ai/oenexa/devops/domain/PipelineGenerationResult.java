package ai.oenexa.devops.domain;

import java.util.List;
import java.util.Objects;

/**
 * Result payload containing synthesized CI/CD pipeline definition.
 */
public record PipelineGenerationResult(
        String filename,
        String content,
        List<String> stages
) {
    public PipelineGenerationResult {
        Objects.requireNonNull(filename, "filename must not be null");
        if (content == null) {
            content = "";
        }
        if (stages == null) {
            stages = List.of();
        }
    }
}
