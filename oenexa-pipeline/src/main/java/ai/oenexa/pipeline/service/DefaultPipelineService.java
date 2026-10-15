package ai.oenexa.pipeline.service;

import ai.oenexa.pipeline.domain.CiProvider;
import ai.oenexa.pipeline.domain.FailureCategory;
import ai.oenexa.pipeline.domain.PipelineAnalysisRequest;
import ai.oenexa.pipeline.domain.PipelineAnalysisResult;
import ai.oenexa.pipeline.domain.PipelineFailureDiagnosis;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class DefaultPipelineService implements PipelineService {

    @Override
    public PipelineAnalysisResult analyzePipeline(PipelineAnalysisRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        String name = request.pipelineName();
        CiProvider provider = request.provider();
        String logs = request.buildLogs();
        String config = request.pipelineConfig();

        List<PipelineFailureDiagnosis> diagnoses = new ArrayList<>();

        if (logs.contains("error: cannot find symbol") || logs.contains("Compilation failed")) {
            diagnoses.add(new PipelineFailureDiagnosis(
                    "DIAG-COMP-001",
                    FailureCategory.COMPILATION_ERROR,
                    "Java compilation failure due to unresolved symbol or syntax incompatibility",
                    "compileJava",
                    "Verify import statements, type parameters, and Java 25 source compatibility",
                    95
            ));
        }

        if (logs.contains("AssertionFailedError") || logs.contains("failed tests") || logs.matches("(?s).*tests completed, \\d+ failed.*")) {
            diagnoses.add(new PipelineFailureDiagnosis(
                    "DIAG-TEST-001",
                    FailureCategory.TEST_FAILURE,
                    "Automated test suite assertion failure detected in test execution phase",
                    "test",
                    "Inspect failed test assertion logs; verify zero-mock domain invariant expectations",
                    92
            ));
        }

        if (logs.contains("Could not resolve") || logs.contains("401 Unauthorized") || logs.contains("Could not find")) {
            diagnoses.add(new PipelineFailureDiagnosis(
                    "DIAG-DEP-001",
                    FailureCategory.DEPENDENCY_RESOLUTION,
                    "Artifact dependency resolution failed against remote Maven/Gradle repositories",
                    "resolveDependencies",
                    "Check repository URLs, credentials, or run Gradle with --refresh-dependencies",
                    90
            ));
        }

        if (logs.contains("Permission denied") || logs.contains("/gradlew: Permission denied")) {
            diagnoses.add(new PipelineFailureDiagnosis(
                    "DIAG-PERM-001",
                    FailureCategory.PERMISSION_DENIED,
                    "Executable bit not set on Gradle wrapper (gradlew) or runner shell script",
                    "checkout / setup",
                    "Add 'chmod +x gradlew' step in CI workflow before invoking build commands",
                    98
            ));
        }

        if (logs.contains("timed out") || logs.contains("TimeoutException") || logs.contains("job exceeded timeout")) {
            diagnoses.add(new PipelineFailureDiagnosis(
                    "DIAG-TIME-001",
                    FailureCategory.TIMEOUT,
                    "CI job exceeded maximum execution time threshold",
                    "execution",
                    "Increase 'timeout-minutes' setting or parallelize slow integration test suites",
                    85
            ));
        }

        boolean passed = diagnoses.isEmpty();
        String repairedConfig = passed ? config : repairConfiguration(config, diagnoses.get(0).category().name());
        String summary = String.format("Pipeline Analysis for '%s' (%s): %s with %d failure diagnosis(es).",
                name,
                provider,
                passed ? "SUCCESSFUL" : "FAILED",
                diagnoses.size()
        );

        return new PipelineAnalysisResult(name, provider, passed, diagnoses, repairedConfig, summary);
    }

    @Override
    public String repairConfiguration(String brokenConfig, String failureReason) {
        if (brokenConfig == null || brokenConfig.isBlank()) {
            return """
                    name: CI
                    on: [push, pull_request]
                    jobs:
                      build:
                        runs-on: ubuntu-latest
                        steps:
                          - uses: actions/checkout@v4
                          - run: chmod +x gradlew && ./gradlew check
                    """.stripIndent();
        }

        String repaired = brokenConfig;
        if ("PERMISSION_DENIED".equalsIgnoreCase(failureReason) || failureReason.contains("PERM")) {
            if (!repaired.contains("chmod +x")) {
                repaired = repaired.replace("./gradlew", "chmod +x gradlew && ./gradlew");
            }
        } else if ("DEPENDENCY_RESOLUTION".equalsIgnoreCase(failureReason)) {
            if (!repaired.contains("--refresh-dependencies")) {
                repaired = repaired.replace("./gradlew build", "./gradlew build --refresh-dependencies");
            }
        }

        return repaired;
    }
}
