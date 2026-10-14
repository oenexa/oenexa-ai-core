package com.oenexa.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "pull_requests")
public class PullRequestEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "repository_id", nullable = false, length = 36)
    private String repositoryId;

    @Column(name = "pr_number", nullable = false)
    private int prNumber;

    @Column(nullable = false)
    private String title;

    @Column(name = "source_branch", nullable = false, length = 100)
    private String sourceBranch;

    @Column(name = "target_branch", nullable = false, length = 100)
    private String targetBranch;

    @Column(nullable = false, length = 50)
    private String status = "OPEN";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected PullRequestEntity() {}

    public PullRequestEntity(String id, String repositoryId, int prNumber, String title, String sourceBranch, String targetBranch) {
        this.id = id;
        this.repositoryId = repositoryId;
        this.prNumber = prNumber;
        this.title = title;
        this.sourceBranch = sourceBranch;
        this.targetBranch = targetBranch;
        this.status = "OPEN";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public String getRepositoryId() { return repositoryId; }
    public int getPrNumber() { return prNumber; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSourceBranch() { return sourceBranch; }
    public String getTargetBranch() { return targetBranch; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
