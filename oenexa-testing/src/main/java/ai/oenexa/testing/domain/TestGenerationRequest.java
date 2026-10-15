package ai.oenexa.testing.domain;

import java.util.Objects;

/**
 * Request payload for synthesizing automated test suites.
 */
public record TestGenerationRequest(
        String targetComponent,
        String sourceCode,
        String language,
        TestFramework framework,
        TestType type
) {
    public TestGenerationRequest {
        Objects.requireNonNull(targetComponent, "targetComponent must not be null");
        if (sourceCode == null) {
            sourceCode = "";
        }
        if (language == null || language.isBlank()) {
            language = "java";
        }
        if (framework == null) {
            framework = TestFramework.JUNIT5;
        }
        if (type == null) {
            type = TestType.UNIT;
        }
    }
}
