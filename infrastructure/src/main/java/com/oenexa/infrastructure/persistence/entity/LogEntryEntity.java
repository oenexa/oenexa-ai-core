package com.oenexa.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "logs")
public class LogEntryEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "repository_id", length = 36)
    private String repositoryId;

    @Column(name = "scan_id", length = 36)
    private String scanId;

    @Column(name = "log_level", nullable = false, length = 20)
    private String logLevel = "INFO";

    @Column(nullable = false, length = 100)
    private String source;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    @Column(nullable = false)
    private Instant timestamp = Instant.now();

    protected LogEntryEntity() {}

    public LogEntryEntity(String id, String repositoryId, String scanId, String logLevel, String source, String message) {
        this.id = id;
        this.repositoryId = repositoryId;
        this.scanId = scanId;
        this.logLevel = logLevel != null ? logLevel : "INFO";
        this.source = source;
        this.message = message;
        this.timestamp = Instant.now();
    }

    public String getId() { return id; }
    public String getRepositoryId() { return repositoryId; }
    public String getScanId() { return scanId; }
    public String getLogLevel() { return logLevel; }
    public String getSource() { return source; }
    public String getMessage() { return message; }
    public String getStackTrace() { return stackTrace; }
    public void setStackTrace(String stackTrace) { this.stackTrace = stackTrace; }
    public Instant getTimestamp() { return timestamp; }
}
