package ai.oenexa.pipeline.domain;

/**
 * Root cause category for CI/CD workflow and build failures.
 */
public enum FailureCategory {
    COMPILATION_ERROR,
    TEST_FAILURE,
    DEPENDENCY_RESOLUTION,
    TIMEOUT,
    PERMISSION_DENIED,
    SYNTAX_ERROR
}
