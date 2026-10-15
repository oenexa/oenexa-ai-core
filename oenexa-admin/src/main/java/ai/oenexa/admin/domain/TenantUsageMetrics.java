package ai.oenexa.admin.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * Metric rollup representing compute, tokens, and storage consumed by a tenant.
 */
public record TenantUsageMetrics(
        String tenantId,
        long totalApiCalls,
        long totalTokensConsumed,
        int activeUsers,
        double computeHoursUsed,
        Instant periodStart,
        Instant periodEnd
) {
    public TenantUsageMetrics {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        if (periodStart == null) {
            periodStart = Instant.now().minusSeconds(86400 * 30);
        }
        if (periodEnd == null) {
            periodEnd = Instant.now();
        }
    }
}
