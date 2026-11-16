package com.oenexa.api.controller;

import com.oenexa.api.dto.ApiResponse;
import com.oenexa.api.dto.governance.ComplianceGovernanceDtos.*;
import com.oenexa.application.governance.ComplianceGovernanceService;
import com.oenexa.domain.model.governance.ComplianceGovernanceDomain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Enterprise Compliance, Audit & Governance REST API Controller.
 * Exposes endpoints for:
 * 1. Regulatory framework audits (GDPR, AMLD5/AMLD6, OWASP, PQC) (`/api/v4/governance/audit`)
 * 2. Double-entry ledger balance verification (`/api/v4/governance/ledger/verify`)
 * 3. Cryptographic tamper-evident audit log query (`/api/v4/governance/audit/records`)
 * 4. Regulatory framework catalogue (`/api/v4/governance/frameworks`)
 */
@RestController
@RequestMapping("/api/v4/governance")
@CrossOrigin(originPatterns = "*", allowedHeaders = "*")
public class ComplianceGovernanceController {

    private final ComplianceGovernanceService governanceService;

    public ComplianceGovernanceController(ComplianceGovernanceService governanceService) {
        this.governanceService = Objects.requireNonNull(governanceService, "governanceService must not be null");
    }

    @PostMapping("/audit")
    public ResponseEntity<ApiResponse<ComplianceAuditDossier>> runComplianceAudit(
            @RequestBody RunComplianceAuditRequest request
    ) {
        String scope = (request != null) ? request.targetScope() : "global";
        ComplianceFramework framework = (request != null && request.framework() != null)
                ? request.framework()
                : ComplianceFramework.ZERO_TRUST_RBAC;
        String context = (request != null) ? request.technicalContext() : "";

        ComplianceAuditDossier dossier = governanceService.auditCompliance(scope, framework, context);
        return ResponseEntity.ok(ApiResponse.ok(dossier));
    }

    @PostMapping("/ledger/verify")
    public ResponseEntity<ApiResponse<LedgerBalanceAudit>> verifyLedger(
            @RequestBody VerifyLedgerRequest request
    ) {
        String ledgerId = (request != null) ? request.ledgerId() : "default-ledger";
        double debits = (request != null) ? request.debits() : 0.0;
        double credits = (request != null) ? request.credits() : 0.0;

        LedgerBalanceAudit audit = governanceService.verifyLedgerBalance(ledgerId, debits, credits);
        return ResponseEntity.ok(ApiResponse.ok(audit));
    }

    @GetMapping("/audit/records")
    public ResponseEntity<ApiResponse<List<GovernanceAuditRecord>>> listAuditRecords(
            @RequestParam(value = "limit", defaultValue = "50") int limit
    ) {
        return ResponseEntity.ok(ApiResponse.ok(governanceService.listAuditRecords(limit)));
    }

    @GetMapping("/frameworks")
    public ResponseEntity<ApiResponse<List<FrameworkInfoResponse>>> listFrameworks() {
        List<FrameworkInfoResponse> list = Arrays.stream(ComplianceFramework.values())
                .map(fw -> new FrameworkInfoResponse(fw, fw.getTitle(), fw.getDescription()))
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }
}
