package com.oenexa.application.governance;

import com.oenexa.domain.model.governance.ComplianceGovernanceDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ComplianceGovernanceService Zero-Mock Unit Tests")
class ComplianceGovernanceServiceTest {

    private ComplianceGovernanceService governanceService;

    @BeforeEach
    void setUp() {
        governanceService = new ComplianceGovernanceService();
    }

    @Test
    @DisplayName("Should detect GDPR non-compliance when consent or Argon2id is missing")
    void shouldDetectGdprNonCompliance() {
        ComplianceAuditDossier dossier = governanceService.auditCompliance(
                "user-auth-service",
                ComplianceFramework.GDPR,
                "public record User(String email, String password) {}"
        );

        assertNotNull(dossier);
        assertEquals(ComplianceFramework.GDPR, dossier.framework());
        assertFalse(dossier.compliant());
        assertTrue(dossier.riskScore() > 0.40);
        assertTrue(dossier.findings().stream().anyMatch(f -> f.contains("consent")));
        assertTrue(dossier.findings().stream().anyMatch(f -> f.contains("Argon2id")));
    }

    @Test
    @DisplayName("Should approve fully compliant GDPR schema")
    void shouldApproveCompliantGdpr() {
        ComplianceAuditDossier dossier = governanceService.auditCompliance(
                "user-auth-service",
                ComplianceFramework.GDPR,
                "public record User(String email, String passwordHash, Instant GDPRConsentAt) { // uses Argon2id }"
        );

        assertNotNull(dossier);
        assertTrue(dossier.compliant());
        assertEquals(0.05, dossier.riskScore());
        assertTrue(dossier.findings().isEmpty());
    }

    @Test
    @DisplayName("Should verify double-entry ledger balance accurately")
    void shouldVerifyDoubleEntryLedger() {
        LedgerBalanceAudit balanced = governanceService.verifyLedgerBalance("tx-100", 500.0, 500.0);
        assertTrue(balanced.balanced());
        assertEquals(0.0, balanced.imbalanceAmount(), 0.0001);

        LedgerBalanceAudit imbalanced = governanceService.verifyLedgerBalance("tx-101", 500.0, 480.0);
        assertFalse(imbalanced.balanced());
        assertEquals(20.0, imbalanced.imbalanceAmount(), 0.0001);
    }

    @Test
    @DisplayName("Should record and retrieve governance audit actions in order")
    void shouldRecordAndRetrieveAuditActions() {
        governanceService.recordGovernanceAction("DEPLOY_ARTIFACT", "devops-lead", "k8s:cluster-prod", "sig:mldsa65-valid");
        governanceService.recordGovernanceAction("ROTATE_KEY", "security-officer", "kms:master-key", "sig:mldsa65-valid");

        List<GovernanceAuditRecord> records = governanceService.listAuditRecords(10);
        assertNotNull(records);
        assertTrue(records.size() >= 2);
        assertEquals("ROTATE_KEY", records.get(0).action()); // LIFO order
    }
}
