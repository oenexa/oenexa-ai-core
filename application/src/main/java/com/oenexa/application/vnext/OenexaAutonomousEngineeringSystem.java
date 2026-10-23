package com.oenexa.application.vnext;

import com.oenexa.domain.model.cognitive.OenexaVNextDomain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Master Enterprise Service uniting all 10 VNext Upgrade Phases into a unified
 * autonomous self-improving AI Engineering Operating System.
 */
@Service
public class OenexaAutonomousEngineeringSystem {

    private final IssueClassifier issueClassifier;
    private final AutonomousResolutionEngine resolutionEngine;
    private final LearningEngineV2 learningEngine;
    private final DeepReasoningEngine deepReasoningEngine;
    private final SelfUpgradeEngine selfUpgradeEngine;
    private final MemoryEvolutionManager memoryManager;
    private final ArchitectureEvolutionManager architectureEvolutionManager;
    private final QualityGateV2 qualityGate;

    private final AtomicInteger continuousCycleCounter = new AtomicInteger(0);

    public record MasterVNextExecutionResult(
            String cycleId,
            ClassifiedIssue issue,
            AutonomousResolutionResult resolution,
            DeepReasoningPlan deepReasoning,
            Optional<CapabilityGenerationArtifact> upgradedCapability,
            QualityGateV2Report qualityReport,
            VNextPattern learnedPattern,
            boolean deliveredSuccessfully
    ) {}

    public OenexaAutonomousEngineeringSystem() {
        this(
                new IssueClassifier(),
                new AutonomousResolutionEngine(),
                new LearningEngineV2(),
                new DeepReasoningEngine(),
                new SelfUpgradeEngine(),
                new MemoryEvolutionManager(),
                new ArchitectureEvolutionManager(),
                new QualityGateV2()
        );
    }

    @Autowired
    public OenexaAutonomousEngineeringSystem(
            IssueClassifier issueClassifier,
            AutonomousResolutionEngine resolutionEngine,
            LearningEngineV2 learningEngine,
            DeepReasoningEngine deepReasoningEngine,
            SelfUpgradeEngine selfUpgradeEngine,
            MemoryEvolutionManager memoryManager,
            ArchitectureEvolutionManager architectureEvolutionManager,
            QualityGateV2 qualityGate
    ) {
        this.issueClassifier = Objects.requireNonNull(issueClassifier, "issueClassifier must not be null");
        this.resolutionEngine = Objects.requireNonNull(resolutionEngine, "resolutionEngine must not be null");
        this.learningEngine = Objects.requireNonNull(learningEngine, "learningEngine must not be null");
        this.deepReasoningEngine = Objects.requireNonNull(deepReasoningEngine, "deepReasoningEngine must not be null");
        this.selfUpgradeEngine = Objects.requireNonNull(selfUpgradeEngine, "selfUpgradeEngine must not be null");
        this.memoryManager = Objects.requireNonNull(memoryManager, "memoryManager must not be null");
        this.architectureEvolutionManager = Objects.requireNonNull(architectureEvolutionManager, "architectureEvolutionManager must not be null");
        this.qualityGate = Objects.requireNonNull(qualityGate, "qualityGate must not be null");
    }

    public MasterVNextExecutionResult executeContinuousImprovementLoop(String inputQueryOrError, Map<String, String> context) {
        Objects.requireNonNull(inputQueryOrError, "inputQueryOrError must not be null");
        String cycleId = "VNEXT-CYCLE-" + continuousCycleCounter.incrementAndGet();

        // 1. Detect
        ClassifiedIssue issue = issueClassifier.classify(inputQueryOrError, context);

        // 2. Resolve Automatically
        AutonomousResolutionResult resolution = resolutionEngine.resolveIssue(issue);

        // 3. Unknown Question Deep Reasoning (if unknown or high severity)
        DeepReasoningPlan deepReasoning = null;
        if (issue.type() == VNextDetectionType.UNKNOWN_SCENARIO || issue.severity().equals("CRITICAL")) {
            deepReasoning = deepReasoningEngine.reasonDeeply(inputQueryOrError);
        }

        // 4. Self-Upgrade (if missing capability required)
        Optional<CapabilityGenerationArtifact> upgradedCapability = selfUpgradeEngine.evaluateAndUpgrade(inputQueryOrError);

        // 5. Quality Gate V2 Validation
        String artifactContent = resolution.appliedFix().codeDiff() + " " + resolution.outcomeSummary();
        QualityGateV2Report qualityReport = qualityGate.evaluate(artifactContent, false, false);

        // 6. Learn
        VNextPattern learned = learningEngine.recordExecutionOutcome(
                "PAT-" + issue.type().name(),
                qualityReport.allPassed(),
                "Cycle: " + cycleId
        );

        return new MasterVNextExecutionResult(
                cycleId,
                issue,
                resolution,
                deepReasoning,
                upgradedCapability,
                qualityReport,
                learned,
                qualityReport.acceptedForDelivery()
        );
    }

    public MemoryEvolutionManager getMemoryManager() {
        return memoryManager;
    }

    public ArchitectureEvolutionManager getArchitectureEvolutionManager() {
        return architectureEvolutionManager;
    }

    public LearningEngineV2 getLearningEngine() {
        return learningEngine;
    }

    public SelfUpgradeEngine getSelfUpgradeEngine() {
        return selfUpgradeEngine;
    }

    public int getCycleCount() {
        return continuousCycleCounter.get();
    }
}

