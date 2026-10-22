package com.oenexa.application.engine;

import com.oenexa.domain.AiEngineerDomain.BugFixRequest;
import com.oenexa.domain.AiEngineerDomain.BugFixResult;
import com.oenexa.domain.AiEngineerDomain.RefactoringRequest;
import com.oenexa.domain.AiEngineerDomain.RefactoringResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RefactoringEngine Industrial Bug-Fixing & Self-Healing Tests")
class RefactoringEngineTest {

    private final RefactoringEngine engine = new RefactoringEngine();

    @Test
    @DisplayName("Should autonomously diagnose and fix NullPointerException (CWE-476)")
    void shouldFixNullPointerException() {
        BugFixRequest request = new BugFixRequest(
                "public String process(Map<String, String> map) { return map.get(key).toUpperCase(); }",
                "java.lang.NullPointerException: Cannot invoke toUpperCase() on null",
                "Handle null references safely"
        );

        BugFixResult result = engine.fixBug(request);

        assertNotNull(result);
        assertTrue(result.rootCauseAnalysis().contains("CWE-476"));
        assertTrue(result.patchDiff().contains("Optional.ofNullable"));
        assertTrue(result.fixedCode().contains("SafeProcessor"));
        assertTrue(result.regressionTestCode().contains("@Test"));
        assertTrue(result.verified());
    }

    @Test
    @DisplayName("Should autonomously diagnose and fix IndexOutOfBoundsException (CWE-125)")
    void shouldFixIndexOutOfBoundsException() {
        BugFixRequest request = new BugFixRequest(
                "public String getItem(List<String> list, int index) { return list.get(index); }",
                "java.lang.IndexOutOfBoundsException: Index 5 out of bounds for length 2",
                "Safely access elements with bounds check"
        );

        BugFixResult result = engine.fixBug(request);

        assertNotNull(result);
        assertTrue(result.rootCauseAnalysis().contains("CWE-125"));
        assertTrue(result.fixedCode().contains("SafeIndexAccessor"));
        assertTrue(result.regressionTestCode().contains("shouldHandleBoundsSafely"));
    }

    @Test
    @DisplayName("Should autonomously diagnose and fix ConcurrentModificationException (CWE-362)")
    void shouldFixConcurrentModificationException() {
        BugFixRequest request = new BugFixRequest(
                "for (Item item : items) { items.remove(item); }",
                "java.util.ConcurrentModificationException at java.base/java.util.ArrayList",
                "Remove elements safely during traversal"
        );

        BugFixResult result = engine.fixBug(request);

        assertNotNull(result);
        assertTrue(result.rootCauseAnalysis().contains("CWE-362"));
        assertTrue(result.fixedCode().contains("SafeCollectionModifier"));
        assertTrue(result.regressionTestCode().contains("shouldAvoidConcurrentModification"));
    }

    @Test
    @DisplayName("Should autonomously diagnose and fix ArithmeticException (CWE-369)")
    void shouldFixArithmeticException() {
        BugFixRequest request = new BugFixRequest(
                "long ratio = num / den;",
                "java.lang.ArithmeticException: / by zero",
                "Prevent divide by zero faults"
        );

        BugFixResult result = engine.fixBug(request);

        assertNotNull(result);
        assertTrue(result.rootCauseAnalysis().contains("CWE-369"));
        assertTrue(result.fixedCode().contains("SafeArithmetic"));
        assertTrue(result.regressionTestCode().contains("safeDivide"));
    }

    @Test
    @DisplayName("Should autonomously diagnose and fix ClassCastException (CWE-704)")
    void shouldFixClassCastException() {
        BugFixRequest request = new BugFixRequest(
                "String s = (String) Cast(obj);",
                "java.lang.ClassCastException: class java.lang.Integer cannot be cast to class java.lang.String",
                "Safely match object types"
        );

        BugFixResult result = engine.fixBug(request);

        assertNotNull(result);
        assertTrue(result.rootCauseAnalysis().contains("CWE-704"));
        assertTrue(result.fixedCode().contains("SafePatternMatcher"));
    }

    @Test
    @DisplayName("Should handle generic boundary invariant violation (CWE-20)")
    void shouldHandleGenericBoundaryViolation() {
        BugFixRequest request = new BugFixRequest(
                "public void register(String name) {}",
                "Invalid input argument",
                "Validate input"
        );

        BugFixResult result = engine.fixBug(request);

        assertNotNull(result);
        assertTrue(result.rootCauseAnalysis().contains("CWE-20"));
        assertTrue(result.patchDiff().contains("Objects.requireNonNull"));
    }

    @Test
    @DisplayName("Should refactor mutable JavaBean to immutable record")
    void shouldRefactorJavaBeanToRecord() {
        RefactoringRequest request = new RefactoringRequest(
                "public class UserDto { private String name; public String getName() { return name; } public void setName(String name) { this.name = name; } }",
                "IMMUTABLE_RECORD",
                com.oenexa.domain.AiEngineerDomain.SupportedLanguage.JAVA
        );

        RefactoringResult result = engine.refactorCode(request);

        assertNotNull(result);
        assertTrue(result.refactoredCode().contains("public record RefactoredEntity"));
        assertTrue(result.improvementsApplied().stream().anyMatch(s -> s.contains("immutable Java 25 record")));
        assertTrue(result.cyclomaticComplexityAfter() < result.cyclomaticComplexityBefore());
    }

    @Test
    @DisplayName("Should handle null and empty requests gracefully")
    void shouldHandleNullRequests() {
        BugFixResult bugRes = engine.fixBug(null);
        assertNotNull(bugRes);
        assertTrue(bugRes.verified());

        RefactoringResult refactorRes = engine.refactorCode(null);
        assertNotNull(refactorRes);
        assertTrue(refactorRes.backwardsCompatible());
    }
}
