package ai.oenexa.pipeline;

import ai.oenexa.pipeline.domain.CiProvider;
import ai.oenexa.pipeline.domain.FailureCategory;
import ai.oenexa.pipeline.domain.PipelineAnalysisRequest;
import ai.oenexa.pipeline.domain.PipelineAnalysisResult;
import ai.oenexa.pipeline.domain.PipelineFailureDiagnosis;
import ai.oenexa.pipeline.service.DefaultPipelineService;
import ai.oenexa.pipeline.web.PipelineController;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OENEXA Pipeline Service & Controller Tests (Zero-Mock Policy)")
class PipelineServiceTest {

    private DefaultPipelineService pipelineService;
    private PipelineController pipelineController;

    @BeforeEach
    void setUp() {
        pipelineService = new DefaultPipelineService();
        pipelineController = new PipelineController(pipelineService);
    }

    @Test
    @DisplayName("PipelineModule metadata and initialization")
    void testPipelineModuleMetadata() {
        PipelineModule module = new PipelineModule();
        assertEquals("pipeline", module.id());
        assertEquals("CI/CD Engine", module.displayName());
        assertTrue(module.capabilities().contains("failure-analysis"));
    }

    @Test
    @DisplayName("CiProvider and FailureCategory enum invariants")
    void testEnumInvariants() {
        assertEquals(4, CiProvider.values().length);
        assertEquals(CiProvider.GITHUB_ACTIONS, CiProvider.valueOf("GITHUB_ACTIONS"));

        assertEquals(6, FailureCategory.values().length);
        assertEquals(FailureCategory.COMPILATION_ERROR, FailureCategory.valueOf("COMPILATION_ERROR"));
    }

    @Test
    @DisplayName("Domain records null safety and fallback invariants")
    void testDomainRecordsEdgeCases() {
        PipelineFailureDiagnosis diag = new PipelineFailureDiagnosis("F-1", FailureCategory.TIMEOUT, "Timeout exceeded", null, null, 85);
        assertEquals("unknown", diag.failedStep());
        assertEquals("", diag.proposedFix());

        assertThrows(NullPointerException.class, () -> new PipelineFailureDiagnosis(null, FailureCategory.TIMEOUT, "cause", "", "", 90));
        assertThrows(NullPointerException.class, () -> new PipelineFailureDiagnosis("F", null, "cause", "", "", 90));
        assertThrows(NullPointerException.class, () -> new PipelineFailureDiagnosis("F", FailureCategory.TIMEOUT, null, "", "", 90));

        PipelineAnalysisRequest req = new PipelineAnalysisRequest("ci", null, null, null);
        assertEquals(CiProvider.GITHUB_ACTIONS, req.provider());
        assertEquals("", req.buildLogs());
        assertEquals("", req.pipelineConfig());

        assertThrows(NullPointerException.class, () -> new PipelineAnalysisRequest(null, CiProvider.GITHUB_ACTIONS, "", ""));

        PipelineAnalysisResult res = new PipelineAnalysisResult("ci", CiProvider.GITHUB_ACTIONS, true, null, null, null);
        assertTrue(res.diagnoses().isEmpty());
        assertEquals("", res.repairedConfig());
        assertEquals("", res.summary());
        assertFalse(res.hasFailures());

        assertThrows(NullPointerException.class, () -> new PipelineAnalysisResult(null, CiProvider.GITHUB_ACTIONS, true, List.of(), "", ""));
        assertThrows(NullPointerException.class, () -> new PipelineAnalysisResult("ci", null, true, List.of(), "", ""));
    }

    @Test
    @DisplayName("Diagnose compilation failure in build logs")
    void testDiagnoseCompilationFailure() {
        String log = """
                > Task :api:compileJava FAILED
                C:\\workspace\\App.java:12: error: cannot find symbol
                    symbol:   class MissingClass
                Compilation failed with 1 error.
                """;
        PipelineAnalysisRequest req = new PipelineAnalysisRequest("Build", CiProvider.GITHUB_ACTIONS, log, "steps: [run: ./gradlew build]");
        PipelineAnalysisResult result = pipelineService.analyzePipeline(req);

        assertFalse(result.passed());
        assertTrue(result.hasFailures());
        assertTrue(result.diagnoses().stream().anyMatch(d -> d.category() == FailureCategory.COMPILATION_ERROR));
    }

