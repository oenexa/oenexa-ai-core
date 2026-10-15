package ai.oenexa.admin.service;

import ai.oenexa.admin.domain.AuditAction;
import ai.oenexa.admin.domain.AuditLog;
import ai.oenexa.admin.domain.TenantSettings;
import ai.oenexa.admin.domain.TenantUsageMetrics;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;

@Service
public class DefaultAdminService implements AdminService {

    private final List<AuditLog> auditLogs = new CopyOnWriteArrayList<>();
    private final Map<String, TenantSettings> settingsStore = new ConcurrentHashMap<>();

    @Override
    public AuditLog recordAuditLog(String tenantId, String actor, AuditAction action, String resource, String ipAddress, boolean successful) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(actor, "actor must not be null");
        Objects.requireNonNull(action, "action must not be null");

        String id = "audit-" + UUID.randomUUID().toString().substring(0, 8);
        AuditLog log = new AuditLog(id, tenantId, actor, action, resource, Instant.now(), ipAddress, successful);
        auditLogs.add(log);
        return log;
    }

    @Override
    public List<AuditLog> getAuditLogsForTenant(String tenantId, int limit) {
        if (tenantId == null) {
            return List.of();
        }
        int effectiveLimit = limit <= 0 ? 50 : limit;
        List<AuditLog> matches = new ArrayList<>(auditLogs.stream()
                .filter(a -> tenantId.equals(a.tenantId()))
                .toList());
        Collections.reverse(matches);
        if (matches.size() > effectiveLimit) {
            matches = matches.subList(0, effectiveLimit);
        }
        return matches;
    }

    @Override
    public TenantUsageMetrics getUsageMetrics(String tenantId) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        long calls = auditLogs.stream().filter(a -> tenantId.equals(a.tenantId())).count();
        long tokens = calls * 350L;
        int activeUsers = (int) auditLogs.stream().filter(a -> tenantId.equals(a.tenantId())).map(AuditLog::actor).distinct().count();
        double computeHours = Math.round((calls * 0.05) * 100.0) / 100.0;

        return new TenantUsageMetrics(
                tenantId,
                calls,
                tokens,
                Math.max(1, activeUsers),
                computeHours,
                Instant.now().minusSeconds(86400 * 7),
                Instant.now()
        );
    }

    @Override
    public TenantSettings updateTenantSettings(TenantSettings settings) {
        Objects.requireNonNull(settings, "settings must not be null");
        settingsStore.put(settings.tenantId(), settings);
        return settings;
    }

    @Override
    public TenantSettings getTenantSettings(String tenantId) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        return settingsStore.computeIfAbsent(tenantId, TenantSettings::defaults);
    }
}
