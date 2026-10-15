package ai.oenexa.pipeline.domain;

import java.util.List;
import java.util.Objects;

/**
 * Result payload containing diagnoses and repaired configuration.
 */
public record PipelineAnalysisResult(
        String pipelineName,
        CiProvider provider,
        boolean passed,
        List<PipelineFailureDiagnosis> diagnoses,
        String repairedConfig,
        String summary
) {
    public PipelineAnalysisResult {
        Objects.requireNonNull(pipelineName, "pipelineName must not be null");
        Objects.requireNonNull(provider, "provider must not be null");
        if (diagnoses == null) {
            diagnoses = List.of();
        }
        if (repairedConfig == null) {
            repairedConfig = "";
        }
        if (summary == null) {
            summary = "";
        }
    }

    public boolean hasFailures() {
        return !diagnoses.isEmpty();
    }
}
