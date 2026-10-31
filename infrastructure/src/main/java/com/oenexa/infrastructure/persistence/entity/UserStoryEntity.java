package com.oenexa.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "user_stories")
public class UserStoryEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "prd_id", length = 36)
    private String prdId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "as_a", nullable = false, length = 255)
    private String asA;

    @Column(name = "i_want", columnDefinition = "TEXT", nullable = false)
    private String iWant;

    @Column(name = "so_that", columnDefinition = "TEXT", nullable = false)
    private String soThat;

    @Column(name = "acceptance_criteria", columnDefinition = "TEXT", nullable = false)
    private String acceptanceCriteria;

    @Column(name = "story_points", nullable = false)
    private int storyPoints = 3;

    @Column(nullable = false, length = 50)
    private String status = "BACKLOG";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public UserStoryEntity() {}

    public UserStoryEntity(String id, String prdId, String title, String asA, String iWant, String soThat, String acceptanceCriteria, int storyPoints, String status) {
        this.id = id;
        this.prdId = prdId;
        this.title = title;
        this.asA = asA;
        this.iWant = iWant;
        this.soThat = soThat;
        this.acceptanceCriteria = acceptanceCriteria;
        this.storyPoints = storyPoints;
        this.status = status;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPrdId() {
        return prdId;
    }

    public void setPrdId(String prdId) {
        this.prdId = prdId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAsA() {
        return asA;
    }

    public void setAsA(String asA) {
        this.asA = asA;
    }

    public String getIWant() {
        return iWant;
    }

    public void setIWant(String iWant) {
        this.iWant = iWant;
    }

    public String getSoThat() {
        return soThat;
    }

    public void setSoThat(String soThat) {
        this.soThat = soThat;
    }

    public String getAcceptanceCriteria() {
        return acceptanceCriteria;
    }

    public void setAcceptanceCriteria(String acceptanceCriteria) {
        this.acceptanceCriteria = acceptanceCriteria;
    }

    public int getStoryPoints() {
        return storyPoints;
    }

    public void setStoryPoints(int storyPoints) {
        this.storyPoints = storyPoints;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
