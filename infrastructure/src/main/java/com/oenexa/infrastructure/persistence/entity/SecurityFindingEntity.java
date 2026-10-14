package com.oenexa.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "security_findings")
public class SecurityFindingEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "repository_id", nullable = false, length = 36)
    private String repositoryId;

    @Column(name = "scan_id", length = 36)
    private String scanId;

    @Column(name = "cve_id", length = 50)
    private String cveId;

    @Column(name = "vulnerability_type", nullable = false, length = 100)
    private String vulnerabilityType;

    @Column(nullable = false, length = 30)
    private String severity = "HIGH";

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String remediation;

    @Column(nullable = false, length = 50)
    private String status = "UNRESOLVED";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected SecurityFindingEntity() {}

    public SecurityFindingEntity(String id, String repositoryId, String scanId, String cveId, String vulnerabilityType, String severity, String description, String remediation) {
        this.id = id;
        this.repositoryId = repositoryId;
        this.scanId = scanId;
        this.cveId = cveId;
        this.vulnerabilityType = vulnerabilityType;
        this.severity = severity != null ? severity : "HIGH";
        this.description = description;
        this.remediation = remediation;
        this.status = "UNRESOLVED";
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public String getRepositoryId() { return repositoryId; }
    public String getScanId() { return scanId; }
    public String getCveId() { return cveId; }
    public String getVulnerabilityType() { return vulnerabilityType; }
    public String getSeverity() { return severity; }
    public String getDescription() { return description; }
    public String getRemediation() { return remediation; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
}
