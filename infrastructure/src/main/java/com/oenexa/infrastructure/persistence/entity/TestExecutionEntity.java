package com.oenexa.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "tests")
public class TestExecutionEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "repository_id", nullable = false, length = 36)
    private String repositoryId;

    @Column(name = "test_framework", nullable = false, length = 50)
    private String testFramework = "JUNIT5";

    @Column(name = "test_count", nullable = false)
    private int testCount = 0;

    @Column(name = "passed_count", nullable = false)
    private int passedCount = 0;

    @Column(name = "failed_count", nullable = false)
    private int failedCount = 0;

    @Column(name = "coverage_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal coveragePct = BigDecimal.ZERO;

    @Column(name = "run_at", nullable = false)
    private Instant runAt = Instant.now();

    protected TestExecutionEntity() {}

    public TestExecutionEntity(String id, String repositoryId, String testFramework, int testCount, int passedCount, int failedCount, BigDecimal coveragePct) {
        this.id = id;
        this.repositoryId = repositoryId;
        this.testFramework = testFramework != null ? testFramework : "JUNIT5";
        this.testCount = testCount;
        this.passedCount = passedCount;
        this.failedCount = failedCount;
        this.coveragePct = coveragePct != null ? coveragePct : BigDecimal.ZERO;
        this.runAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public String getTestFramework() {
        return testFramework;
    }

    public int getTestCount() {
        return testCount;
    }

    public int getPassedCount() {
        return passedCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public BigDecimal getCoveragePct() {
        return coveragePct;
    }

    public Instant getRunAt() {
        return runAt;
    }
}
