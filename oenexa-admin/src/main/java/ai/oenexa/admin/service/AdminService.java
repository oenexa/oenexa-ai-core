package ai.oenexa.admin.service;

import ai.oenexa.admin.domain.AuditAction;
import ai.oenexa.admin.domain.AuditLog;
import ai.oenexa.admin.domain.TenantSettings;
import ai.oenexa.admin.domain.TenantUsageMetrics;
import java.util.List;

/**
 * Service port for multi-tenant governance, audit recording, and usage analytics.
 */
public interface AdminService {

    AuditLog recordAuditLog(String tenantId, String actor, AuditAction action, String resource, String ipAddress, boolean successful);

    List<AuditLog> getAuditLogsForTenant(String tenantId, int limit);

    TenantUsageMetrics getUsageMetrics(String tenantId);

    TenantSettings updateTenantSettings(TenantSettings settings);

    TenantSettings getTenantSettings(String tenantId);
}
