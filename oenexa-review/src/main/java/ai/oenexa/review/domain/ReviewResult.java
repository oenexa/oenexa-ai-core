package ai.oenexa.review.domain;

import java.util.List;
import java.util.Objects;

/**
 * Complete review report for a pull request or code change.
 */
public record ReviewResult(
        String prId,
        boolean approved,
        String summary,
        QualityScore qualityScore,
        RiskScore riskScore,
        List<ReviewComment> comments
) {
    public ReviewResult {
        Objects.requireNonNull(prId, "prId must not be null");
        Objects.requireNonNull(summary, "summary must not be null");
        Objects.requireNonNull(qualityScore, "qualityScore must not be null");
        Objects.requireNonNull(riskScore, "riskScore must not be null");
        if (comments == null) {
            comments = List.of();
        }
    }

    public long errorCount() {
        return comments.stream().filter(c -> c.severity() == ReviewComment.Severity.ERROR).count();
    }

    public long warningCount() {
        return comments.stream().filter(c -> c.severity() == ReviewComment.Severity.WARNING).count();
    }
}
