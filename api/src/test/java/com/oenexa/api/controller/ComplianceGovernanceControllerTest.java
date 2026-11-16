package com.oenexa.api.controller;

import com.oenexa.api.dto.ApiResponse;
import com.oenexa.api.dto.governance.ComplianceGovernanceDtos.*;
import com.oenexa.application.governance.ComplianceGovernanceService;
import com.oenexa.domain.model.governance.ComplianceGovernanceDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ComplianceGovernanceController Zero-Mock Unit Tests")
class ComplianceGovernanceControllerTest {

    private ComplianceGovernanceController controller;

    @BeforeEach
    void setUp() {
        ComplianceGovernanceService service = new ComplianceGovernanceService();
        controller = new ComplianceGovernanceController(service);
    }

    @Test
    @DisplayName("Should run compliance audit via REST endpoint")
    void shouldRunComplianceAudit() {
        RunComplianceAuditRequest req = new RunComplianceAuditRequest(
                "oenexa-auth",
                ComplianceFramework.GDPR,
                "public record User(String email, String passwordHash, Instant GDPRConsentAt) { // uses Argon2id }"
        );

        ResponseEntity<ApiResponse<ComplianceAuditDossier>> resp = controller.runComplianceAudit(req);

        assertNotNull(resp);
        assertEquals(200, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().success());

        ComplianceAuditDossier dossier = resp.getBody().data();
        assertNotNull(dossier);
        assertEquals("oenexa-auth", dossier.targetScope());
        assertTrue(dossier.compliant());
    }

    @Test
    @DisplayName("Should verify ledger balance via REST endpoint")
    void shouldVerifyLedger() {
        VerifyLedgerRequest req = new VerifyLedgerRequest("ledger-test-01", 1250.75, 1250.75);

        ResponseEntity<ApiResponse<LedgerBalanceAudit>> resp = controller.verifyLedger(req);

        assertNotNull(resp);
        assertEquals(200, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().success());

        LedgerBalanceAudit audit = resp.getBody().data();
        assertNotNull(audit);
        assertTrue(audit.balanced());
    }

    @Test
    @DisplayName("Should list available compliance frameworks")
    void shouldListFrameworks() {
        ResponseEntity<ApiResponse<List<FrameworkInfoResponse>>> resp = controller.listFrameworks();

        assertNotNull(resp);
        assertEquals(200, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().success());
        assertEquals(ComplianceFramework.values().length, resp.getBody().data().size());
    }

    @Test
    @DisplayName("Should list governance audit records via REST endpoint")
    void shouldListAuditRecords() {
        ResponseEntity<ApiResponse<List<GovernanceAuditRecord>>> resp = controller.listAuditRecords(10);

        assertNotNull(resp);
        assertEquals(200, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().success());
    }
}
