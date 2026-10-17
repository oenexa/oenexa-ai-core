package com.oenexa.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "product_requirements_documents")
public class ProductRequirementsDocumentEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "target_audience", nullable = false, length = 255)
    private String targetAudience;

    @Column(name = "problem_statement", columnDefinition = "TEXT", nullable = false)
    private String problemStatement;

    @Column(name = "key_features", columnDefinition = "TEXT", nullable = false)
    private String keyFeatures;

    @Column(name = "technical_constraints", columnDefinition = "TEXT", nullable = false)
    private String technicalConstraints;

    @Column(name = "release_roadmap", nullable = false, length = 255)
    private String releaseRoadmap;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public ProductRequirementsDocumentEntity() {}

    public ProductRequirementsDocumentEntity(String id, String title, String targetAudience, String problemStatement, String keyFeatures, String technicalConstraints, String releaseRoadmap) {
        this.id = id;
        this.title = title;
        this.targetAudience = targetAudience;
        this.problemStatement = problemStatement;
        this.keyFeatures = keyFeatures;
        this.technicalConstraints = technicalConstraints;
        this.releaseRoadmap = releaseRoadmap;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getTargetAudience() { return targetAudience; }
    public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }
    public String getProblemStatement() { return problemStatement; }
    public void setProblemStatement(String problemStatement) { this.problemStatement = problemStatement; }
    public String getKeyFeatures() { return keyFeatures; }
    public void setKeyFeatures(String keyFeatures) { this.keyFeatures = keyFeatures; }
    public String getTechnicalConstraints() { return technicalConstraints; }
    public void setTechnicalConstraints(String technicalConstraints) { this.technicalConstraints = technicalConstraints; }
    public String getReleaseRoadmap() { return releaseRoadmap; }
    public void setReleaseRoadmap(String releaseRoadmap) { this.releaseRoadmap = releaseRoadmap; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
