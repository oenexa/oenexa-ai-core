package com.oenexa.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "local_agent_audit_logs")
public class LocalAgentAuditLogEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "goal_id", length = 36)
    private String goalId;

    @Column(name = "local_only_mode", nullable = false)
    private boolean localOnlyMode = true;

    @Column(name = "telemetry_disabled", nullable = false)
    private boolean telemetryDisabled = true;

    @Column(name = "local_model_endpoint", nullable = false, length = 255)
    private String localModelEndpoint;

    @Column(name = "selected_local_model", nullable = false, length = 100)
    private String selectedLocalModel;

    @Column(name = "max_vram_allocation_mb", nullable = false)
    private int maxVramAllocationMb = 8192;

    @Column(name = "air_gapped_verification", nullable = false)
    private boolean airGappedVerification = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public LocalAgentAuditLogEntity() {}

    public LocalAgentAuditLogEntity(String id, String goalId, boolean localOnlyMode, boolean telemetryDisabled, String localModelEndpoint, String selectedLocalModel, int maxVramAllocationMb, boolean airGappedVerification) {
        this.id = id;
        this.goalId = goalId;
        this.localOnlyMode = localOnlyMode;
        this.telemetryDisabled = telemetryDisabled;
        this.localModelEndpoint = localModelEndpoint;
        this.selectedLocalModel = selectedLocalModel;
        this.maxVramAllocationMb = maxVramAllocationMb;
        this.airGappedVerification = airGappedVerification;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getGoalId() { return goalId; }
    public void setGoalId(String goalId) { this.goalId = goalId; }
    public boolean isLocalOnlyMode() { return localOnlyMode; }
    public void setLocalOnlyMode(boolean localOnlyMode) { this.localOnlyMode = localOnlyMode; }
    public boolean isTelemetryDisabled() { return telemetryDisabled; }
    public void setTelemetryDisabled(boolean telemetryDisabled) { this.telemetryDisabled = telemetryDisabled; }
    public String getLocalModelEndpoint() { return localModelEndpoint; }
    public void setLocalModelEndpoint(String localModelEndpoint) { this.localModelEndpoint = localModelEndpoint; }
    public String getSelectedLocalModel() { return selectedLocalModel; }
    public void setSelectedLocalModel(String selectedLocalModel) { this.selectedLocalModel = selectedLocalModel; }
    public int getMaxVramAllocationMb() { return maxVramAllocationMb; }
    public void setMaxVramAllocationMb(int maxVramAllocationMb) { this.maxVramAllocationMb = maxVramAllocationMb; }
    public boolean isAirGappedVerification() { return airGappedVerification; }
    public void setAirGappedVerification(boolean airGappedVerification) { this.airGappedVerification = airGappedVerification; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
