package com.oenexa.api.controller;

import com.oenexa.api.dto.ApiResponse;
import com.oenexa.api.dto.code.CodeDtos.CodeAnalysisRequestDto;
import com.oenexa.api.dto.code.CodeDtos.CodeAnalysisResultDto;
import com.oenexa.api.dto.code.CodeDtos.SecurityAuditSummaryDto;
import com.oenexa.api.dto.code.CodeDtos.TestSynthesisResultDto;
import com.oenexa.api.mapper.CodeApiMapper;
import com.oenexa.application.CodeEngineeringService;
import com.oenexa.application.security.CodeSecurityPolicy;
import com.oenexa.application.validation.CodeEngineeringValidator;
import com.oenexa.domain.CodeEngineeringDomain.CodeAnalysisRequest;
import com.oenexa.domain.CodeEngineeringDomain.CodeAnalysisResponse;
import com.oenexa.domain.CodeEngineeringDomain.SecurityScanRequest;
import com.oenexa.domain.CodeEngineeringDomain.SecurityScanResponse;
import com.oenexa.domain.CodeEngineeringDomain.TestGenerationRequest;
import com.oenexa.domain.CodeEngineeringDomain.TestGenerationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * Enterprise REST Controller exposing Code Engineering endpoints:
 * 1. AST Inspection & Cyclomatic Complexity Analysis (/api/v1/code/analyze)
 * 2. SAST Vulnerability & Secret Leak Auditing (/api/v1/security/scan)
 * 3. Synthesized Zero-Mock JUnit 5 Test Suite Generation (/api/v1/testing/generate)
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class CodeEngineeringController {

    private final CodeEngineeringService codeEngineeringService;
    private final CodeEngineeringValidator codeEngineeringValidator;
    private final CodeSecurityPolicy codeSecurityPolicy;
    private final CodeApiMapper codeApiMapper;

    public CodeEngineeringController(CodeEngineeringService codeEngineeringService) {
        this(codeEngineeringService, new CodeEngineeringValidator(), new CodeSecurityPolicy(), new CodeApiMapper());
    }

    @Autowired
    public CodeEngineeringController(
            CodeEngineeringService codeEngineeringService,
            CodeEngineeringValidator codeEngineeringValidator,
            CodeSecurityPolicy codeSecurityPolicy,
            CodeApiMapper codeApiMapper
    ) {
        this.codeEngineeringService = Objects.requireNonNull(codeEngineeringService, "codeEngineeringService must not be null");
        this.codeEngineeringValidator = codeEngineeringValidator != null ? codeEngineeringValidator : new CodeEngineeringValidator();
        this.codeSecurityPolicy = codeSecurityPolicy != null ? codeSecurityPolicy : new CodeSecurityPolicy();
        this.codeApiMapper = codeApiMapper != null ? codeApiMapper : new CodeApiMapper();
    }

    public CodeEngineeringValidator getCodeEngineeringValidator() {
        return codeEngineeringValidator;
    }

    public CodeSecurityPolicy getCodeSecurityPolicy() {
        return codeSecurityPolicy;
    }

    public CodeApiMapper getCodeApiMapper() {
        return codeApiMapper;
    }

    @PostMapping("/v2/code/analyze")
    public ResponseEntity<ApiResponse<CodeAnalysisResultDto>> analyzeCodeV2(@RequestBody CodeAnalysisRequestDto requestDto) {
        if (requestDto == null) {
            return ResponseEntity.badRequest().build();
        }
        CodeAnalysisRequest request = codeApiMapper.toDomainAnalysisRequest(requestDto);
        codeEngineeringValidator.validateAnalysisRequest(request);
        codeSecurityPolicy.auditCodeSecurity(request.code());
        CodeAnalysisResponse response = codeEngineeringService.analyzeAst(request);
        return ResponseEntity.ok(ApiResponse.ok("Code AST analysis completed", codeApiMapper.toAnalysisResultDto(response)));
    }

    @PostMapping("/v2/security/scan")
    public ResponseEntity<ApiResponse<SecurityAuditSummaryDto>> scanSecurityV2(@RequestBody SecurityScanRequest request) {
        if (request == null) {
            return ResponseEntity.badRequest().build();
        }
        codeEngineeringValidator.validateSecurityScan(request);
        codeSecurityPolicy.auditCodeSecurity(request.code());
        SecurityScanResponse response = codeEngineeringService.auditSecurity(request);
        return ResponseEntity.ok(ApiResponse.ok("Security SAST audit completed", codeApiMapper.toSecuritySummaryDto(response)));
    }

    @PostMapping("/v2/testing/generate")
    public ResponseEntity<ApiResponse<TestSynthesisResultDto>> generateTestsV2(@RequestBody TestGenerationRequest request) {
        if (request == null) {
            return ResponseEntity.badRequest().build();
        }
        codeEngineeringValidator.validateTestGeneration(request);
        TestGenerationResponse response = codeEngineeringService.synthesizeTests(request);
        return ResponseEntity.ok(ApiResponse.ok("Zero-mock test synthesis completed", codeApiMapper.toTestResultDto(response)));
    }

    @PostMapping("/code/analyze")
    public ResponseEntity<CodeAnalysisResponse> analyzeCode(@RequestBody CodeAnalysisRequest request) {
        if (request == null) {
            return ResponseEntity.badRequest().build();
        }
        codeEngineeringValidator.validateAnalysisRequest(request);
        codeSecurityPolicy.auditCodeSecurity(request.code());
        return ResponseEntity.ok(codeEngineeringService.analyzeAst(request));
    }

    @PostMapping("/security/scan")
    public ResponseEntity<SecurityScanResponse> scanSecurity(@RequestBody SecurityScanRequest request) {
        if (request == null) {
            return ResponseEntity.badRequest().build();
        }
        codeEngineeringValidator.validateSecurityScan(request);
        codeSecurityPolicy.auditCodeSecurity(request.code());
        return ResponseEntity.ok(codeEngineeringService.auditSecurity(request));
    }

    @PostMapping("/testing/generate")
    public ResponseEntity<TestGenerationResponse> generateTests(@RequestBody TestGenerationRequest request) {
        if (request == null) {
            return ResponseEntity.badRequest().build();
        }
        codeEngineeringValidator.validateTestGeneration(request);
        return ResponseEntity.ok(codeEngineeringService.synthesizeTests(request));
    }
}
