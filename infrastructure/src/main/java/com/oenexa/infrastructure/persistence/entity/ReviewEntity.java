package com.oenexa.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "reviews")
public class ReviewEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "pull_request_id", nullable = false, length = 36)
    private String pullRequestId;

    @Column(name = "reviewer_type", nullable = false, length = 50)
    private String reviewerType = "AI_AGENT";

    @Column(name = "quality_score")
    private Integer qualityScore = 100;

    @Column(name = "risk_score")
    private Integer riskScore = 0;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(nullable = false, length = 50)
    private String status = "APPROVED";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected ReviewEntity() {}

    public ReviewEntity(String id, String pullRequestId, String summary, Integer qualityScore, Integer riskScore) {
        this.id = id;
        this.pullRequestId = pullRequestId;
        this.summary = summary;
        this.qualityScore = qualityScore;
        this.riskScore = riskScore;
        this.reviewerType = "AI_AGENT";
        this.status = "APPROVED";
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public String getPullRequestId() { return pullRequestId; }
    public String getReviewerType() { return reviewerType; }
    public Integer getQualityScore() { return qualityScore; }
    public Integer getRiskScore() { return riskScore; }
    public String getSummary() { return summary; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
}
