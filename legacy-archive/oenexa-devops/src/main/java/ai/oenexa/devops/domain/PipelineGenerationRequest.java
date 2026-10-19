package ai.oenexa.devops.domain;

import java.util.Objects;

/**
 * Request payload for synthesizing CI/CD pipelines.
 */
public record PipelineGenerationRequest(
        String targetPlatform,
        String language,
        String buildTool,
        boolean dockerPublish
) {
    public PipelineGenerationRequest {
        if (targetPlatform == null || targetPlatform.isBlank()) {
            targetPlatform = "github-actions";
        }
        if (language == null || language.isBlank()) {
            language = "java";
        }
        if (buildTool == null || buildTool.isBlank()) {
            buildTool = "gradle";
        }
    }
}
