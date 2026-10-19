package com.oenexa.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Domain contracts for Phase 4: AI Software Engineer.
 * Models Pull Request reviews, API generation, autonomous bug fixing & refactoring,
 * architecture documentation, and infrastructure automation.
 */
public class AiEngineerDomain {

    public enum ReviewVerdict {
        APPROVE, REQUEST_CHANGES, COMMENT
    }

    public enum SupportedLanguage {
        JAVA, GO, RUST, TYPESCRIPT, JAVASCRIPT, PYTHON, KOTLIN, CPP, CSHARP, PHP
    }

    public enum SupportedFramework {
        SPRING_BOOT, REACT, ANGULAR, NEXT_JS, NEST_JS, EXPRESS, QUARKUS, MICRONAUT
    }

    public record PrReviewFileComment(
            String filePath,
            int lineNumber,
            String severity,
            String category,
            String comment,
            String suggestedFixDiff
    ) {
        public PrReviewFileComment {
            Objects.requireNonNull(filePath, "filePath must not be null");
            Objects.requireNonNull(comment, "comment must not be null");
            severity = (severity != null) ? severity : "INFO";
            category = (category != null) ? category : "CODE_QUALITY";
            suggestedFixDiff = (suggestedFixDiff != null) ? suggestedFixDiff : "";
        }
    }

    public record PrReviewRequest(
            String prTitle,
            String prDescription,
            String baseBranch,
            String headBranch,
            String diffContent
    ) {
        public PrReviewRequest {
            prTitle = (prTitle != null) ? prTitle : "Pull Request";
            prDescription = (prDescription != null) ? prDescription : "";
            baseBranch = (baseBranch != null) ? baseBranch : "main";
            headBranch = (headBranch != null) ? headBranch : "feature";
            diffContent = (diffContent != null) ? diffContent : "";
        }
    }

    public record PrReviewResult(
            String reviewId,
            ReviewVerdict verdict,
            int qualityScore,
            int securityScore,
            String summary,
            List<PrReviewFileComment> lineComments,
            List<String> keyStrengths,
            List<String> requiredChanges,
            boolean zeroMockCompliant,
            Instant timestamp
    ) {
        public PrReviewResult {
            Objects.requireNonNull(reviewId, "reviewId must not be null");
            Objects.requireNonNull(verdict, "verdict must not be null");
            lineComments = (lineComments != null) ? List.copyOf(lineComments) : List.of();
            keyStrengths = (keyStrengths != null) ? List.copyOf(keyStrengths) : List.of();
            requiredChanges = (requiredChanges != null) ? List.copyOf(requiredChanges) : List.of();
            timestamp = (timestamp != null) ? timestamp : Instant.now();
        }
    }

    public record ApiGenerationRequest(
            String entityName,
            SupportedLanguage language,
            SupportedFramework framework,
            List<String> fields,
            List<String> operations
    ) {
        public ApiGenerationRequest {
            entityName = (entityName != null && !entityName.isBlank()) ? entityName : "Resource";
            language = (language != null) ? language : SupportedLanguage.JAVA;
            framework = (framework != null) ? framework : SupportedFramework.SPRING_BOOT;
            fields = (fields != null) ? List.copyOf(fields) : List.of("id:UUID", "name:String", "active:boolean");
            operations = (operations != null) ? List.copyOf(operations) : List.of("CREATE", "GET_BY_ID", "LIST", "UPDATE", "DELETE");
        }
    }

    public record ApiGenerationResult(
            String entityName,
            SupportedLanguage language,
            SupportedFramework framework,
            String controllerCode,
            String domainModelCode,
            String openApiSpecYaml,
            String unitTestCode,
            Instant timestamp
    ) {
        public ApiGenerationResult {
            Objects.requireNonNull(entityName, "entityName must not be null");
            Objects.requireNonNull(controllerCode, "controllerCode must not be null");
            Objects.requireNonNull(domainModelCode, "domainModelCode must not be null");
            timestamp = (timestamp != null) ? timestamp : Instant.now();
        }
    }

    public record RefactoringRequest(
            String sourceCode,
            String targetSmellOrGoal,
            SupportedLanguage language
    ) {
        public RefactoringRequest {
            sourceCode = (sourceCode != null) ? sourceCode : "";
            targetSmellOrGoal = (targetSmellOrGoal != null) ? targetSmellOrGoal : "CLEAN_ARCHITECTURE";
            language = (language != null) ? language : SupportedLanguage.JAVA;
        }
    }

    public record RefactoringResult(
            String refactoredCode,
            String patchDiff,
            List<String> improvementsApplied,
            int cyclomaticComplexityBefore,
            int cyclomaticComplexityAfter,
            boolean backwardsCompatible,
            Instant timestamp
    ) {
        public RefactoringResult {
            Objects.requireNonNull(refactoredCode, "refactoredCode must not be null");
            improvementsApplied = (improvementsApplied != null) ? List.copyOf(improvementsApplied) : List.of();
            timestamp = (timestamp != null) ? timestamp : Instant.now();
        }
    }

    public record BugFixRequest(
            String failingCode,
            String errorOrStackTrace,
            String expectedBehavior
    ) {
        public BugFixRequest {
            failingCode = (failingCode != null) ? failingCode : "";
            errorOrStackTrace = (errorOrStackTrace != null) ? errorOrStackTrace : "";
            expectedBehavior = (expectedBehavior != null) ? expectedBehavior : "";
        }
    }

    public record BugFixResult(
            String fixedCode,
            String rootCauseAnalysis,
            String patchDiff,
            String regressionTestCode,
            boolean verified,
            Instant timestamp
    ) {
        public BugFixResult {
            Objects.requireNonNull(fixedCode, "fixedCode must not be null");
            Objects.requireNonNull(rootCauseAnalysis, "rootCauseAnalysis must not be null");
            timestamp = (timestamp != null) ? timestamp : Instant.now();
        }
    }

    public record InfrastructureAutomationRequest(
            String applicationName,
            SupportedFramework framework,
            int port,
            boolean includeK8s,
            boolean includeCiCd
    ) {
        public InfrastructureAutomationRequest {
            applicationName = (applicationName != null && !applicationName.isBlank()) ? applicationName : "oenexa-service";
            framework = (framework != null) ? framework : SupportedFramework.SPRING_BOOT;
            if (port <= 0) port = 8080;
        }
    }

    public record InfrastructureAutomationResult(
            String applicationName,
            String dockerfile,
            String kubernetesManifestYaml,
            String githubActionsWorkflowYaml,
            String dockerComposeYaml,
            Instant timestamp
    ) {
        public InfrastructureAutomationResult {
            Objects.requireNonNull(applicationName, "applicationName must not be null");
            Objects.requireNonNull(dockerfile, "dockerfile must not be null");
            timestamp = (timestamp != null) ? timestamp : Instant.now();
        }
    }
}
