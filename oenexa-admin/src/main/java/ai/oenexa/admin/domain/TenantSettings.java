package ai.oenexa.admin.domain;

import java.util.List;
import java.util.Objects;

/**
 * Enterprise tenant governance settings and security policies.
 */
public record TenantSettings(
        String tenantId,
        boolean mfaRequired,
        int tokenExpiryMinutes,
        int maxConcurrentSessions,
        List<String> allowedIpRanges
) {
    public TenantSettings {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        if (tokenExpiryMinutes <= 0) {
            tokenExpiryMinutes = 60;
        }
        if (maxConcurrentSessions <= 0) {
            maxConcurrentSessions = 5;
        }
        if (allowedIpRanges == null) {
            allowedIpRanges = List.of();
        }
    }

    public static TenantSettings defaults(String tenantId) {
        return new TenantSettings(tenantId, false, 60, 5, List.of("0.0.0.0/0"));
    }
}
