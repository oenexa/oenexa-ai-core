package ai.oenexa.observability.domain;

import java.util.List;

/**
 * Metric summary of ingested logs, errors, and top error groups.
 */
public record LogAnalyticsSummary(
        long totalLogs,
        long errorCount,
        long warnCount,
        int uniqueErrorGroups,
        List<ErrorGroup> topErrorGroups,
        double errorRatePercent
) {
    public LogAnalyticsSummary {
        if (topErrorGroups == null) {
            topErrorGroups = List.of();
        }
    }
}
