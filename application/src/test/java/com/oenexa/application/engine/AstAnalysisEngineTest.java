package com.oenexa.application.engine;

import com.oenexa.domain.model.code.CodeEngineeringDomain.CodeAnalysisRequest;
import com.oenexa.domain.model.code.CodeEngineeringDomain.CodeAnalysisResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AstAnalysisEngine Zero-Mock Unit Tests")
class AstAnalysisEngineTest {

    private AstAnalysisEngine engine;

    @BeforeEach
    void setUp() {
        engine = new AstAnalysisEngine();
    }

    @Test
    @DisplayName("Should reject null request with NullPointerException")
    void shouldRejectNullRequest() {
        assertThrows(NullPointerException.class, () -> engine.analyze(null));
    }

    @Test
    @DisplayName("Should analyze empty code snippet gracefully")
    void shouldAnalyzeEmptyCode() {
        CodeAnalysisRequest request = new CodeAnalysisRequest("", "java");
        CodeAnalysisResponse response = engine.analyze(request);

        assertNotNull(response);
        assertEquals("java", response.language());
        assertEquals(0, response.totalLines());
        assertTrue(response.classes().isEmpty());
        assertTrue(response.methods().isEmpty());
        assertTrue(response.imports().isEmpty());
        assertEquals(1, response.cyclomaticComplexity());
        assertTrue(response.clean());
    }

    @Test
    @DisplayName("Should extract imports, classes, and methods from standard Java class")
    void shouldExtractImportsClassesAndMethods() {
        String code = """
                package com.example;

                import java.util.List;
                import java.util.Objects;

                public class UserService {
                    public List<String> listUsers() {
                        return List.of("alice", "bob");
                    }

                    public void saveUser(String user) {
                        Objects.requireNonNull(user);
                    }
                }
                """;

        CodeAnalysisRequest request = new CodeAnalysisRequest(code, "java");
        CodeAnalysisResponse response = engine.analyze(request);

        assertNotNull(response);
        assertEquals(2, response.imports().size());
        assertTrue(response.imports().contains("java.util.List"));
        assertTrue(response.imports().contains("java.util.Objects"));

        assertEquals(1, response.classes().size());
        assertEquals("UserService", response.classes().get(0));

        assertEquals(2, response.methods().size());
        assertTrue(response.methods().contains("listUsers"));
        assertTrue(response.methods().contains("saveUser"));

        assertTrue(response.clean());
    }

    @Test
    @DisplayName("Should detect default fallback class and methods when none explicitly defined")
    void shouldFallbackWhenNoClassOrMethodFound() {
        String code = "System.out.println(\"Hello World\");";
        CodeAnalysisRequest request = new CodeAnalysisRequest(code, null);
        CodeAnalysisResponse response = engine.analyze(request);

        assertNotNull(response);
        assertEquals("java", response.language());
        assertEquals(1, response.classes().size());
        assertEquals("Solution", response.classes().get(0));
        assertTrue(response.methods().contains("main"));
        assertTrue(response.methods().contains("listActiveUsers"));
        assertFalse(response.clean());
        assertTrue(response.codeSmells().stream().anyMatch(s -> s.contains("Direct stdout/stderr logging")));
    }

    @Test
    @DisplayName("Should compute cyclomatic complexity and identify high complexity smell")
    void shouldDetectHighCyclomaticComplexityAndSmells() {
        StringBuilder sb = new StringBuilder();
        sb.append("public class ComplexClass {\n");
        sb.append("    public void complexMethod(int a, int b) {\n");
        for (int i = 0; i < 12; i++) {
            sb.append("        if (a > ").append(i).append(" && b < ").append(i).append(") { /* branch */ }\n");
        }
        sb.append("        try {\n");
        sb.append("            Thread.sleep(100);\n");
        sb.append("        } catch (Exception e) {\n");
        sb.append("            System.err.println(e.getMessage());\n");
        sb.append("        }\n");
        sb.append("    }\n");
        sb.append("}\n");

        CodeAnalysisRequest request = new CodeAnalysisRequest(sb.toString(), "java");
        CodeAnalysisResponse response = engine.analyze(request);

        assertNotNull(response);
        assertTrue(response.cyclomaticComplexity() > 10);
        assertFalse(response.clean());

        assertTrue(response.codeSmells().stream().anyMatch(s -> s.contains("High Cyclomatic Complexity")));
        assertTrue(response.codeSmells().stream().anyMatch(s -> s.contains("Overly broad exception catching")));
        assertTrue(response.codeSmells().stream().anyMatch(s -> s.contains("Thread.sleep in execution path")));
        assertTrue(response.codeSmells().stream().anyMatch(s -> s.contains("Direct stdout/stderr logging")));
    }

    @Test
    @DisplayName("Should detect large file size smell when code exceeds 300 lines")
    void shouldDetectLargeFileSizeSmell() {
        StringBuilder sb = new StringBuilder();
        sb.append("public class LargeClass {\n");
        for (int i = 0; i < 310; i++) {
            sb.append("    // line ").append(i).append("\n");
        }
        sb.append("}\n");

        CodeAnalysisRequest request = new CodeAnalysisRequest(sb.toString(), "java");
        CodeAnalysisResponse response = engine.analyze(request);

        assertNotNull(response);
        assertTrue(response.totalLines() > 300);
        assertTrue(response.codeSmells().stream().anyMatch(s -> s.contains("Large File Size")));
    }
}

