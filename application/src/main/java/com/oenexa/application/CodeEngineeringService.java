package com.oenexa.application;

import com.oenexa.application.engine.AstAnalysisEngine;
import com.oenexa.application.engine.SecurityAuditEngine;
import com.oenexa.application.engine.TestSynthesizerEngine;
import com.oenexa.domain.CodeEngineeringDomain.CodeAnalysisRequest;
import com.oenexa.domain.CodeEngineeringDomain.CodeAnalysisResponse;
import com.oenexa.domain.CodeEngineeringDomain.SecurityScanRequest;
import com.oenexa.domain.CodeEngineeringDomain.SecurityScanResponse;
import com.oenexa.domain.CodeEngineeringDomain.TestGenerationRequest;
import com.oenexa.domain.CodeEngineeringDomain.TestGenerationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Enterprise Service Facade for Code Engineering.
 * Coordinates AST analysis, static security vulnerability auditing, and zero-mock test synthesis.
 * Adheres strictly to the Single Responsibility Principle (SRP) by delegating to dedicated engines.
 */
@Service
public class CodeEngineeringService {

    private final AstAnalysisEngine astAnalysisEngine;
    private final SecurityAuditEngine securityAuditEngine;
    private final TestSynthesizerEngine testSynthesizerEngine;

    public CodeEngineeringService() {
        this(new AstAnalysisEngine(), new SecurityAuditEngine(), new TestSynthesizerEngine());
    }

    @Autowired
    public CodeEngineeringService(
            AstAnalysisEngine astAnalysisEngine,
            SecurityAuditEngine securityAuditEngine,
            TestSynthesizerEngine testSynthesizerEngine
    ) {
        this.astAnalysisEngine = Objects.requireNonNull(astAnalysisEngine, "astAnalysisEngine must not be null");
        this.securityAuditEngine = Objects.requireNonNull(securityAuditEngine, "securityAuditEngine must not be null");
        this.testSynthesizerEngine = Objects.requireNonNull(testSynthesizerEngine, "testSynthesizerEngine must not be null");
    }

    public CodeAnalysisResponse analyzeAst(CodeAnalysisRequest request) {
        return astAnalysisEngine.analyze(request);
    }

    public SecurityScanResponse auditSecurity(SecurityScanRequest request) {
        return securityAuditEngine.scan(request);
    }

    public TestGenerationResponse synthesizeTests(TestGenerationRequest request) {
        return testSynthesizerEngine.synthesize(request);
    }
}
