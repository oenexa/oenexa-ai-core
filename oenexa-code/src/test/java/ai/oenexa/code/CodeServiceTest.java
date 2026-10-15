package ai.oenexa.code;

import ai.oenexa.code.domain.AstAnalysisResult;
import ai.oenexa.code.domain.AstNode;
import ai.oenexa.code.domain.CodeExplanation;
import ai.oenexa.code.domain.CodeGenerationRequest;
import ai.oenexa.code.domain.CodeGenerationResult;
import ai.oenexa.code.domain.CodeSnippet;
import ai.oenexa.code.domain.RefactorRequest;
import ai.oenexa.code.domain.RefactorResult;
import ai.oenexa.code.service.DefaultCodeService;
import ai.oenexa.code.web.CodeController;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OENEXA Code Service & Controller Tests (Zero-Mock Policy)")
class CodeServiceTest {

    private DefaultCodeService codeService;
    private CodeController codeController;

    @BeforeEach
    void setUp() {
        codeService = new DefaultCodeService();
        codeController = new CodeController(codeService);
    }

    @Test
    @DisplayName("CodeSnippet line count and character count invariants")
    void testCodeSnippetInvariants() {
        CodeSnippet snippet = new CodeSnippet("snip-1", "App.java", "java", "public class App {\n    int x = 1;\n}", Instant.now());
        assertEquals("snip-1", snippet.id());
        assertEquals("App.java", snippet.filename());
        assertEquals(3, snippet.lineCount());
        assertTrue(snippet.characterCount() > 0);

        CodeSnippet empty = new CodeSnippet("snip-2", "Empty.java", "java", "", null);
        assertEquals(0, empty.lineCount());
        assertNotNull(empty.createdAt());
    }

    @Test
    @DisplayName("Generate code for domain record specification")
    void testGenerateRecord() {
        CodeGenerationRequest req = new CodeGenerationRequest("Create a DTO record for User", "java", "", Map.of());
        CodeGenerationResult result = codeService.generateCode(req);

        assertNotNull(result.generatedCode());
        assertTrue(result.generatedCode().contains("public record GeneratedRecord"));
        assertTrue(result.suggestedTests().size() >= 2);
        assertTrue(result.tokenEstimate() > 0);
    }

    @Test
    @DisplayName("Generate code for Spring Boot REST Controller")
    void testGenerateController() {
        CodeGenerationRequest req = new CodeGenerationRequest("Create a REST controller for API resources", "java", null, null);
        CodeGenerationResult result = codeService.generateCode(req);

        assertTrue(result.generatedCode().contains("@RestController"));
        assertTrue(result.generatedCode().contains("@RequestMapping"));
        assertEquals("java", result.language());
    }

    @Test
    @DisplayName("Generate code for TypeScript and Python languages")
    void testGenerateTypeScriptAndPython() {
        CodeGenerationRequest tsReq = new CodeGenerationRequest("Create a data model", "typescript", "", Map.of());
        CodeGenerationResult tsResult = codeService.generateCode(tsReq);
        assertTrue(tsResult.generatedCode().contains("export interface ResourceModel"));

        CodeGenerationRequest pyReq = new CodeGenerationRequest("Create a data model", "python", "", Map.of());
        CodeGenerationResult pyResult = codeService.generateCode(pyReq);
        assertTrue(pyResult.generatedCode().contains("@dataclass(frozen=True)"));

        CodeGenerationRequest defReq = new CodeGenerationRequest("Create a worker service", "go", "", Map.of());
        CodeGenerationResult defResult = codeService.generateCode(defReq);
        assertTrue(defResult.generatedCode().contains("class GeneratedService"));
    }

