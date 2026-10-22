package com.oenexa.application.vnext;

import com.oenexa.domain.OenexaVNextDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OenexaVNext Application Engines Zero-Mock Tests")
class OenexaVNextApplicationTest {

    private IssueClassifier issueClassifier;
    private RootCauseEngine rootCauseEngine;
    private RiskAssessmentEngine riskAssessmentEngine;
    private ArchitectureValidator architectureValidator;
    private DependencyAnalyzer dependencyAnalyzer;
    private AutonomousResolutionEngine resolutionEngine;
    private LearningEngineV2 learningEngine;
    private DeepReasoningEngine deepReasoningEngine;
    private SelfUpgradeEngine selfUpgradeEngine;
    private MemoryEvolutionManager memoryManager;
    private ArchitectureEvolutionManager architectureEvolutionManager;
    private QualityGateV2 qualityGate;
    private OenexaAutonomousEngineeringSystem engineeringSystem;

    @BeforeEach
    void setUp() {
        issueClassifier = new IssueClassifier();
        rootCauseEngine = new RootCauseEngine();
        riskAssessmentEngine = new RiskAssessmentEngine();
        architectureValidator = new ArchitectureValidator();
        dependencyAnalyzer = new DependencyAnalyzer();
        resolutionEngine = new AutonomousResolutionEngine();
        learningEngine = new LearningEngineV2();
        deepReasoningEngine = new DeepReasoningEngine();
        selfUpgradeEngine = new SelfUpgradeEngine();
        memoryManager = new MemoryEvolutionManager();
        architectureEvolutionManager = new ArchitectureEvolutionManager();
        qualityGate = new QualityGateV2();

        engineeringSystem = new OenexaAutonomousEngineeringSystem(
                issueClassifier, resolutionEngine, learningEngine,
                deepReasoningEngine, selfUpgradeEngine, memoryManager,
                architectureEvolutionManager, qualityGate
        );
    }

    @Test
    @DisplayName("Phase 1: IssueClassifier classifies all 8 detection categories accurately")
    void testIssueClassification() {
        assertEquals(VNextDetectionType.BUG, issueClassifier.classify("NullPointerException at line 42", null).type());
        assertEquals(VNextDetectionType.MEMORY_LEAK, issueClassifier.classify("OutOfMemory heap space", null).type());
        assertEquals(VNextDetectionType.SECURITY_RISK, issueClassifier.classify("SQL injection vulnerability", null).type());
        assertEquals(VNextDetectionType.RUNTIME_FAILURE, issueClassifier.classify("Connection refused timeout crash", null).type());
        assertEquals(VNextDetectionType.ARCHITECTURE_VIOLATION, issueClassifier.classify("Domain depends on framework leak", null).type());
        assertEquals(VNextDetectionType.PERFORMANCE_BOTTLENECK, issueClassifier.classify("High latency slow query thread starvation", null).type());
        assertEquals(VNextDetectionType.DEPENDENCY_CONFLICT, issueClassifier.classify("NoSuchMethodError incompatible version conflict", null).type());
        assertEquals(VNextDetectionType.UNKNOWN_SCENARIO, issueClassifier.classify("General inquiry on roadmap", null).type());
    }

    @Test
    @DisplayName("Phase 1: RootCauseEngine & RiskAssessmentEngine analyze causes and compute risk")
    void testRootCauseAndRisk() {
        ClassifiedIssue issue = issueClassifier.classify("NullPointerException in UserRegistry", null);
        RootCauseReport root = rootCauseEngine.analyzeRootCause(issue);
        assertEquals("CWE-476", root.cweClassification());
        assertFalse(root.evidenceChain().isEmpty());

        RiskAssessment risk = riskAssessmentEngine.evaluateRisk(issue, root);
        assertTrue(risk.riskScore() >= 0.70);
        assertTrue(risk.requiresImmediateMitigation());
    }

    @Test
    @DisplayName("Phase 1: ArchitectureValidator & DependencyAnalyzer enforce structural invariants")
    void testArchitectureAndDependencies() {
        var violations = architectureValidator.validateArchitecture("com.oenexa.domain.user", List.of("org.springframework.stereotype.Service"));
        assertEquals(1, violations.size());
        assertEquals("DOMAIN_FRAMEWORK_LEAKAGE", violations.get(0).violationType());

        var conflicts = dependencyAnalyzer.analyzeDependencies(
                Map.of("io.netty", "4.1.100.Final"),
                Map.of("io.netty", "4.1.95.Final")
        );
        assertEquals(1, conflicts.size());
        assertEquals("io.netty", conflicts.get(0).dependencyName());
    }

