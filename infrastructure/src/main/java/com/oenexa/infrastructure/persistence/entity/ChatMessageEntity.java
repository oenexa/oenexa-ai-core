package com.oenexa.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "chat_messages")
public class ChatMessageEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "session_id", nullable = false, length = 36)
    private String sessionId;

    @Column(nullable = false, length = 30)
    private String role;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "token_count", nullable = false)
    private int tokenCount = 0;

    @Column(name = "model_used", length = 50)
    private String modelUsed;

    @Column(name = "agent_type", length = 50)
    private String agentType;

    @Column(name = "ranking_score", nullable = false)
    private double rankingScore = 1.0;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public ChatMessageEntity() {}

    public ChatMessageEntity(String id, String sessionId, String role, String content, int tokenCount, String modelUsed, String agentType, double rankingScore) {
        this.id = id;
        this.sessionId = sessionId;
        this.role = role;
        this.content = content;
        this.tokenCount = tokenCount;
        this.modelUsed = modelUsed;
        this.agentType = agentType;
        this.rankingScore = rankingScore;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public int getTokenCount() { return tokenCount; }
    public void setTokenCount(int tokenCount) { this.tokenCount = tokenCount; }
    public String getModelUsed() { return modelUsed; }
    public void setModelUsed(String modelUsed) { this.modelUsed = modelUsed; }
    public String getAgentType() { return agentType; }
    public void setAgentType(String agentType) { this.agentType = agentType; }
    public double getRankingScore() { return rankingScore; }
    public void setRankingScore(double rankingScore) { this.rankingScore = rankingScore; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
