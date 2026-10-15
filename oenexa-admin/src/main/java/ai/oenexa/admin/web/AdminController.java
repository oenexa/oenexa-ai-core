package ai.oenexa.admin.web;

import ai.oenexa.admin.domain.AuditAction;
import ai.oenexa.admin.domain.AuditLog;
import ai.oenexa.admin.domain.TenantSettings;
import ai.oenexa.admin.domain.TenantUsageMetrics;
import ai.oenexa.admin.service.AdminService;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = Objects.requireNonNull(adminService, "adminService must not be null");
    }

    @PostMapping("/audit")
    public ResponseEntity<AuditLog> recordAudit(@RequestBody Map<String, Object> payload) {
        String tenantId = (String) payload.getOrDefault("tenantId", "default");
        String actor = (String) payload.getOrDefault("actor", "system");
        String actionStr = (String) payload.getOrDefault("action", "USER_LOGIN");
        AuditAction action = AuditAction.valueOf(actionStr);
        String resource = (String) payload.getOrDefault("resource", "auth");
        String ip = (String) payload.getOrDefault("ipAddress", "127.0.0.1");
        boolean success = (boolean) payload.getOrDefault("successful", true);

        return ResponseEntity.ok(adminService.recordAuditLog(tenantId, actor, action, resource, ip, success));
    }

    @GetMapping("/audit/{tenantId}")
    public ResponseEntity<List<AuditLog>> getAuditLogs(@PathVariable String tenantId, @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(adminService.getAuditLogsForTenant(tenantId, limit));
    }

    @GetMapping("/usage/{tenantId}")
    public ResponseEntity<TenantUsageMetrics> getUsage(@PathVariable String tenantId) {
        return ResponseEntity.ok(adminService.getUsageMetrics(tenantId));
    }

    @GetMapping("/settings/{tenantId}")
    public ResponseEntity<TenantSettings> getSettings(@PathVariable String tenantId) {
        return ResponseEntity.ok(adminService.getTenantSettings(tenantId));
    }

    @PutMapping("/settings")
    public ResponseEntity<TenantSettings> updateSettings(@RequestBody TenantSettings settings) {
        return ResponseEntity.ok(adminService.updateTenantSettings(settings));
    }
}
