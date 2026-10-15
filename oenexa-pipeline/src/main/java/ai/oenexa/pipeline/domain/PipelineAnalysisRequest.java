package ai.oenexa.pipeline.domain;

import java.util.Objects;

/**
 * Request payload for analyzing CI/CD logs and broken pipeline definitions.
 */
public record PipelineAnalysisRequest(
        String pipelineName,
        CiProvider provider,
        String buildLogs,
        String pipelineConfig
) {
    public PipelineAnalysisRequest {
        Objects.requireNonNull(pipelineName, "pipelineName must not be null");
        if (provider == null) {
            provider = CiProvider.GITHUB_ACTIONS;
        }
        if (buildLogs == null) {
            buildLogs = "";
        }
        if (pipelineConfig == null) {
            pipelineConfig = "";
        }
    }
}
