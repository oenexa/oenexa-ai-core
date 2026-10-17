package com.oenexa.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "argon_self_healing_cycles")
public class ArgonSelfHealingCycleEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "goal_id", nullable = false, length = 36)
    private String goalId;

    @Column(name = "issue_detected", columnDefinition = "TEXT", nullable = false)
    private String issueDetected;

    @Column(name = "root_cause", columnDefinition = "TEXT", nullable = false)
    private String rootCause;

    @Column(name = "patch_diff", columnDefinition = "TEXT", nullable = false)
    private String patchDiff;

    @Column(nullable = false)
    private boolean resolved = true;

    @Column(nullable = false)
    private Instant timestamp = Instant.now();

    public ArgonSelfHealingCycleEntity() {}

    public ArgonSelfHealingCycleEntity(String id, String goalId, String issueDetected, String rootCause, String patchDiff, boolean resolved) {
        this.id = id;
        this.goalId = goalId;
        this.issueDetected = issueDetected;
        this.rootCause = rootCause;
        this.patchDiff = patchDiff;
        this.resolved = resolved;
        this.timestamp = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getGoalId() { return goalId; }
    public void setGoalId(String goalId) { this.goalId = goalId; }
    public String getIssueDetected() { return issueDetected; }
    public void setIssueDetected(String issueDetected) { this.issueDetected = issueDetected; }
    public String getRootCause() { return rootCause; }
    public void setRootCause(String rootCause) { this.rootCause = rootCause; }
    public String getPatchDiff() { return patchDiff; }
    public void setPatchDiff(String patchDiff) { this.patchDiff = patchDiff; }
    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
