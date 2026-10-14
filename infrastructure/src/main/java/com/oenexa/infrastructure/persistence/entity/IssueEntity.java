package com.oenexa.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "issues")
public class IssueEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "repository_id", nullable = false, length = 36)
    private String repositoryId;

    @Column(name = "scan_id", length = 36)
    private String scanId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 30)
    private String severity = "MEDIUM";

    @Column(nullable = false, length = 50)
    private String status = "OPEN";

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "line_number")
    private Integer lineNumber;

    @Column(name = "root_cause", columnDefinition = "TEXT")
    private String rootCause;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected IssueEntity() {}

    public IssueEntity(String id, String repositoryId, String scanId, String title, String severity, String filePath, Integer lineNumber) {
        this.id = id;
        this.repositoryId = repositoryId;
        this.scanId = scanId;
        this.title = title;
        this.severity = severity != null ? severity : "MEDIUM";
        this.filePath = filePath;
        this.lineNumber = lineNumber;
        this.status = "OPEN";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public String getRepositoryId() { return repositoryId; }
    public String getScanId() { return scanId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getFilePath() { return filePath; }
    public Integer getLineNumber() { return lineNumber; }
    public String getRootCause() { return rootCause; }
    public void setRootCause(String rootCause) { this.rootCause = rootCause; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
