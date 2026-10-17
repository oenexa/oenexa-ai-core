package com.oenexa.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "argon_execution_events")
public class ArgonExecutionEventEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "goal_id", nullable = false, length = 36)
    private String goalId;

    @Column(nullable = false, length = 50)
    private String phase;

    @Column(name = "agent_persona", nullable = false, length = 50)
    private String agentPersona;

    @Column(name = "thought_process", columnDefinition = "TEXT")
    private String thoughtProcess;

    @Column(name = "tool_action", length = 100)
    private String toolAction;

    @Column(name = "output_snippet", columnDefinition = "TEXT")
    private String outputSnippet;

    @Column(nullable = false, length = 50)
    private String status = "SUCCESS";

    @Column(nullable = false)
    private Instant timestamp = Instant.now();

    public ArgonExecutionEventEntity() {}

    public ArgonExecutionEventEntity(String id, String goalId, String phase, String agentPersona, String thoughtProcess, String toolAction, String outputSnippet, String status) {
        this.id = id;
        this.goalId = goalId;
        this.phase = phase;
        this.agentPersona = agentPersona;
        this.thoughtProcess = thoughtProcess;
        this.toolAction = toolAction;
        this.outputSnippet = outputSnippet;
        this.status = status;
        this.timestamp = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getGoalId() { return goalId; }
    public void setGoalId(String goalId) { this.goalId = goalId; }
    public String getPhase() { return phase; }
    public void setPhase(String phase) { this.phase = phase; }
    public String getAgentPersona() { return agentPersona; }
    public void setAgentPersona(String agentPersona) { this.agentPersona = agentPersona; }
    public String getThoughtProcess() { return thoughtProcess; }
    public void setThoughtProcess(String thoughtProcess) { this.thoughtProcess = thoughtProcess; }
    public String getToolAction() { return toolAction; }
    public void setToolAction(String toolAction) { this.toolAction = toolAction; }
    public String getOutputSnippet() { return outputSnippet; }
    public void setOutputSnippet(String outputSnippet) { this.outputSnippet = outputSnippet; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
