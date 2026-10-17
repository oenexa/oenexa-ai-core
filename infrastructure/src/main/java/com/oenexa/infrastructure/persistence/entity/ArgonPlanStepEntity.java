package com.oenexa.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "argon_plan_steps")
public class ArgonPlanStepEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "goal_id", nullable = false, length = 36)
    private String goalId;

    @Column(name = "step_index", nullable = false)
    private int stepIndex = 0;

    @Column(name = "agent_persona", nullable = false, length = 50)
    private String agentPersona;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "tool_to_invoke", length = 100)
    private String toolToInvoke;

    @Column(nullable = false, length = 50)
    private String status = "PENDING";

    @Column(columnDefinition = "TEXT")
    private String output;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public ArgonPlanStepEntity() {}

    public ArgonPlanStepEntity(String id, String goalId, int stepIndex, String agentPersona, String description, String toolToInvoke) {
        this.id = id;
        this.goalId = goalId;
        this.stepIndex = stepIndex;
        this.agentPersona = agentPersona;
        this.description = description;
        this.toolToInvoke = toolToInvoke;
        this.status = "PENDING";
        this.retryCount = 0;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getGoalId() { return goalId; }
    public void setGoalId(String goalId) { this.goalId = goalId; }
    public int getStepIndex() { return stepIndex; }
    public void setStepIndex(int stepIndex) { this.stepIndex = stepIndex; }
    public String getAgentPersona() { return agentPersona; }
    public void setAgentPersona(String agentPersona) { this.agentPersona = agentPersona; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getToolToInvoke() { return toolToInvoke; }
    public void setToolToInvoke(String toolToInvoke) { this.toolToInvoke = toolToInvoke; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getOutput() { return output; }
    public void setOutput(String output) { this.output = output; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
