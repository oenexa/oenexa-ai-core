package com.oenexa.api.dto.governance;

import com.oenexa.domain.model.governance.ComplianceGovernanceDomain.ComplianceFramework;

import java.util.List;

/**
 * Enterprise Compliance, Audit & Governance API Data Transfer Objects (DTOs).
 * Strictly immutable boundary schemas for request/response serialization.
 */
public final class ComplianceGovernanceDtos {

    private ComplianceGovernanceDtos() {}

    public record RunComplianceAuditRequest(
            String targetScope,
            ComplianceFramework framework,
            String technicalContext
    ) {}

    public record VerifyLedgerRequest(
            String ledgerId,
            double debits,
            double credits
    ) {}

    public record FrameworkInfoResponse(
            ComplianceFramework framework,
            String title,
            String description
    ) {}
}
