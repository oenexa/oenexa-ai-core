package com.oenexa.application.service.code;

import com.oenexa.application.port.*;
import com.oenexa.application.service.agent.*;
import com.oenexa.application.service.auth.*;
import com.oenexa.application.service.chat.*;
import com.oenexa.application.service.code.*;
import com.oenexa.application.service.knowledge.*;
import com.oenexa.application.service.memory.*;
import com.oenexa.application.service.orchestration.*;

import com.oenexa.application.engine.ApiGeneratorEngine;
import com.oenexa.application.engine.InfrastructureAutomationEngine;
import com.oenexa.application.engine.PullRequestReviewEngine;
import com.oenexa.application.engine.RefactoringEngine;
import com.oenexa.domain.model.code.AiEngineerDomain.*;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Enterprise Application Service for Phase 4: AI Software Engineer.
 * Coordinates Pull Request Reviews, REST API Generation, Autonomous Bug Fixing,
 * Code Refactoring, and DevOps Infrastructure Automation.
 */
@Service
public class AiSoftwareEngineerService {

    private final PullRequestReviewEngine prReviewEngine;
    private final ApiGeneratorEngine apiGeneratorEngine;
    private final RefactoringEngine refactoringEngine;
    private final InfrastructureAutomationEngine infraEngine;

    public AiSoftwareEngineerService() {
        this(new PullRequestReviewEngine(), new ApiGeneratorEngine(), new RefactoringEngine(), new InfrastructureAutomationEngine());
    }

    public AiSoftwareEngineerService(
            PullRequestReviewEngine prReviewEngine,
            ApiGeneratorEngine apiGeneratorEngine,
            RefactoringEngine refactoringEngine,
            InfrastructureAutomationEngine infraEngine
    ) {
        this.prReviewEngine = Objects.requireNonNull(prReviewEngine, "prReviewEngine must not be null");
        this.apiGeneratorEngine = Objects.requireNonNull(apiGeneratorEngine, "apiGeneratorEngine must not be null");
        this.refactoringEngine = Objects.requireNonNull(refactoringEngine, "refactoringEngine must not be null");
        this.infraEngine = Objects.requireNonNull(infraEngine, "infraEngine must not be null");
    }

    public PrReviewResult reviewPullRequest(PrReviewRequest request) {
        return prReviewEngine.reviewPr(request);
    }

    public ApiGenerationResult generateApi(ApiGenerationRequest request) {
        return apiGeneratorEngine.generateApi(request);
    }

    public RefactoringResult refactorCode(RefactoringRequest request) {
        return refactoringEngine.refactorCode(request);
    }

    public BugFixResult fixBug(BugFixRequest request) {
        return refactoringEngine.fixBug(request);
    }

    public InfrastructureAutomationResult generateInfrastructure(InfrastructureAutomationRequest request) {
        return infraEngine.generateInfrastructure(request);
    }
}


