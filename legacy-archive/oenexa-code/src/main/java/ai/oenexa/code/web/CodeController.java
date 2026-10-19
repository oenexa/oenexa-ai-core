package ai.oenexa.code.web;

import ai.oenexa.code.domain.AstAnalysisResult;
import ai.oenexa.code.domain.CodeExplanation;
import ai.oenexa.code.domain.CodeGenerationRequest;
import ai.oenexa.code.domain.CodeGenerationResult;
import ai.oenexa.code.domain.RefactorRequest;
import ai.oenexa.code.domain.RefactorResult;
import ai.oenexa.code.service.CodeService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoint exposing code generation, refactoring, explanation, and AST analysis.
 */
@RestController
@RequestMapping("/api/v1/code")
public class CodeController {

    private final CodeService codeService;

    public CodeController(CodeService codeService) {
        this.codeService = codeService;
    }

    @PostMapping("/generate")
    public ResponseEntity<CodeGenerationResult> generate(@RequestBody CodeGenerationRequest request) {
        return ResponseEntity.ok(codeService.generateCode(request));
    }

    @PostMapping("/refactor")
    public ResponseEntity<RefactorResult> refactor(@RequestBody RefactorRequest request) {
        return ResponseEntity.ok(codeService.refactorCode(request));
    }

    @PostMapping("/explain")
    public ResponseEntity<CodeExplanation> explain(@RequestBody Map<String, String> payload) {
        String code = payload.getOrDefault("code", "");
        String language = payload.getOrDefault("language", "java");
        return ResponseEntity.ok(codeService.explainCode(code, language));
    }

    @PostMapping("/analyze")
    public ResponseEntity<AstAnalysisResult> analyze(@RequestBody Map<String, String> payload) {
        String code = payload.getOrDefault("code", "");
        String language = payload.getOrDefault("language", "java");
        return ResponseEntity.ok(codeService.analyzeAst(code, language));
    }
}
