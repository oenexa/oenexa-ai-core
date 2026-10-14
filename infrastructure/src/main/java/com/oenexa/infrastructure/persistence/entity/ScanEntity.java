package com.oenexa.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "scans")
public class ScanEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "repository_id", nullable = false, length = 36)
    private String repositoryId;

    @Column(nullable = false, length = 100)
    private String branch;

    @Column(name = "commit_hash", length = 100)
    private String commitHash;

    @Column(name = "scan_type", nullable = false, length = 50)
    private String scanType;

    @Column(nullable = false, length = 50)
    private String status = "PENDING";

    @Column(name = "findings_count", nullable = false)
    private int findingsCount = 0;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;

    protected ScanEntity() {}

    public ScanEntity(String id, String repositoryId, String branch, String commitHash, String scanType) {
        this.id = id;
        this.repositoryId = repositoryId;
        this.branch = branch;
        this.commitHash = commitHash;
        this.scanType = scanType;
        this.status = "PENDING";
        this.startedAt = Instant.now();
    }

    public String getId() { return id; }
    public String getRepositoryId() { return repositoryId; }
    public String getBranch() { return branch; }
    public String getCommitHash() { return commitHash; }
    public String getScanType() { return scanType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getFindingsCount() { return findingsCount; }
    public void setFindingsCount(int findingsCount) { this.findingsCount = findingsCount; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
