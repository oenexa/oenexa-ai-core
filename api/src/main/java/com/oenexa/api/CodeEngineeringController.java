package com.oenexa.api;

import com.oenexa.application.CodeEngineeringService;
import com.oenexa.domain.CodeEngineeringDomain.CodeAnalysisRequest;
import com.oenexa.domain.CodeEngineeringDomain.CodeAnalysisResponse;
import com.oenexa.domain.CodeEngineeringDomain.SecurityScanRequest;
import com.oenexa.domain.CodeEngineeringDomain.SecurityScanResponse;
import com.oenexa.domain.CodeEngineeringDomain.TestGenerationRequest;
import com.oenexa.domain.CodeEngineeringDomain.TestGenerationResponse;
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

    public CodeEngineeringController(CodeEngineeringService codeEngineeringService) {
        this.codeEngineeringService = Objects.requireNonNull(codeEngineeringService, "codeEngineeringService must not be null");
    }

    @PostMapping("/code/analyze")
    public ResponseEntity<CodeAnalysisResponse> analyzeCode(@RequestBody CodeAnalysisRequest request) {
        if (request == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(codeEngineeringService.analyzeAst(request));
    }

    @PostMapping("/security/scan")
    public ResponseEntity<SecurityScanResponse> scanSecurity(@RequestBody SecurityScanRequest request) {
        if (request == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(codeEngineeringService.auditSecurity(request));
    }

    @PostMapping("/testing/generate")
    public ResponseEntity<TestGenerationResponse> generateTests(@RequestBody TestGenerationRequest request) {
        if (request == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(codeEngineeringService.synthesizeTests(request));
    }
}
