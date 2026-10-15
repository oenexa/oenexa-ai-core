package ai.oenexa.devops;

import ai.oenexa.devops.domain.DevopsFinding;
import ai.oenexa.devops.domain.DiagnosisSeverity;
import ai.oenexa.devops.domain.ManifestAnalysisRequest;
import ai.oenexa.devops.domain.ManifestAnalysisResult;
import ai.oenexa.devops.domain.ManifestType;
import ai.oenexa.devops.domain.PipelineGenerationRequest;
import ai.oenexa.devops.domain.PipelineGenerationResult;
import ai.oenexa.devops.service.DefaultDevopsService;
import ai.oenexa.devops.web.DevopsController;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OENEXA DevOps Service & Controller Tests (Zero-Mock Policy)")
class DevopsServiceTest {

    private DefaultDevopsService devopsService;
    private DevopsController devopsController;

    @BeforeEach
    void setUp() {
        devopsService = new DefaultDevopsService();
        devopsController = new DevopsController(devopsService);
    }

    @Test
    @DisplayName("DevopsModule metadata and initialization")
    void testDevopsModuleMetadata() {
        DevopsModule module = new DevopsModule();
        assertEquals("devops", module.id());
        assertEquals("DevOps Center", module.displayName());
        assertTrue(module.capabilities().contains("dockerfile-analysis"));
    }

    @Test
    @DisplayName("ManifestType and DiagnosisSeverity enum invariants")
    void testEnumInvariants() {
        assertEquals(4, ManifestType.values().length);
        assertEquals(ManifestType.DOCKERFILE, ManifestType.valueOf("DOCKERFILE"));

        assertEquals(3, DiagnosisSeverity.values().length);
        assertEquals(DiagnosisSeverity.CRITICAL, DiagnosisSeverity.valueOf("CRITICAL"));
    }

    @Test
    @DisplayName("Domain records null safety and fallback invariants")
    void testDomainRecordsEdgeCases() {
        DevopsFinding finding = new DevopsFinding("RULE-1", DiagnosisSeverity.INFO, null, "message", null);
        assertEquals("general", finding.resource());
        assertEquals("", finding.remediation());

        assertThrows(NullPointerException.class, () -> new DevopsFinding(null, DiagnosisSeverity.INFO, "r", "m", ""));
        assertThrows(NullPointerException.class, () -> new DevopsFinding("R", null, "r", "m", ""));
        assertThrows(NullPointerException.class, () -> new DevopsFinding("R", DiagnosisSeverity.INFO, "r", null, ""));

        ManifestAnalysisRequest req = new ManifestAnalysisRequest("Dockerfile", null, null);
        assertEquals(ManifestType.DOCKERFILE, req.type());
        assertEquals("", req.content());

        assertThrows(NullPointerException.class, () -> new ManifestAnalysisRequest(null, ManifestType.DOCKERFILE, ""));

        ManifestAnalysisResult res = new ManifestAnalysisResult("Dockerfile", ManifestType.DOCKERFILE, true, 100, null, null);
        assertTrue(res.findings().isEmpty());
        assertEquals("", res.summary());
        assertEquals(0, res.criticalCount());
        assertEquals(0, res.warningCount());

        assertThrows(NullPointerException.class, () -> new ManifestAnalysisResult(null, ManifestType.DOCKERFILE, true, 100, List.of(), ""));
        assertThrows(NullPointerException.class, () -> new ManifestAnalysisResult("D", null, true, 100, List.of(), ""));

        PipelineGenerationRequest pReq = new PipelineGenerationRequest(null, null, null, false);
        assertEquals("github-actions", pReq.targetPlatform());
        assertEquals("java", pReq.language());
        assertEquals("gradle", pReq.buildTool());

        PipelineGenerationResult pRes = new PipelineGenerationResult("file", null, null);
        assertEquals("", pRes.content());
        assertTrue(pRes.stages().isEmpty());

        assertThrows(NullPointerException.class, () -> new PipelineGenerationResult(null, "", List.of()));
    }

    @Test
    @DisplayName("Analyze Dockerfile with bad practices: latest tag, root user, missing healthcheck, single stage")
    void testAnalyzeVulnerableDockerfile() {
        String dockerfile = """
                FROM openjdk:latest
                COPY . /app
                CMD ["java", "-jar", "app.jar"]
                """;
        ManifestAnalysisRequest req = new ManifestAnalysisRequest("Dockerfile", ManifestType.DOCKERFILE, dockerfile);
        ManifestAnalysisResult result = devopsService.analyzeManifest(req);

        assertFalse(result.healthy());
        assertTrue(result.warningCount() >= 2);
        assertTrue(result.findings().stream().anyMatch(f -> f.ruleId().equals("DOCK-TAG-001")));
        assertTrue(result.findings().stream().anyMatch(f -> f.ruleId().equals("DOCK-USER-001")));
        assertTrue(result.findings().stream().anyMatch(f -> f.ruleId().equals("DOCK-HLTH-001")));
        assertTrue(result.findings().stream().anyMatch(f -> f.ruleId().equals("DOCK-STG-001")));
    }

