package ai.oenexa.admin;

import ai.oenexa.admin.domain.AuditAction;
import ai.oenexa.admin.domain.AuditLog;
import ai.oenexa.admin.domain.TenantSettings;
import ai.oenexa.admin.domain.TenantUsageMetrics;
import ai.oenexa.admin.service.DefaultAdminService;
import ai.oenexa.admin.web.AdminController;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OENEXA Admin Service & Controller Tests (Zero-Mock Policy)")
class AdminServiceTest {

    private DefaultAdminService adminService;
    private AdminController adminController;

    @BeforeEach
    void setUp() {
        adminService = new DefaultAdminService();
        adminController = new AdminController(adminService);
    }

    @Test
    @DisplayName("AdminModule metadata and initialization")
    void testAdminModuleMetadata() {
        AdminModule module = new AdminModule();
        assertEquals("admin", module.id());
        assertEquals("OENEXA Admin", module.displayName());
        assertTrue(module.capabilities().contains("audit-logs"));
    }

    @Test
    @DisplayName("AuditAction enum invariants")
    void testEnumInvariants() {
        assertEquals(5, AuditAction.values().length);
        assertEquals(AuditAction.USER_LOGIN, AuditAction.valueOf("USER_LOGIN"));
        assertEquals(AuditAction.SECRET_ROTATED, AuditAction.valueOf("SECRET_ROTATED"));
    }

    @Test
    @DisplayName("Domain records null safety and fallback invariants")
    void testDomainRecordsEdgeCases() {
        AuditLog log = new AuditLog("a-1", "tenant-1", "alice", AuditAction.USER_LOGIN, null, null, null, true);
        assertEquals("general", log.resource());
        assertNotNull(log.timestamp());
        assertEquals("127.0.0.1", log.ipAddress());
        assertTrue(log.successful());

        assertThrows(NullPointerException.class, () -> new AuditLog(null, "t", "a", AuditAction.USER_LOGIN, "", null, "", true));
        assertThrows(NullPointerException.class, () -> new AuditLog("id", null, "a", AuditAction.USER_LOGIN, "", null, "", true));
        assertThrows(NullPointerException.class, () -> new AuditLog("id", "t", null, AuditAction.USER_LOGIN, "", null, "", true));
        assertThrows(NullPointerException.class, () -> new AuditLog("id", "t", "a", null, "", null, "", true));

        TenantUsageMetrics metrics = new TenantUsageMetrics("tenant-1", 100, 35000, 5, 2.5, null, null);
        assertNotNull(metrics.periodStart());
        assertNotNull(metrics.periodEnd());

        assertThrows(NullPointerException.class, () -> new TenantUsageMetrics(null, 0, 0, 0, 0, null, null));

        TenantSettings defaults = TenantSettings.defaults("tenant-1");
        assertEquals(60, defaults.tokenExpiryMinutes());
        assertEquals(5, defaults.maxConcurrentSessions());
        assertFalse(defaults.mfaRequired());

        TenantSettings custom = new TenantSettings("tenant-2", true, -1, 0, null);
        assertEquals(60, custom.tokenExpiryMinutes());
        assertEquals(5, custom.maxConcurrentSessions());
        assertTrue(custom.allowedIpRanges().isEmpty());

        assertThrows(NullPointerException.class, () -> new TenantSettings(null, false, 60, 5, List.of()));
    }

    @Test
    @DisplayName("Record audit logs and retrieve with filtering and limits")
    void testAuditLogWorkflow() {
        AuditLog log1 = adminService.recordAuditLog("tenant-corp", "admin-1", AuditAction.WORKSPACE_CREATED, "ws-100", "10.0.0.1", true);
        AuditLog log2 = adminService.recordAuditLog("tenant-corp", "user-2", AuditAction.TOKEN_ISSUED, "auth", "10.0.0.2", true);
        AuditLog logOther = adminService.recordAuditLog("tenant-other", "user-3", AuditAction.USER_LOGIN, "auth", "10.0.0.3", false);

        assertNotNull(log1.id());
        assertNotNull(log2.id());

        List<AuditLog> corpLogs = adminService.getAuditLogsForTenant("tenant-corp", 10);
        assertEquals(2, corpLogs.size());

        List<AuditLog> limitedLogs = adminService.getAuditLogsForTenant("tenant-corp", 1);
        assertEquals(1, limitedLogs.size());

        assertTrue(adminService.getAuditLogsForTenant(null, 10).isEmpty());
        assertEquals(1, adminService.getAuditLogsForTenant("tenant-other", 0).size());
    }

    @Test
    @DisplayName("Calculate tenant usage metrics from audit activity")
    void testTenantUsageMetrics() {
        adminService.recordAuditLog("tenant-usage", "dev-1", AuditAction.USER_LOGIN, "login", "127.0.0.1", true);
        adminService.recordAuditLog("tenant-usage", "dev-2", AuditAction.TOKEN_ISSUED, "token", "127.0.0.1", true);

        TenantUsageMetrics metrics = adminService.getUsageMetrics("tenant-usage");
        assertEquals("tenant-usage", metrics.tenantId());
        assertEquals(2, metrics.totalApiCalls());
        assertEquals(700L, metrics.totalTokensConsumed());
        assertEquals(2, metrics.activeUsers());
        assertTrue(metrics.computeHoursUsed() >= 0.1);
    }

    @Test
    @DisplayName("Get and update tenant enterprise settings")
    void testTenantSettingsManagement() {
        TenantSettings initial = adminService.getTenantSettings("tenant-secure");
        assertFalse(initial.mfaRequired());

        TenantSettings updated = new TenantSettings("tenant-secure", true, 120, 10, List.of("192.168.1.0/24"));
        adminService.updateTenantSettings(updated);

        TenantSettings retrieved = adminService.getTenantSettings("tenant-secure");
        assertTrue(retrieved.mfaRequired());
        assertEquals(120, retrieved.tokenExpiryMinutes());
        assertEquals(10, retrieved.maxConcurrentSessions());
    }

    @Test
    @DisplayName("AdminController endpoints return valid HTTP responses")
    void testAdminControllerEndpoints() {
        ResponseEntity<AuditLog> auditResp = adminController.recordAudit(Map.of(
                "tenantId", "tenant-ctrl",
                "actor", "superadmin",
                "action", "POLICY_CHANGED",
                "resource", "security-policy",
                "ipAddress", "192.168.0.10",
                "successful", true
        ));
        assertEquals(200, auditResp.getStatusCode().value());
        assertNotNull(auditResp.getBody());

        ResponseEntity<List<AuditLog>> logsResp = adminController.getAuditLogs("tenant-ctrl", 50);
        assertEquals(200, logsResp.getStatusCode().value());
        assertEquals(1, logsResp.getBody().size());

        ResponseEntity<TenantUsageMetrics> usageResp = adminController.getUsage("tenant-ctrl");
        assertEquals(200, usageResp.getStatusCode().value());
        assertNotNull(usageResp.getBody());

        ResponseEntity<TenantSettings> settingsResp = adminController.getSettings("tenant-ctrl");
        assertEquals(200, settingsResp.getStatusCode().value());

        TenantSettings newSettings = new TenantSettings("tenant-ctrl", true, 90, 8, List.of("10.0.0.0/8"));
        ResponseEntity<TenantSettings> updateResp = adminController.updateSettings(newSettings);
        assertEquals(200, updateResp.getStatusCode().value());
        assertTrue(updateResp.getBody().mfaRequired());
    }
}
