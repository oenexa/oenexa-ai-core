package com.oenexa.application.mapper;

import com.oenexa.domain.CodeEngineeringDomain.CodeAnalysisResponse;
import com.oenexa.domain.CodeEngineeringDomain.SecurityScanResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Enterprise Mapper Layer for Code Intelligence Domain.
 * Maps between source code analysis requests, AST parsing outputs, and SAST security audit summaries.
 */
@Component
public class CodeDtoMapper {

    public CodeAnalysisResponse toAnalysisResponse(
            String language,
            int totalLines,
            int classCount,
            int methodCount,
            List<String> classes,
            List<String> methods,
            List<String> imports
    ) {
        return new CodeAnalysisResponse(
                language != null ? language : "java",
                totalLines,
                classCount,
                methodCount,
                classCount + methodCount,
                classes != null ? classes : List.of(),
                methods != null ? methods : List.of(),
                imports != null ? imports : List.of(),
                List.of(),
                true
        );
    }

    public SecurityScanResponse toSecurityResponse(
            boolean passed,
            int totalIssues,
            List<String> vulnerabilities,
            List<String> secrets,
            String summary
    ) {
        return new SecurityScanResponse(
                UUID.randomUUID().toString(),
                passed,
                totalIssues,
                0,
                0,
                0,
                totalIssues,
                vulnerabilities != null ? vulnerabilities : List.of(),
                secrets != null ? secrets : List.of(),
                summary != null ? summary : "Zero-mock SAST compliance check passed",
                "OWASP Top 10 Monolith Verification",
                Instant.now()
        );
    }
}
