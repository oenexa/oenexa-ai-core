package ai.oenexa.pipeline.domain;

import java.util.Objects;

/**
 * Diagnosis report pinpointing root cause of CI failure with proposed repair.
 */
public record PipelineFailureDiagnosis(
        String failureId,
        FailureCategory category,
        String rootCause,
        String failedStep,
        String proposedFix,
        int confidencePercent
) {
    public PipelineFailureDiagnosis {
        Objects.requireNonNull(failureId, "failureId must not be null");
        Objects.requireNonNull(category, "category must not be null");
        Objects.requireNonNull(rootCause, "rootCause must not be null");
        if (failedStep == null) {
            failedStep = "unknown";
        }
        if (proposedFix == null) {
            proposedFix = "";
        }
    }
}
