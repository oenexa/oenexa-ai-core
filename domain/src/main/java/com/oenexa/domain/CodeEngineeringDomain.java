package com.oenexa.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain records and models for Code Engineering, AST inspection,
 * SAST security auditing, and zero-mock test synthesis.
 */
public class CodeEngineeringDomain {

    public record CodeAnalysisRequest(String code, String language) {
        public CodeAnalysisRequest {
            if (code == null) {
                code = "";
            }
            if (language == null || language.isBlank()) {
                language = "java";
            }
        }
    }

    public record CodeAnalysisResponse(
            String language,
            int totalLines,
            int classCount,
            int methodCount,
            int cyclomaticComplexity,
            List<String> classes,
            List<String> methods,
            List<String> imports,
            List<String> codeSmells,
            boolean clean
    ) {
        public CodeAnalysisResponse {
            Objects.requireNonNull(language, "language must not be null");
            if (classes == null) {
                classes = List.of();
            }
            if (methods == null) {
                methods = List.of();
            }
            if (imports == null) {
                imports = List.of();
            }
            if (codeSmells == null) {
                codeSmells = List.of();
            }
        }
    }

    public record SecurityScanRequest(String code, String filename) {
        public SecurityScanRequest {
            if (code == null) {
                code = "";
            }
            if (filename == null || filename.isBlank()) {
                filename = "Solution.java";
            }
        }
    }

    public record SecurityScanResponse(
            String scanId,
            boolean passed,
            int totalIssues,
            int criticalCount,
            int highCount,
            int mediumCount,
            int lowCount,
            List<String> vulnerabilities,
            List<String> secrets,
            String summary,
            String compliance,
            Instant timestamp
    ) {
        public SecurityScanResponse {
            Objects.requireNonNull(scanId, "scanId must not be null");
            if (vulnerabilities == null) {
                vulnerabilities = List.of();
            }
            if (secrets == null) {
                secrets = List.of();
            }
            if (summary == null) {
                summary = "";
            }
            if (compliance == null) {
                compliance = "OWASP Top 10 Monolith Verification";
            }
            if (timestamp == null) {
                timestamp = Instant.now();
            }
        }

        public SecurityScanResponse(String scanId, boolean passed, int totalIssues, int criticalCount, int highCount, int mediumCount, int lowCount, List<String> vulnerabilities, List<String> secrets, String summary, String compliance) {
            this(scanId, passed, totalIssues, criticalCount, highCount, mediumCount, lowCount, vulnerabilities, secrets, summary, compliance, Instant.now());
        }
    }

    public record TestGenerationRequest(
            String className,
            List<String> methods,
            String framework,
            String sourceCode
    ) {
        public TestGenerationRequest(String className, List<String> methods, String framework) {
            this(className, methods, framework, "");
        }

        public TestGenerationRequest {
            if (className == null || className.isBlank()) {
                className = "Solution";
            }
            if (methods == null) {
                methods = List.of();
            }
            if (framework == null || framework.isBlank()) {
                framework = "JUNIT_5";
            }
            if (sourceCode == null) {
                sourceCode = "";
            }
        }
    }

    public record TestGenerationResponse(
            String suiteName,
            String framework,
            String code,
            String testCode,
            List<String> generatedTests,
            int estimatedCoveragePercent,
            boolean zeroMockEnforced
    ) {
        public TestGenerationResponse {
            Objects.requireNonNull(suiteName, "suiteName must not be null");
            Objects.requireNonNull(framework, "framework must not be null");
            if (code == null) {
                code = "";
            }
            if (testCode == null) {
                testCode = code;
            }
            if (generatedTests == null) {
                generatedTests = List.of();
            }
        }
    }
}
