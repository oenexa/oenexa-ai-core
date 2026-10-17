package com.oenexa.application.engine;

import com.oenexa.domain.TargetCapabilitiesDomain.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CursorCodingEngineTest {

    private final CursorCodingEngine engine = new CursorCodingEngine();

    @Test
    void shouldThrowExceptionWhenCuratingContextWithNullQuery() {
        assertThrows(NullPointerException.class, () -> engine.curateCursorContext(null, "repo-1"));
    }

    @Test
    void shouldCurateCursorContextWithDefaultReferences() {
        List<CursorContextReference> refs = engine.curateCursorContext("Find auth symbols", "repo-123");

        assertNotNull(refs);
        assertEquals(4, refs.size());
        assertEquals(CursorContextType.FILE, refs.get(0).type());
        assertEquals(CursorContextType.SYMBOL, refs.get(1).type());
        assertEquals(CursorContextType.GIT_DIFF, refs.get(2).type());
        assertEquals(CursorContextType.CODEBASE_INDEX, refs.get(3).type());
    }

    @Test
    void shouldThrowExceptionWhenGeneratingPatchWithNullObjective() {
        assertThrows(NullPointerException.class, () -> engine.generateCursorAtomicPatch(null, null));
    }

    @Test
    void shouldGenerateDefaultAtomicPatchWhenFilePathsNullOrEmpty() {
        AtomicMultiFilePatch patchNull = engine.generateCursorAtomicPatch("Refactor Security Layer", null);
        assertNotNull(patchNull);
        assertEquals(2, patchNull.fileDiffs().size());
        assertTrue(patchNull.rollbackSafe());
        assertEquals(2, patchNull.affectedSymbols().size());
        assertTrue(patchNull.fileDiffs().get(0).filePath().contains("AuthFilter.java"));
        assertTrue(patchNull.fileDiffs().get(1).filePath().contains("AuthFilterTest.java"));

        AtomicMultiFilePatch patchEmpty = engine.generateCursorAtomicPatch("Refactor Security Layer", List.of());
        assertNotNull(patchEmpty);
        assertEquals(2, patchEmpty.fileDiffs().size());
    }

    @Test
    void shouldGenerateCustomAtomicPatchForTestAndSourceFiles() {
        List<String> files = List.of(
                "src/main/java/com/oenexa/application/CustomService.java",
                "src/test/java/com/oenexa/application/CustomServiceTest.java"
        );

        AtomicMultiFilePatch patch = engine.generateCursorAtomicPatch("Add Invariant Validation", files);

        assertNotNull(patch);
        assertEquals(2, patch.fileDiffs().size());
        assertEquals("src/main/java/com/oenexa/application/CustomService.java", patch.fileDiffs().get(0).filePath());
        assertTrue(patch.fileDiffs().get(0).patchContent().contains("Objects.requireNonNull"));
        assertEquals("src/test/java/com/oenexa/application/CustomServiceTest.java", patch.fileDiffs().get(1).filePath());
        assertTrue(patch.fileDiffs().get(1).patchContent().contains("shouldAuthenticateZeroMock"));
        assertEquals(2, patch.affectedSymbols().size());
    }
}
