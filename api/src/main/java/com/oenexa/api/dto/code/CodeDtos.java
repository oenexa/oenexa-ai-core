package com.oenexa.api.dto.code;

import java.time.Instant;
import java.util.List;

/**
 * Standardized DTO Layer for Code Intelligence & AST Domain.
 */
public class CodeDtos {

    public record CodeAnalysisRequestDto(
            String sourceCode,
            String language,
            String filePath
    ) {
        public CodeAnalysisRequestDto {
            sourceCode = (sourceCode != null) ? sourceCode : "";
            language = (language != null && !language.isBlank()) ? language : "java";
            filePath = (filePath != null && !filePath.isBlank()) ? filePath : "Solution.java";
        }
    }

    public record CodeAnalysisResultDto(
            String language,
            int lines,
            int classCount,
            int methodCount,
            int complexity,
            List<String> smells,
            boolean clean
    ) {
        public CodeAnalysisResultDto {
            smells = (smells != null) ? List.copyOf(smells) : List.of();
        }
    }

    public record SecurityAuditSummaryDto(
            String scanId,
            boolean passed,
            int totalVulnerabilities,
            List<String> findings,
            String complianceStandard,
            Instant timestamp
    ) {
        public SecurityAuditSummaryDto {
            findings = (findings != null) ? List.copyOf(findings) : List.of();
            complianceStandard = (complianceStandard != null) ? complianceStandard : "OWASP Top 10";
            timestamp = (timestamp != null) ? timestamp : Instant.now();
        }
    }

    public record TestSynthesisResultDto(
            String suiteName,
            String framework,
            String testCode,
            int estimatedCoverage,
            boolean zeroMockVerified
    ) {
        public TestSynthesisResultDto {
            suiteName = (suiteName != null) ? suiteName : "SolutionTest";
            framework = (framework != null) ? framework : "JUNIT_5";
            testCode = (testCode != null) ? testCode : "";
        }
    }
}
