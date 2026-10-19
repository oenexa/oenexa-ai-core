package ai.oenexa.testing.web;

import ai.oenexa.testing.domain.CoverageReport;
import ai.oenexa.testing.domain.GeneratedTestSuite;
import ai.oenexa.testing.domain.TestGenerationRequest;
import ai.oenexa.testing.service.TestingService;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/testing")
public class TestingController {

    private final TestingService testingService;

    public TestingController(TestingService testingService) {
        this.testingService = Objects.requireNonNull(testingService, "testingService must not be null");
    }

    @PostMapping("/generate")
    public ResponseEntity<GeneratedTestSuite> generate(@RequestBody TestGenerationRequest request) {
        return ResponseEntity.ok(testingService.generateTests(request));
    }

    @PostMapping("/evaluate")
    public ResponseEntity<CoverageReport> evaluate(@RequestBody Map<String, Object> payload) {
        String module = (String) payload.getOrDefault("module", "unspecified");
        int linesTotal = ((Number) payload.getOrDefault("linesTotal", 0)).intValue();
        int linesCovered = ((Number) payload.getOrDefault("linesCovered", 0)).intValue();
        int branchesTotal = ((Number) payload.getOrDefault("branchesTotal", 0)).intValue();
        int branchesCovered = ((Number) payload.getOrDefault("branchesCovered", 0)).intValue();
        double threshold = ((Number) payload.getOrDefault("threshold", 80.0)).doubleValue();

        CoverageReport report = testingService.evaluateCoverage(module, linesTotal, linesCovered, branchesTotal, branchesCovered, threshold);
        return ResponseEntity.ok(report);
    }
}