    @Test
    @DisplayName("Diagnose test failure and dependency resolution failure")
    void testDiagnoseTestAndDependencyFailure() {
        String log = """
                > Task :oenexa-auth:test FAILED
                AssertionFailedError: expected 200 but was 401
                Could not resolve com.oenexa:oenexa-core:1.0.0
                job exceeded timeout
                """;
        PipelineAnalysisRequest req = new PipelineAnalysisRequest("Test-Job", CiProvider.GITLAB_CI, log, "");
        PipelineAnalysisResult result = pipelineService.analyzePipeline(req);

        assertFalse(result.passed());
        assertEquals(3, result.diagnoses().size());
        assertTrue(result.diagnoses().stream().anyMatch(d -> d.category() == FailureCategory.TEST_FAILURE));
        assertTrue(result.diagnoses().stream().anyMatch(d -> d.category() == FailureCategory.DEPENDENCY_RESOLUTION));
        assertTrue(result.diagnoses().stream().anyMatch(d -> d.category() == FailureCategory.TIMEOUT));
    }

    @Test
    @DisplayName("Diagnose permission denied on gradlew and auto-inject chmod +x")
    void testDiagnosePermissionDeniedAndRepair() {
        String log = "/bin/sh: ./gradlew: Permission denied";
        String config = """
                name: CI
                jobs:
                  build:
                    steps:
                      - run: ./gradlew build
                """;
        PipelineAnalysisRequest req = new PipelineAnalysisRequest("Permission-Job", CiProvider.GITHUB_ACTIONS, log, config);
        PipelineAnalysisResult result = pipelineService.analyzePipeline(req);

        assertFalse(result.passed());
        assertTrue(result.diagnoses().stream().anyMatch(d -> d.category() == FailureCategory.PERMISSION_DENIED));
        assertTrue(result.repairedConfig().contains("chmod +x gradlew && ./gradlew"));
    }

    @Test
    @DisplayName("Clean build logs pass with zero diagnoses")
    void testCleanPipelinePasses() {
        String log = "BUILD SUCCESSFUL in 8s\n7 actionable tasks executed\n";
        PipelineAnalysisRequest req = new PipelineAnalysisRequest("Clean-Job", CiProvider.GITHUB_ACTIONS, log, "steps: []");
        PipelineAnalysisResult result = pipelineService.analyzePipeline(req);

        assertTrue(result.passed());
        assertFalse(result.hasFailures());
        assertEquals(0, result.diagnoses().size());
        assertTrue(result.summary().contains("SUCCESSFUL"));
    }

    @Test
    @DisplayName("Repair configuration edge cases")
    void testRepairConfigEdgeCases() {
        String defaultRepaired = pipelineService.repairConfiguration(null, "ANY");
        assertTrue(defaultRepaired.contains("chmod +x gradlew && ./gradlew check"));

        String depRepaired = pipelineService.repairConfiguration("run: ./gradlew build", "DEPENDENCY_RESOLUTION");
        assertTrue(depRepaired.contains("--refresh-dependencies"));
    }

    @Test
    @DisplayName("PipelineController endpoints return valid HTTP responses")
    void testControllerEndpoints() {
        PipelineAnalysisRequest req = new PipelineAnalysisRequest("API-Test", CiProvider.GITHUB_ACTIONS, "BUILD SUCCESSFUL", "");
        ResponseEntity<PipelineAnalysisResult> aResp = pipelineController.analyze(req);
        assertEquals(200, aResp.getStatusCode().value());
        assertNotNull(aResp.getBody());
        assertTrue(aResp.getBody().passed());

        ResponseEntity<Map<String, String>> rResp = pipelineController.repair(Map.of("config", "./gradlew build", "reason", "DEPENDENCY_RESOLUTION"));
        assertEquals(200, rResp.getStatusCode().value());
        assertNotNull(rResp.getBody());
        assertTrue(rResp.getBody().get("repairedConfig").contains("--refresh-dependencies"));
    }
}