    @Test
    @DisplayName("Refactor code to modern Java record")
    void testRefactorCode() {
        String legacyPojo = """
                public class UserProfile {
                    private String id;
                    private String name;
                    public String getId() { return id; }
                }
                """;
        RefactorRequest req = new RefactorRequest(legacyPojo, "java", "RECORDS");
        RefactorResult result = codeService.refactorCode(req);

        assertTrue(result.refactoredCode().contains("public record UserProfile"));
        assertTrue(result.complexityDelta() < 0);
        assertTrue(result.appliedTransformations().size() >= 2);
    }

    @Test
    @DisplayName("Explain code complexity and architectural patterns")
    void testExplainCode() {
        String complexCode = """
                @RestController
                public class OrderController {
                    public void process(List<Order> orders) {
                        for (Order o : orders) {
                            for (Item i : o.items()) {
                                processItem(i);
                            }
                        }
                    }
                }
                """;
        CodeExplanation explanation = codeService.explainCode(complexCode, "java");

        assertEquals("O(n^2)", explanation.timeComplexity());
        assertTrue(explanation.keyPatterns().contains("Spring Dependency Injection & Controller"));
        assertTrue(explanation.potentialPitfalls().stream().anyMatch(p -> p.contains("Nested loops")));
    }

    @Test
    @DisplayName("AST analysis detects classes, methods, imports, and cyclomatic complexity")
    void testAnalyzeAst() {
        String sampleSource = """
                package com.example;
                import java.util.List;
                import java.util.Objects;

                public class SampleService {
                    public void run(int a, int b) {
                        if (a > 0 && b > 0) {
                            System.out.println("positive");
                        } else if (a < 0) {
                            System.out.println("negative");
                        }
                    }
                }
                """;
        AstAnalysisResult result = codeService.analyzeAst(sampleSource, "java");

        assertEquals(1, result.classCount());
        assertEquals(1, result.methodCount());
        assertEquals(2, result.imports().size());
        assertTrue(result.cyclomaticComplexity() >= 4);
        assertTrue(result.detectedCodeSmells().stream().anyMatch(s -> s.contains("Direct stdout/stderr")));
        assertFalse(result.topLevelNodes().isEmpty());
    }

    @Test
    @DisplayName("CodeController endpoints return correct responses")
    void testControllerEndpoints() {
        CodeGenerationRequest genReq = new CodeGenerationRequest("Create record", "java", "", Map.of());
        ResponseEntity<CodeGenerationResult> genResp = codeController.generate(genReq);
        assertEquals(200, genResp.getStatusCode().value());
        assertNotNull(genResp.getBody());

        RefactorRequest refReq = new RefactorRequest("public class Test { }", "java", "CLEAN");
        ResponseEntity<RefactorResult> refResp = codeController.refactor(refReq);
        assertEquals(200, refResp.getStatusCode().value());
        assertNotNull(refResp.getBody());

        ResponseEntity<CodeExplanation> expResp = codeController.explain(Map.of("code", "int a = 1;", "language", "java"));
        assertEquals(200, expResp.getStatusCode().value());
        assertNotNull(expResp.getBody());

        ResponseEntity<AstAnalysisResult> astResp = codeController.analyze(Map.of("code", "class A {}", "language", "java"));
        assertEquals(200, astResp.getStatusCode().value());
        assertNotNull(astResp.getBody());
    }

    @Test
    @DisplayName("CodeModule metadata and initialization")
    void testCodeModuleMetadata() {
        CodeModule module = new CodeModule();
        assertEquals("code", module.id());
        assertEquals("OENEXA Code", module.displayName());
        assertTrue(module.capabilities().contains("refactoring"));
    }

