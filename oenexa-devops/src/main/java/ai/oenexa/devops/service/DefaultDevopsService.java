package ai.oenexa.devops.service;

import ai.oenexa.devops.domain.DevopsFinding;
import ai.oenexa.devops.domain.DiagnosisSeverity;
import ai.oenexa.devops.domain.ManifestAnalysisRequest;
import ai.oenexa.devops.domain.ManifestAnalysisResult;
import ai.oenexa.devops.domain.ManifestType;
import ai.oenexa.devops.domain.PipelineGenerationRequest;
import ai.oenexa.devops.domain.PipelineGenerationResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class DefaultDevopsService implements DevopsService {

    @Override
    public ManifestAnalysisResult analyzeManifest(ManifestAnalysisRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        String filename = request.filename();
        ManifestType type = request.type();
        String content = request.content();

        List<DevopsFinding> findings = new ArrayList<>();

        switch (type) {
            case DOCKERFILE -> {
                if (content.contains(":latest")) {
                    findings.add(new DevopsFinding(
                            "DOCK-TAG-001",
                            DiagnosisSeverity.WARNING,
                            filename,
                            "Using unpinned ':latest' tag for base image creates unpredictable builds",
                            "Pin an exact version or digest (e.g., eclipse-temurin:25-jdk-alpine)"
                    ));
                }
                if (!content.contains("USER ") || content.contains("USER root")) {
                    findings.add(new DevopsFinding(
                            "DOCK-USER-001",
                            DiagnosisSeverity.WARNING,
                            filename,
                            "Container runs as root user by default, increasing privilege escalation risk",
                            "Add a non-root user (e.g., USER 10001:10001 or USER spring)"
                    ));
                }
                if (!content.contains("HEALTHCHECK")) {
                    findings.add(new DevopsFinding(
                            "DOCK-HLTH-001",
                            DiagnosisSeverity.INFO,
                            filename,
                            "No HEALTHCHECK instruction configured for container liveness monitoring",
                            "Add HEALTHCHECK CMD curl -f http://127.0.0.1:8080/actuator/health || exit 1"
                    ));
                }
                if (!content.contains(" AS ") && !content.contains(" as ")) {
                    findings.add(new DevopsFinding(
                            "DOCK-STG-001",
                            DiagnosisSeverity.INFO,
                            filename,
                            "Single-stage Dockerfile may bundle unnecessary build tools and bloated layers into runtime image",
                            "Refactor into multi-stage build with distinct build and JRE runtime stages"
                    ));
                }
            }
            case KUBERNETES_MANIFEST -> {
                if (content.contains("privileged: true")) {
                    findings.add(new DevopsFinding(
                            "K8S-SEC-001",
                            DiagnosisSeverity.CRITICAL,
                            filename,
                            "Container configured with privileged: true, granting root access to host node",
                            "Disable privileged mode and apply least-privilege Linux capabilities"
                    ));
                }
                if (!content.contains("limits:") && !content.contains("resources:")) {
                    findings.add(new DevopsFinding(
                            "K8S-RES-001",
                            DiagnosisSeverity.WARNING,
                            filename,
                            "Pod spec lacks CPU and memory resource limits, risking node resource exhaustion",
                            "Define resources.limits and resources.requests (e.g. cpu: 500m, memory: 512Mi)"
                    ));
                }
                if (!content.contains("livenessProbe") || !content.contains("readinessProbe")) {
                    findings.add(new DevopsFinding(
                            "K8S-PRB-001",
                            DiagnosisSeverity.WARNING,
                            filename,
                            "Missing Kubernetes liveness or readiness probes",
                            "Add livenessProbe and readinessProbe targeting actuator endpoints"
                    ));
                }
            }
            case DOCKER_COMPOSE -> {
                if (!content.contains("networks:")) {
                    findings.add(new DevopsFinding(
                            "COMP-NET-001",
                            DiagnosisSeverity.INFO,
                            filename,
                            "Compose file does not define custom isolated bridge networks",
                            "Define a custom bridge network for service isolation"
                    ));
                }
            }
            case HELM_VALUES -> {
                if (!content.contains("tag:") || content.contains("tag: latest")) {
                    findings.add(new DevopsFinding(
                            "HELM-TAG-001",
                            DiagnosisSeverity.WARNING,
                            filename,
                            "Helm values file uses unspecified or 'latest' image tag",
                            "Specify deterministic semantic version tag in values.yaml"
                    ));
                }
            }
        }

        long critical = findings.stream().filter(f -> f.severity() == DiagnosisSeverity.CRITICAL).count();
        long warning = findings.stream().filter(f -> f.severity() == DiagnosisSeverity.WARNING).count();
        long info = findings.stream().filter(f -> f.severity() == DiagnosisSeverity.INFO).count();

        int score = (int) Math.max(0, 100 - (critical * 30) - (warning * 15) - (info * 5));
        boolean healthy = critical == 0 && score >= 70;

        String summary = String.format("DevOps Analysis for %s: %s (Health Score: %d/100, Critical: %d, Warning: %d, Info: %d).",
                filename,
                healthy ? "HEALTHY" : "NEEDS_REMEDIATION",
                score,
                critical,
                warning,
                info
        );

        return new ManifestAnalysisResult(filename, type, healthy, score, findings, summary);
    }

    @Override
    public PipelineGenerationResult generatePipeline(PipelineGenerationRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        String platform = request.targetPlatform();
        String buildTool = request.buildTool();
        boolean docker = request.dockerPublish();

        List<String> stages = new ArrayList<>(List.of("Checkout", "Setup Environment", "Build & Test"));
        if (docker) {
            stages.add("Docker Container Build & Publish");
        }

        String filename;
        String content;

        if ("gitlab".equalsIgnoreCase(platform)) {
            filename = ".gitlab-ci.yml";
            content = """
                    stages:
                      - build
                      - test
                      - docker

                    build_job:
                      stage: build
                      script:
                        - ./gradlew --no-daemon assemble

                    test_job:
                      stage: test
                      script:
                        - ./gradlew --no-daemon check
                    """;
        } else {
            filename = ".github/workflows/ci.yml";
            content = """
                    name: CI Pipeline

                    on:
                      push:
                        branches: [ main, develop ]
                      pull_request:
                        branches: [ main ]

                    jobs:
                      build:
                        runs-on: ubuntu-latest
                        steps:
                          - name: Checkout Repository
                            uses: actions/checkout@v4

                          - name: Set up JDK 25
                            uses: actions/setup-java@v4
                            with:
                              java-version: '25'
                              distribution: 'temurin'
                              cache: '%s'

                          - name: Run Build and Tests
                            run: ./run.sh test && ./run.sh build
                    """.formatted(buildTool);

            if (docker) {
                content += """

                          - name: Build Docker Container
                            run: ./docker-runner.sh up
                    """;
            }
        }

        return new PipelineGenerationResult(filename, content.stripIndent(), stages);
    }
}
