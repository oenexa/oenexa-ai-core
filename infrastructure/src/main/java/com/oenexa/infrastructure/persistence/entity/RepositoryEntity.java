package com.oenexa.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "repositories")
public class RepositoryEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "project_id", nullable = false, length = 36)
    private String projectId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "clone_url", nullable = false, length = 500)
    private String cloneUrl;

    @Column(nullable = false, length = 50)
    private String provider = "GITHUB";

    @Column(name = "default_branch", nullable = false, length = 100)
    private String defaultBranch = "main";

    @Column(nullable = false, length = 50)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected RepositoryEntity() {}

    public RepositoryEntity(String id, String projectId, String name, String cloneUrl, String provider, String defaultBranch) {
        this.id = id;
        this.projectId = projectId;
        this.name = name;
        this.cloneUrl = cloneUrl;
        this.provider = provider != null ? provider : "GITHUB";
        this.defaultBranch = defaultBranch != null ? defaultBranch : "main";
        this.status = "ACTIVE";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCloneUrl() { return cloneUrl; }
    public void setCloneUrl(String cloneUrl) { this.cloneUrl = cloneUrl; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getDefaultBranch() { return defaultBranch; }
    public void setDefaultBranch(String defaultBranch) { this.defaultBranch = defaultBranch; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
