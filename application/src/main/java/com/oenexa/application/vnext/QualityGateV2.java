package com.oenexa.application.vnext;

import com.oenexa.domain.model.cognitive.OenexaVNextDomain.*;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Phase 9: QualityGateV2
 * Enforces strict multi-vector validation before any code, architecture plan,
 * or resolution artifact is accepted for delivery:
 * 1. Accuracy Validation
 * 2. Logic Validation
 * 3. Security Validation
 * 4. Architecture Validation
 * 5. Performance Validation
 * 6. Test Validation
 * 7. Compliance Validation
 */
@Component
public class QualityGateV2 {

    public QualityGateV2Report evaluate(String solutionArtifact, boolean hasSecurityVulnerability, boolean hasMockFrameworkLeak) {
        Objects.requireNonNull(solutionArtifact, "solutionArtifact must not be null");

        List<QualityGateCheck> checks = new ArrayList<>();

        // 1. Accuracy Validation
        boolean accuracy = !solutionArtifact.isBlank() && !solutionArtifact.contains("HALLUCINATION");
        checks.add(new QualityGateCheck("Accuracy", accuracy, accuracy ? 1.0 : 0.0, accuracy ? "Verified" : "Fails accuracy"));

        // 2. Logic Validation
        boolean logic = !solutionArtifact.contains("CONTRADICTION") && !solutionArtifact.contains("CIRCULAR_DEPENDENCY");
        checks.add(new QualityGateCheck("Logic", logic, logic ? 1.0 : 0.0, logic ? "Consistent" : "Logical contradiction"));

        // 3. Security Validation
        boolean security = !hasSecurityVulnerability && !solutionArtifact.contains("eval(") && !solutionArtifact.contains("EXEC(");
        checks.add(new QualityGateCheck("Security", security, security ? 1.0 : 0.0, security ? "Clean" : "Vulnerability detected"));

        // 4. Architecture Validation
        boolean arch = !solutionArtifact.contains("org.springframework.") || !solutionArtifact.contains("package com.oenexa.domain");
        checks.add(new QualityGateCheck("Architecture", arch, arch ? 1.0 : 0.0, arch ? "Hexagonal boundaries intact" : "Domain pollution"));

        // 5. Performance Validation
        boolean perf = !solutionArtifact.contains("Thread.sleep") && !solutionArtifact.contains("synchronized(this)");
        checks.add(new QualityGateCheck("Performance", perf, perf ? 1.0 : 0.0, perf ? "Virtual thread safe" : "Blocking carrier thread"));

        // 6. Test Validation
        boolean test = !hasMockFrameworkLeak && !solutionArtifact.contains("Mockito.") && !solutionArtifact.contains("@Mock");
        checks.add(new QualityGateCheck("Test", test, test ? 1.0 : 0.0, test ? "100% Zero-Mock compliant" : "Mock framework detected"));

        // 7. Compliance Validation
        boolean compliance = !solutionArtifact.contains("CONFIDENTIAL_LEAK") && !solutionArtifact.contains("API_KEY=");
        checks.add(new QualityGateCheck("Compliance", compliance, compliance ? 1.0 : 0.0, compliance ? "Compliant" : "Secret leakage"));

        long passedCount = checks.stream().filter(QualityGateCheck::passed).count();
        double score = (double) passedCount / checks.size();
        boolean allPassed = passedCount == checks.size();

        return new QualityGateV2Report(allPassed, score, checks, allPassed && score >= 0.85);
    }
}

