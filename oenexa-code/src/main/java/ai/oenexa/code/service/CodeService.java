package ai.oenexa.code.service;

import ai.oenexa.code.domain.AstAnalysisResult;
import ai.oenexa.code.domain.CodeExplanation;
import ai.oenexa.code.domain.CodeGenerationRequest;
import ai.oenexa.code.domain.CodeGenerationResult;
import ai.oenexa.code.domain.RefactorRequest;
import ai.oenexa.code.domain.RefactorResult;

/**
 * Service contract for code generation, AST analysis, refactoring, and explanation.
 */
public interface CodeService {

    CodeGenerationResult generateCode(CodeGenerationRequest request);

    RefactorResult refactorCode(RefactorRequest request);

    CodeExplanation explainCode(String sourceCode, String language);

    AstAnalysisResult analyzeAst(String sourceCode, String language);
}