    @Test
    @DisplayName("AstNode and AstAnalysisResult domain invariants and null safety")
    void testDomainRecordsEdgeCases() {
        AstNode leaf = new AstNode("variable", "count", 1, 1, null);
        assertFalse(leaf.hasChildren());
        assertEquals(0, leaf.children().size());

        AstNode parent = new AstNode("class", "Test", 1, 10, java.util.List.of(leaf));
        assertTrue(parent.hasChildren());
        assertEquals(1, parent.children().size());

        assertThrows(NullPointerException.class, () -> new AstNode(null, "x", 1, 1, null));
        assertThrows(NullPointerException.class, () -> new AstNode("type", null, 1, 1, null));

        AstAnalysisResult cleanResult = new AstAnalysisResult("java", 50, 1, 3, 5, null, null, null);
        assertTrue(cleanResult.isClean());
        assertTrue(cleanResult.imports().isEmpty());
        assertTrue(cleanResult.detectedCodeSmells().isEmpty());
        assertTrue(cleanResult.topLevelNodes().isEmpty());

        AstAnalysisResult complexResult = new AstAnalysisResult("java", 150, 2, 8, 14, null, java.util.List.of("Smell"), null);
        assertFalse(complexResult.isClean());

        CodeGenerationRequest genReq = new CodeGenerationRequest("Prompt", null, null, null);
        assertEquals("java", genReq.language());
        assertEquals("", genReq.context());
        assertNotNull(genReq.parameters());

        RefactorRequest refReq = new RefactorRequest("code", null, null);
        assertEquals("java", refReq.language());
        assertEquals("CLEAN_CODE", refReq.goal());

        RefactorResult refRes = new RefactorResult("orig", "refact", "goal", null, 0);
        assertNotNull(refRes.appliedTransformations());
    }

    @Test
    @DisplayName("DefaultCodeService edge cases: smells, broad exceptions, loops, and null fallbacks")
    void testServiceEdgeCases() {
        // Broad exception and Optional in explanation
        String codeWithPitfalls = """
                public class Service {
                    public java.util.Optional<String> find() {
                        try {
                            for (int i = 0; i < 10; i++) {}
                            return java.util.Optional.of("ok");
                        } catch (Exception e) {
                            return java.util.Optional.empty();
                        }
                    }
                }
                """;
        CodeExplanation explanation = codeService.explainCode(codeWithPitfalls, null);
        assertEquals("O(n)", explanation.timeComplexity());
        assertTrue(explanation.keyPatterns().contains("Monadic Null-Safety (Optional Pattern)"));
        assertTrue(explanation.potentialPitfalls().stream().anyMatch(p -> p.contains("Overly broad exception")));
        assertTrue(explanation.summary().contains("code snippet"));

        CodeExplanation recordExplanation = codeService.explainCode("public record User(String id) {}", "java");
        assertTrue(recordExplanation.keyPatterns().contains("Immutable Data Carrier (Java Record)"));

        CodeExplanation emptyExplanation = codeService.explainCode("", "java");
        assertEquals(0, codeService.analyzeAst("", "java").totalLines());
        RefactorRequest loopReq = new RefactorRequest("for (int i = 0; i < n; i++) {}", "java", "MODERNIZE");
        RefactorResult loopResult = codeService.refactorCode(loopReq);
        assertTrue(loopResult.appliedTransformations().stream().anyMatch(t -> t.contains("loop optimization")));

        // High cyclomatic complexity (> 15) and large file (> 300 lines) in AST
        StringBuilder largeSource = new StringBuilder();
        largeSource.append("public class LargeService {\n");
        for (int i = 0; i < 320; i++) {
            largeSource.append("    // line ").append(i).append("\n");
        }
        for (int i = 0; i < 18; i++) {
            largeSource.append("    void m").append(i).append("() { if (true) {} }\n");
        }
        largeSource.append("}\n");

        AstAnalysisResult astResult = codeService.analyzeAst(largeSource.toString(), null);
        assertEquals("java", astResult.language());
        assertTrue(astResult.cyclomaticComplexity() > 15);
        assertTrue(astResult.totalLines() > 300);
        assertTrue(astResult.detectedCodeSmells().stream().anyMatch(s -> s.contains("High Cyclomatic Complexity")));
        assertTrue(astResult.detectedCodeSmells().stream().anyMatch(s -> s.contains("Large File Size")));
    }
}
