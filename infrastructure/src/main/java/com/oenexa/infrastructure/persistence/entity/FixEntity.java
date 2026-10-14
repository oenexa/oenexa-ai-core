package com.oenexa.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "fixes")
public class FixEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "issue_id", nullable = false, length = 36)
    private String issueId;

    @Column(nullable = false, length = 50)
    private String status = "GENERATED";

    @Column(name = "patch_diff", columnDefinition = "TEXT")
    private String patchDiff;

    @Column(name = "commit_message")
    private String commitMessage;

    @Column(nullable = false)
    private boolean validated = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected FixEntity() {}

    public FixEntity(String id, String issueId, String patchDiff, String commitMessage) {
        this.id = id;
        this.issueId = issueId;
        this.patchDiff = patchDiff;
        this.commitMessage = commitMessage;
        this.status = "GENERATED";
        this.validated = false;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public String getIssueId() { return issueId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPatchDiff() { return patchDiff; }
    public void setPatchDiff(String patchDiff) { this.patchDiff = patchDiff; }
    public String getCommitMessage() { return commitMessage; }
    public void setCommitMessage(String commitMessage) { this.commitMessage = commitMessage; }
    public boolean isValidated() { return validated; }
    public void setValidated(boolean validated) { this.validated = validated; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