    @Test
    @DisplayName("Analyze clean production-grade multi-stage Dockerfile")
    void testAnalyzeCleanDockerfile() {
        String cleanDockerfile = """
                FROM eclipse-temurin:25-jdk AS builder
                WORKDIR /build
                COPY . .
                RUN ./gradlew assemble

                FROM eclipse-temurin:25-jre
                USER 10001:10001
                HEALTHCHECK CMD curl -f http://127.0.0.1:8080/actuator/health || exit 1
                COPY --from=builder /build/app.jar /app.jar
                ENTRYPOINT ["java", "-jar", "/app.jar"]
                """;
        ManifestAnalysisRequest req = new ManifestAnalysisRequest("Dockerfile", ManifestType.DOCKERFILE, cleanDockerfile);
        ManifestAnalysisResult result = devopsService.analyzeManifest(req);

        assertTrue(result.healthy());
        assertEquals(100, result.healthScore());
        assertEquals(0, result.findings().size());
        assertTrue(result.summary().contains("HEALTHY"));
    }

    @Test
    @DisplayName("Analyze Kubernetes manifest with privileged container and missing limits/probes")
    void testAnalyzeVulnerableKubernetes() {
        String k8sManifest = """
                apiVersion: apps/v1
                kind: Deployment
                metadata:
                  name: backend
                spec:
                  template:
                    spec:
                      containers:
                        - name: app
                          image: backend:1.0
                          securityContext:
                            privileged: true
                """;
        ManifestAnalysisRequest req = new ManifestAnalysisRequest("deployment.yaml", ManifestType.KUBERNETES_MANIFEST, k8sManifest);
        ManifestAnalysisResult result = devopsService.analyzeManifest(req);

        assertFalse(result.healthy());
        assertTrue(result.criticalCount() >= 1);
        assertTrue(result.findings().stream().anyMatch(f -> f.ruleId().equals("K8S-SEC-001")));
        assertTrue(result.findings().stream().anyMatch(f -> f.ruleId().equals("K8S-RES-001")));
        assertTrue(result.findings().stream().anyMatch(f -> f.ruleId().equals("K8S-PRB-001")));
    }

    @Test
    @DisplayName("Analyze Docker Compose and Helm values configurations")
    void testAnalyzeComposeAndHelm() {
        String composeWithoutNetworks = "version: '3.8'\nservices:\n  web:\n    image: nginx\n";
        ManifestAnalysisResult compRes = devopsService.analyzeManifest(new ManifestAnalysisRequest("docker-compose.yml", ManifestType.DOCKER_COMPOSE, composeWithoutNetworks));
        assertTrue(compRes.findings().stream().anyMatch(f -> f.ruleId().equals("COMP-NET-001")));

        String composeWithNetworks = "version: '3.8'\nservices:\n  web:\n    image: nginx\nnetworks:\n  app-net:\n";
        ManifestAnalysisResult compClean = devopsService.analyzeManifest(new ManifestAnalysisRequest("docker-compose.yml", ManifestType.DOCKER_COMPOSE, composeWithNetworks));
        assertTrue(compClean.findings().isEmpty());

        String helmLatest = "image:\n  repository: oenexa\n  tag: latest\n";
        ManifestAnalysisResult helmRes = devopsService.analyzeManifest(new ManifestAnalysisRequest("values.yaml", ManifestType.HELM_VALUES, helmLatest));
        assertTrue(helmRes.findings().stream().anyMatch(f -> f.ruleId().equals("HELM-TAG-001")));

        String helmSemantic = "image:\n  repository: oenexa\n  tag: 1.2.0\n";
        ManifestAnalysisResult helmClean = devopsService.analyzeManifest(new ManifestAnalysisRequest("values.yaml", ManifestType.HELM_VALUES, helmSemantic));
        assertTrue(helmClean.findings().isEmpty());
    }

    @Test
    @DisplayName("Generate CI/CD pipelines for GitHub Actions and GitLab CI")
    void testGeneratePipelines() {
        PipelineGenerationRequest ghReq = new PipelineGenerationRequest("github-actions", "java", "gradle", true);
        PipelineGenerationResult ghRes = devopsService.generatePipeline(ghReq);
        assertEquals(".github/workflows/ci.yml", ghRes.filename());
        assertTrue(ghRes.content().contains("uses: actions/setup-java@v4"));
        assertTrue(ghRes.content().contains("java-version: '25'"));
        assertTrue(ghRes.content().contains("./docker-runner.sh up"));
        assertEquals(4, ghRes.stages().size());

        PipelineGenerationRequest gitlabReq = new PipelineGenerationRequest("gitlab", "java", "gradle", false);
        PipelineGenerationResult gitlabRes = devopsService.generatePipeline(gitlabReq);
        assertEquals(".gitlab-ci.yml", gitlabRes.filename());
        assertTrue(gitlabRes.content().contains("build_job:"));
        assertTrue(gitlabRes.content().contains("test_job:"));
    }

    @Test
    @DisplayName("DevopsController endpoints return valid HTTP responses")
    void testControllerEndpoints() {
        ManifestAnalysisRequest mReq = new ManifestAnalysisRequest("Dockerfile", ManifestType.DOCKERFILE, "FROM alpine:3.19\n");
        ResponseEntity<ManifestAnalysisResult> mResp = devopsController.analyze(mReq);
        assertEquals(200, mResp.getStatusCode().value());
        assertNotNull(mResp.getBody());

        PipelineGenerationRequest pReq = new PipelineGenerationRequest("github", "java", "gradle", false);
        ResponseEntity<PipelineGenerationResult> pResp = devopsController.pipeline(pReq);
        assertEquals(200, pResp.getStatusCode().value());
        assertNotNull(pResp.getBody());
    }
}
