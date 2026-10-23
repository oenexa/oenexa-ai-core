package com.oenexa.api.mapper;

import com.oenexa.api.dto.code.CodeDtos.CodeAnalysisRequestDto;
import com.oenexa.api.dto.code.CodeDtos.CodeAnalysisResultDto;
import com.oenexa.api.dto.code.CodeDtos.SecurityAuditSummaryDto;
import com.oenexa.api.dto.code.CodeDtos.TestSynthesisResultDto;
import com.oenexa.domain.model.code.CodeEngineeringDomain.CodeAnalysisRequest;
import com.oenexa.domain.model.code.CodeEngineeringDomain.CodeAnalysisResponse;
import com.oenexa.domain.model.code.CodeEngineeringDomain.SecurityScanResponse;
import com.oenexa.domain.model.code.CodeEngineeringDomain.TestGenerationResponse;
import org.springframework.stereotype.Component;

/**
 * Enterprise API Mapper for Code Intelligence DTOs.
 */
@Component
public class CodeApiMapper {

    public CodeAnalysisRequest toDomainAnalysisRequest(CodeAnalysisRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return new CodeAnalysisRequest(dto.sourceCode(), dto.language());
    }

    public CodeAnalysisResultDto toAnalysisResultDto(CodeAnalysisResponse response) {
        if (response == null) {
            return null;
        }
        return new CodeAnalysisResultDto(
                response.language(),
                response.totalLines(),
                response.classCount(),
                response.methodCount(),
                response.cyclomaticComplexity(),
                response.codeSmells(),
                response.clean()
        );
    }

    public SecurityAuditSummaryDto toSecuritySummaryDto(SecurityScanResponse response) {
        if (response == null) {
            return null;
        }
        return new SecurityAuditSummaryDto(
                response.scanId(),
                response.passed(),
                response.totalIssues(),
                response.vulnerabilities(),
                response.compliance(),
                response.timestamp()
        );
    }

    public TestSynthesisResultDto toTestResultDto(TestGenerationResponse response) {
        if (response == null) {
            return null;
        }
        return new TestSynthesisResultDto(
                response.suiteName(),
                response.framework(),
                response.testCode(),
                response.estimatedCoveragePercent(),
                response.zeroMockEnforced()
        );
    }
}

