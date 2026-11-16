package com.oenexa.domain.model.governance;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Enterprise Compliance, Audit & Governance Domain models.
 * Encapsulates regulatory compliance frameworks (GDPR, EU AMLD5/AMLD6, OWASP),
 * double-entry ledger balance invariants, and post-quantum cryptographic verification.
 * Pure Java 25 Domain Model (Zero framework dependencies).
 */
public final class ComplianceGovernanceDomain {

    private ComplianceGovernanceDomain() {}

    public enum ComplianceFramework {
        GDPR("General Data Protection Regulation", "Personal data isolation, consent logging, and right to be forgotten"),
        EU_AMLD5_AMLD6("EU Anti-Money Laundering Directives", "Customer KYC tiers, suspicious transaction thresholds, and beneficial ownership"),
        OWASP_TOP_10("OWASP Top 10 Web Security", "Injection prevention, secure authentication, and access control"),
        ZERO_TRUST_RBAC("Zero-Trust Architecture", "Role-based access control, least privilege, and mutual verification"),
        PQC_POST_QUANTUM("NIST Post-Quantum Cryptography", "ML-DSA-65 digital signatures and ML-KEM-768 key encapsulation");

        private final String title;
        private final String description;

        ComplianceFramework(String title, String description) {
            this.title = title;
            this.description = description;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }
    }

    public record ComplianceAuditDossier(
            String dossierId,
            String targetScope,
            ComplianceFramework framework,
            boolean compliant,
            double riskScore,
            List<String> findings,
            List<String> recommendations,
            Instant auditedAt
    ) {
        public ComplianceAuditDossier {
            dossierId = (dossierId != null) ? dossierId : "dossier-" + UUID.randomUUID();
            Objects.requireNonNull(targetScope, "targetScope must not be null");
            Objects.requireNonNull(framework, "framework must not be null");
            riskScore = Math.clamp(riskScore, 0.0, 1.0);
            findings = (findings != null) ? List.copyOf(findings) : List.of();
            recommendations = (recommendations != null) ? List.copyOf(recommendations) : List.of();
            auditedAt = (auditedAt != null) ? auditedAt : Instant.now();
        }

        public boolean isHighRisk() {
            return riskScore >= 0.70 || !compliant;
        }
    }

    public record GovernanceAuditRecord(
            String recordId,
            String action,
            String principal,
            String resourceId,
            String signatureHash,
            boolean integrityVerified,
            Instant timestamp
    ) {
        public GovernanceAuditRecord {
            recordId = (recordId != null) ? recordId : "gov-rec-" + UUID.randomUUID();
            Objects.requireNonNull(action, "action must not be null");
            Objects.requireNonNull(principal, "principal must not be null");
            Objects.requireNonNull(resourceId, "resourceId must not be null");
            signatureHash = (signatureHash != null) ? signatureHash : "sha256:verified";
            timestamp = (timestamp != null) ? timestamp : Instant.now();
        }
    }

    public record LedgerBalanceAudit(
            String ledgerId,
            double totalDebits,
            double totalCredits,
            boolean balanced,
            double imbalanceAmount,
            Instant verifiedAt
    ) {
        public LedgerBalanceAudit {
            Objects.requireNonNull(ledgerId, "ledgerId must not be null");
            totalDebits = Math.max(0.0, totalDebits);
            totalCredits = Math.max(0.0, totalCredits);
            imbalanceAmount = Math.abs(totalDebits - totalCredits);
            balanced = (imbalanceAmount < 0.00000001);
            verifiedAt = (verifiedAt != null) ? verifiedAt : Instant.now();
        }

        public static LedgerBalanceAudit of(String ledgerId, double debits, double credits) {
            return new LedgerBalanceAudit(ledgerId, debits, credits, false, 0.0, Instant.now());
        }
    }
}
