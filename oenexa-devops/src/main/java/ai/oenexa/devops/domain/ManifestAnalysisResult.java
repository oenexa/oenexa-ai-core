package ai.oenexa.devops.domain;

import java.util.List;
import java.util.Objects;

/**
 * Result of DevOps manifest evaluation with health score and remediation.
 */
public record ManifestAnalysisResult(
        String filename,
        ManifestType type,
        boolean healthy,
        int healthScore,
        List<DevopsFinding> findings,
        String summary
) {
    public ManifestAnalysisResult {
        Objects.requireNonNull(filename, "filename must not be null");
        Objects.requireNonNull(type, "type must not be null");
        if (findings == null) {
            findings = List.of();
        }
        if (summary == null) {
            summary = "";
        }
    }

    public long criticalCount() {
        return findings.stream().filter(f -> f.severity() == DiagnosisSeverity.CRITICAL).count();
    }

    public long warningCount() {
        return findings.stream().filter(f -> f.severity() == DiagnosisSeverity.WARNING).count();
    }
}
