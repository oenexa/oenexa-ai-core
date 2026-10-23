package com.oenexa.application.service.code;

import com.oenexa.application.port.*;
import com.oenexa.application.service.agent.*;
import com.oenexa.application.service.auth.*;
import com.oenexa.application.service.chat.*;
import com.oenexa.application.service.code.*;
import com.oenexa.application.service.knowledge.*;
import com.oenexa.application.service.memory.*;
import com.oenexa.application.service.orchestration.*;

import com.oenexa.application.engine.AstAnalysisEngine;
import com.oenexa.application.engine.SecurityAuditEngine;
import com.oenexa.application.engine.TestSynthesizerEngine;
import com.oenexa.domain.model.code.CodeEngineeringDomain.CodeAnalysisRequest;
import com.oenexa.domain.model.code.CodeEngineeringDomain.CodeAnalysisResponse;
import com.oenexa.domain.model.code.CodeEngineeringDomain.SecurityScanRequest;
import com.oenexa.domain.model.code.CodeEngineeringDomain.SecurityScanResponse;
import com.oenexa.domain.model.code.CodeEngineeringDomain.TestGenerationRequest;
import com.oenexa.domain.model.code.CodeEngineeringDomain.TestGenerationResponse;
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


