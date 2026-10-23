package com.oenexa.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@Entity
@Table(name = "user_memories")
public class UserMemoryEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "memory_type", nullable = false, length = 50)
    private String memoryType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "confidence_score", nullable = false)
    private double confidenceScore = 1.0;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public UserMemoryEntity() {}

    public UserMemoryEntity(String id, String userId, String memoryType, String content, double confidenceScore) {
        this.id = id;
        this.userId = userId;
        this.memoryType = memoryType;
        this.content = content;
        this.confidenceScore = confidenceScore;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

}