    @Test
    @DisplayName("Phase 2: AutonomousResolutionEngine synthesizes and verifies solutions")
    void testAutonomousResolution() {
        ClassifiedIssue issue = issueClassifier.classify("NullPointerException dereference", null);
        AutonomousResolutionResult res = resolutionEngine.resolveIssue(issue);
        assertNotNull(res.resolutionId());
        assertTrue(res.safetyVerified());
        assertTrue(res.validationPassed());
    }

    @Test
    @DisplayName("Phase 3: LearningEngineV2 stores patterns and adjusts confidence")
    void testSelfLearningV2() {
        int initialCount = learningEngine.getTotalLearnedPatterns();
        assertTrue(initialCount >= 5);

        VNextPattern p = learningEngine.recordExecutionOutcome("PAT-ZERO-MOCK", true, "Test assertion pass");
        assertTrue(p.successTally() > 0);
        assertTrue(learningEngine.evaluatePatternConfidence("PAT-ZERO-MOCK") > 0.85);

        learningEngine.ingestUserFeedback("PAT-ZERO-MOCK", true, "Excellent quality");
        learningEngine.ingestCodeReviewDecision("PAT-ZERO-MOCK", true, "LGTM");
    }

    @Test
    @DisplayName("Phase 4: DeepReasoningEngine generates hypotheses and synthesized plans")
    void testDeepReasoning() {
        DeepReasoningPlan plan = deepReasoningEngine.reasonDeeply("How to achieve zero-telemetry local-first AI?");
        assertFalse(plan.subProblems().isEmpty());
        assertFalse(plan.evaluatedHypotheses().isEmpty());
        assertTrue(plan.finalConfidenceScore() >= 0.90);
        assertTrue(plan.synthesizedAnswer().contains("Deep Reasoning"));
    }

    @Test
    @DisplayName("Phase 5: SelfUpgradeEngine detects missing capabilities and generates artifacts")
    void testSelfUpgrade() {
        Optional<CapabilityGenerationArtifact> artifactOpt = selfUpgradeEngine.evaluateAndUpgrade("We need a Blockchain Analyzer for smart contract audits");
        assertTrue(artifactOpt.isPresent());
        CapabilityGenerationArtifact artifact = artifactOpt.get();
        assertEquals("BlockchainAnalyzer", artifact.capabilityName());
        assertTrue(artifact.javaInterfaceCode().contains("BlockchainAnalyzerPort"));
        assertTrue(artifact.javaImplementationCode().contains("BlockchainAnalyzerAdapter"));
        assertTrue(selfUpgradeEngine.isCapabilityRegistered("BlockchainAnalyzer"));
        assertEquals(1, selfUpgradeEngine.getRegisteredCount());
    }

    @Test
    @DisplayName("Phase 6 & 7: Memory consolidation and Architecture layer plans")
    void testMemoryAndArchitectureEvolution() {
        List<String> raw = List.of("entry1", "entry2", "entry1", "entry3");
        MemoryConsolidationReport report = memoryManager.consolidateMemory(raw);
        assertEquals(3, report.entriesConsolidated());
        assertEquals(1, report.duplicatesEliminated());
        assertEquals(7, memoryManager.getActiveTiers().size());

        ArchitectureEvolutionPlan domainPlan = architectureEvolutionManager.evaluateLayerEvolution("DOMAIN");
        assertEquals("DOMAIN", domainPlan.targetLayer());
        assertTrue(domainPlan.backwardCompatible());
    }

    @Test
    @DisplayName("Phase 9: QualityGateV2 evaluates multi-vector validation")
    void testQualityGateV2() {
        QualityGateV2Report report = qualityGate.evaluate("### Valid Architecture\nPure records verified", false, false);
        assertTrue(report.allPassed());
        assertEquals(1.0, report.aggregateScore(), 0.001);
        assertTrue(report.acceptedForDelivery());

        QualityGateV2Report failingSecurity = qualityGate.evaluate("code with eval(userInput)", true, false);
        assertFalse(failingSecurity.acceptedForDelivery());
    }

    @Test
    @DisplayName("Phase 10: Master continuous improvement loop runs end-to-end")
    void testMasterContinuousImprovementLoop() {
        var result = engineeringSystem.executeContinuousImprovementLoop("Analyze NullPointerException in AuthService", Map.of("env", "prod"));
        assertNotNull(result.cycleId());
        assertEquals(VNextDetectionType.BUG, result.issue().type());
        assertTrue(result.deliveredSuccessfully());
        assertEquals(1, engineeringSystem.getCycleCount());
    }
}
