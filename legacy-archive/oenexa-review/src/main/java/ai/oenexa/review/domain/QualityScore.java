package ai.oenexa.review.domain;

/**
 * Quality scores calculated during automated PR review.
 */
public record QualityScore(
        int maintainability,
        int reliability,
        int security,
        int overall
) {
    public QualityScore {
        maintainability = clamp(maintainability);
        reliability = clamp(reliability);
        security = clamp(security);
        overall = clamp(overall);
    }

    private static int clamp(int val) {
        return Math.max(0, Math.min(100, val));
    }

    public boolean isAcceptable() {
        return overall >= 75 && security >= 70;
    }
}
