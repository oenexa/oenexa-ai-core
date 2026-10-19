package ai.oenexa.testing.domain;

import java.util.List;
import java.util.Objects;

/**
 * Result payload containing a complete runnable test suite and coverage estimates.
 */
public record GeneratedTestSuite(
        String suiteName,
        TestFramework framework,
        TestType type,
        String testCode,
        List<TestCase> testCases,
        int estimatedCoveragePercent
) {
    public GeneratedTestSuite {
        Objects.requireNonNull(suiteName, "suiteName must not be null");
        Objects.requireNonNull(framework, "framework must not be null");
        Objects.requireNonNull(type, "type must not be null");
        if (testCode == null) {
            testCode = "";
        }
        if (testCases == null) {
            testCases = List.of();
        }
    }

    public boolean hasTests() {
        return !testCases.isEmpty();
    }
}
