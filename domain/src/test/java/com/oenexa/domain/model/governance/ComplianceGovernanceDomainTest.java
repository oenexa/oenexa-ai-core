package com.oenexa.domain.model.governance;

import com.oenexa.domain.model.governance.ComplianceGovernanceDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ComplianceGovernanceDomain Zero-Mock Unit Tests")
class ComplianceGovernanceDomainTest {

    @Test
    @DisplayName("Should create ComplianceAuditDossier and compute high risk status accurately")
    void shouldCreateComplianceAuditDossier() {
        ComplianceAuditDossier cleanDossier = new ComplianceAuditDossier(
                "dos-01",
                "oengo-api",
                ComplianceFramework.GDPR,
                true,
                0.15,
                List.of(),
                List.of(),
                null
        );

        assertNotNull(cleanDossier);
        assertEquals("dos-01", cleanDossier.dossierId());
        assertEquals(ComplianceFramework.GDPR, cleanDossier.framework());
        assertTrue(cleanDossier.compliant());
        assertFalse(cleanDossier.isHighRisk());

        ComplianceAuditDossier riskyDossier = new ComplianceAuditDossier(
                "dos-02",
                "legacy-payment-service",
                ComplianceFramework.EU_AMLD5_AMLD6,
                false,
                0.85,
                List.of("Anonymous wallet deposit over €150 without KYC"),
                List.of("Enforce KYC Tier 1 validation"),
                null
        );

        assertTrue(riskyDossier.isHighRisk());
        assertFalse(riskyDossier.compliant());
        assertEquals(1, riskyDossier.findings().size());
    }

    @Test
    @DisplayName("Should verify LedgerBalanceAudit double-entry invariant (Debits == Credits)")
    void shouldVerifyLedgerBalanceAudit() {
        LedgerBalanceAudit balanced = LedgerBalanceAudit.of("ledger-001", 1500.50, 1500.50);
        assertTrue(balanced.balanced());
        assertEquals(0.0, balanced.imbalanceAmount(), 0.0001);

        LedgerBalanceAudit imbalanced = LedgerBalanceAudit.of("ledger-002", 1500.50, 1400.00);
        assertFalse(imbalanced.balanced());
        assertEquals(100.50, imbalanced.imbalanceAmount(), 0.0001);
    }

    @Test
    @DisplayName("Should instantiate GovernanceAuditRecord with cryptographic signature hash")
    void shouldInstantiateGovernanceAuditRecord() {
        GovernanceAuditRecord record = new GovernanceAuditRecord(
                "rec-01",
                "PROMPT_EXECUTE",
                "engineer-01",
                "oenexa-ai-core",
                "mldsa65:signatureVerified",
                true,
                null
        );

        assertEquals("rec-01", record.recordId());
        assertEquals("PROMPT_EXECUTE", record.action());
        assertTrue(record.integrityVerified());
        assertTrue(record.signatureHash().contains("mldsa65"));
        assertNotNull(record.timestamp());
    }
}
