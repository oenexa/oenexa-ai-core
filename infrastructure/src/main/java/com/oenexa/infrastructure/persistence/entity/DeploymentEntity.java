package com.oenexa.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "deployments")
public class DeploymentEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "project_id", nullable = false, length = 36)
    private String projectId;

    @Column(nullable = false, length = 50)
    private String environment = "PRODUCTION";

    @Column(nullable = false, length = 50)
    private String version;

    @Column(nullable = false, length = 50)
    private String status = "DEPLOYED";

    @Column(name = "deployed_at", nullable = false)
    private Instant deployedAt = Instant.now();

    protected DeploymentEntity() {}

    public DeploymentEntity(String id, String projectId, String environment, String version) {
        this.id = id;
        this.projectId = projectId;
        this.environment = environment != null ? environment : "PRODUCTION";
        this.version = version;
        this.status = "DEPLOYED";
        this.deployedAt = Instant.now();
    }

    public String getId() { return id; }
    public String getProjectId() { return projectId; }
    public String getEnvironment() { return environment; }
    public String getVersion() { return version; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getDeployedAt() { return deployedAt; }
}
