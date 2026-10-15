package ai.oenexa.testing.domain;

import java.util.Objects;

/**
 * Metric analysis comparing achieved coverage against quality gates.
 */
public record CoverageReport(
        String moduleName,
        int linesTotal,
        int linesCovered,
        int branchesTotal,
        int branchesCovered,
        double lineCoveragePercent,
        double branchCoveragePercent,
        boolean passed
) {
    public CoverageReport {
        Objects.requireNonNull(moduleName, "moduleName must not be null");
    }

    public static CoverageReport of(String moduleName, int linesTotal, int linesCovered, int branchesTotal, int branchesCovered, double thresholdPercent) {
        double linePct = linesTotal > 0 ? (linesCovered * 100.0) / linesTotal : 100.0;
        double branchPct = branchesTotal > 0 ? (branchesCovered * 100.0) / branchesTotal : 100.0;
        boolean passed = linePct >= thresholdPercent && branchPct >= (thresholdPercent > 20.0 ? thresholdPercent - 10.0 : 0.0);
        return new CoverageReport(
                moduleName,
                linesTotal,
                linesCovered,
                branchesTotal,
                branchesCovered,
                Math.round(linePct * 100.0) / 100.0,
                Math.round(branchPct * 100.0) / 100.0,
                passed
        );
    }
}
