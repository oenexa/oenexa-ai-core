package com.oenexa.application.engine;

import com.oenexa.domain.model.orchestration.TargetCapabilitiesDomain.AtomicMultiFilePatch;
import com.oenexa.domain.model.orchestration.TargetCapabilitiesDomain.CodeSynthesisContextReference;
import com.oenexa.domain.model.orchestration.TargetCapabilitiesDomain.CodeSynthesisContextType;
import com.oenexa.domain.model.orchestration.TargetCapabilitiesDomain.CursorContextReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OenexaCodeSynthesisEngine Zero-Mock Unit Tests")
class OenexaCodeSynthesisEngineTest {

    private final OenexaCodeSynthesisEngine engine = new OenexaCodeSynthesisEngine();

    @Test
    @DisplayName("Should reject null query in context curation")
    void shouldRejectNullQuery() {
        assertThrows(NullPointerException.class, () -> engine.curateContext(null, "repo-1"));
        assertThrows(NullPointerException.class, () -> engine.curateCursorContext(null, "repo-1"));
    }

    @Test
    @DisplayName("Should curate multi-source code synthesis context")
    void shouldCurateContext() {
        List<CodeSynthesisContextReference> refs = engine.curateContext("find symbols", "oenexa-repo");
        assertEquals(4, refs.size());
        assertEquals(CodeSynthesisContextType.FILE, refs.get(0).type());
        assertEquals(CodeSynthesisContextType.SYMBOL, refs.get(1).type());
        assertEquals(CodeSynthesisContextType.GIT_DIFF, refs.get(2).type());
        assertEquals(CodeSynthesisContextType.CODEBASE_INDEX, refs.get(3).type());
    }

    @Test
    @DisplayName("Should provide backward-compatible cursor context references")
    void shouldProvideCursorContext() {
        List<CursorContextReference> refs = engine.curateCursorContext("query", "repo");
        assertEquals(4, refs.size());
        assertNotNull(refs.get(0).targetPathOrSymbol());
    }

    @Test
    @DisplayName("Should generate atomic multi-file patch with zero-mock tests")
    void shouldGenerateAtomicPatch() {
        assertThrows(NullPointerException.class, () -> engine.generateAtomicPatch(null, List.of()));

        AtomicMultiFilePatch patch = engine.generateAtomicPatch("Refactor Auth Invariants", List.of(
                "src/main/java/com/oenexa/AuthFilter.java",
                "src/test/java/com/oenexa/AuthFilterTest.java"
        ));

        assertNotNull(patch);
        assertTrue(patch.description().contains("OENEXA Atomic Patch"));
        assertEquals(2, patch.fileDiffs().size());
        assertEquals(2, patch.affectedSymbols().size());
        assertTrue(patch.rollbackSafe());
    }
}
