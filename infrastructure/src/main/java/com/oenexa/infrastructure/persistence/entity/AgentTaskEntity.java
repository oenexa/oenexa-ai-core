package com.oenexa.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "agent_tasks")
public class AgentTaskEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "agent_type", nullable = false, length = 50)
    private String agentType;

    @Column(nullable = false, length = 50)
    private String status = "RUNNING";

    @Column(name = "input_payload", columnDefinition = "TEXT")
    private String inputPayload;

    @Column(name = "output_payload", columnDefinition = "TEXT")
    private String outputPayload;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;

    protected AgentTaskEntity() {}

    public AgentTaskEntity(String id, String agentType, String inputPayload) {
        this.id = id;
        this.agentType = agentType;
        this.inputPayload = inputPayload;
        this.status = "RUNNING";
        this.startedAt = Instant.now();
    }

    public String getId() { return id; }
    public String getAgentType() { return agentType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getInputPayload() { return inputPayload; }
    public void setInputPayload(String inputPayload) { this.inputPayload = inputPayload; }
    public String getOutputPayload() { return outputPayload; }
    public void setOutputPayload(String outputPayload) { this.outputPayload = outputPayload; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
