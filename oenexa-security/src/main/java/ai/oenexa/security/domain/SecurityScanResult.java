package ai.oenexa.security.domain;

import java.util.List;
import java.util.Objects;

/**
 * Aggregated report from SAST and secrets scanners.
 */
public record SecurityScanResult(
        String scanId,
        boolean passed,
        int totalIssues,
        int criticalCount,
        int highCount,
        int mediumCount,
        int lowCount,
        List<VulnerabilityFinding> vulnerabilities,
        List<SecretFinding> secrets,
        String summary
) {
    public SecurityScanResult {
        Objects.requireNonNull(scanId, "scanId must not be null");
        if (vulnerabilities == null) vulnerabilities = List.of();
        if (secrets == null) secrets = List.of();
        if (summary == null) summary = "";
    }
}
