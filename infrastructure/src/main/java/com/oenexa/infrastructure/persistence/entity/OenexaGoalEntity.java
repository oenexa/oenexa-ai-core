package com.oenexa.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "oenexa_goals")
public class OenexaGoalEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "repository_id", nullable = false, length = 100)
    private String repositoryId;

    @Column(name = "goal_description", columnDefinition = "TEXT", nullable = false)
    private String goalDescription;

    @Column(name = "current_phase", nullable = false, length = 50)
    private String currentPhase = "PLANNING";

    @Column(name = "autonomous_self_healing", nullable = false)
    private boolean autonomousSelfHealing = true;

    @Column(name = "extended_thinking", nullable = false)
    private boolean extendedThinking = true;

    @Column(name = "confidence_score", nullable = false)
    private double confidenceScore = 1.0;

    @Column(name = "final_summary", columnDefinition = "TEXT")
    private String finalSummary;

    @Column(nullable = false, length = 50)
    private String status = "RUNNING";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;

    public OenexaGoalEntity() {}

    public OenexaGoalEntity(String id, String userId, String repositoryId, String goalDescription) {
        this.id = id;
        this.userId = userId;
        this.repositoryId = repositoryId;
        this.goalDescription = goalDescription;
        this.currentPhase = "PLANNING";
        this.status = "RUNNING";
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getRepositoryId() { return repositoryId; }
    public void setRepositoryId(String repositoryId) { this.repositoryId = repositoryId; }

    public String getGoalDescription() { return goalDescription; }
    public void setGoalDescription(String goalDescription) { this.goalDescription = goalDescription; }

    public String getCurrentPhase() { return currentPhase; }
    public void setCurrentPhase(String currentPhase) { this.currentPhase = currentPhase; }

    public boolean isAutonomousSelfHealing() { return autonomousSelfHealing; }
    public void setAutonomousSelfHealing(boolean autonomousSelfHealing) { this.autonomousSelfHealing = autonomousSelfHealing; }

    public boolean isExtendedThinking() { return extendedThinking; }
    public void setExtendedThinking(boolean extendedThinking) { this.extendedThinking = extendedThinking; }

    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }

    public String getFinalSummary() { return finalSummary; }
    public void setFinalSummary(String finalSummary) { this.finalSummary = finalSummary; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
