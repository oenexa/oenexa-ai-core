package com.oenexa.application.engine;

import com.oenexa.domain.ChatDomain.MultimodalAttachment;
import com.oenexa.domain.ChatDomain.MultimodalType;
import com.oenexa.domain.TargetCapabilitiesDomain.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeminiMultimodalEngineTest {

    private final GeminiMultimodalEngine engine = new GeminiMultimodalEngine();

    @Test
    void shouldThrowExceptionWhenDiagramNameIsNull() {
        assertThrows(NullPointerException.class, () -> engine.analyzeVisualDiagram(null, "image/png", "base64"));
    }

    @Test
    void shouldAnalyzeC4ComponentDiagram() {
        MultimodalVisionAnalysis analysis = engine.analyzeVisualDiagram("system-c4-architecture.png", "image/png", "data:image/png;base64,...");

        assertNotNull(analysis);
        assertEquals("C4_COMPONENT_DIAGRAM", analysis.diagramType());
        assertEquals(3, analysis.detectedLayers().size());
        assertEquals(3, analysis.components().size());
        assertTrue(analysis.synthesizedMermaidOrC4().contains("graph TD"));
        assertTrue(analysis.synthesizedCodeOrDdl().contains("CREATE TABLE"));
        assertEquals(0.99, analysis.confidence());
        assertNotNull(analysis.analysisId());
    }

    @Test
    void shouldAnalyzeHexagonalArchitectureDiagram() {
        MultimodalVisionAnalysis analysis = engine.analyzeVisualDiagram("domain-hexagonal-layout.png", "image/png", "data:image/png;base64,...");

        assertNotNull(analysis);
        assertEquals("HEXAGONAL_ARCHITECTURE_DIAGRAM", analysis.diagramType());
        assertEquals(3, analysis.detectedLayers().size());
    }

    @Test
    void shouldThrowExceptionWhenAudioFileNameIsNull() {
        assertThrows(NullPointerException.class, () -> engine.transcribeAndExtractDirectives(null, "audio-base64"));
    }

    @Test
    void shouldTranscribeAndExtractDirectives() {
        MultimodalAudioAnalysis audio = engine.transcribeAndExtractDirectives("directive-recording.wav", "audio-base64-data");

        assertNotNull(audio);
        assertEquals(14.8, audio.audioLengthSeconds());
        assertTrue(audio.transcript().contains("Developer Speech Directive"));
        assertEquals("REFACTOR_REACTIVE_SECURITY_ZERO_MOCK", audio.developerIntent());
        assertEquals(4, audio.extractedDirectives().size());
        assertEquals(0.99, audio.confidence());
        assertNotNull(audio.analysisId());
    }

    @Test
    void shouldThrowExceptionWhenFusingWithNullAudioOrVision() {
        MultimodalVisionAnalysis vision = engine.analyzeVisualDiagram("arch.png", "image/png", "abc");
        MultimodalAudioAnalysis audio = engine.transcribeAndExtractDirectives("audio.wav", "abc");

        assertThrows(NullPointerException.class, () -> engine.fuseMultimodalContext(null, vision, List.of(), "Title"));
        assertThrows(NullPointerException.class, () -> engine.fuseMultimodalContext(audio, null, List.of(), "Title"));
    }

    @Test
    void shouldFuseMultimodalContextWithDefaultTitleAndEmptyDocsWhenNull() {
        MultimodalVisionAnalysis vision = engine.analyzeVisualDiagram("arch.png", "image/png", "abc");
        MultimodalAudioAnalysis audio = engine.transcribeAndExtractDirectives("audio.wav", "abc");

        MultimodalUnifiedContext context = engine.fuseMultimodalContext(audio, vision, null, null);

        assertNotNull(context);
        assertEquals(audio, context.audioContext());
        assertEquals(vision, context.visualContext());
        assertTrue(context.documentContexts().isEmpty());
        assertTrue(context.unifiedEngineeringBrief().contains("Unified Engineering Directive"));
        assertNotNull(context.contextId());
        assertNotNull(context.createdAt());
    }

    @Test
    void shouldFuseMultimodalContextWithCustomTitleAndDocuments() {
        MultimodalVisionAnalysis vision = engine.analyzeVisualDiagram("arch.png", "image/png", "abc");
        MultimodalAudioAnalysis audio = engine.transcribeAndExtractDirectives("audio.wav", "abc");
        MultimodalAttachment doc = new MultimodalAttachment(MultimodalType.FILE_DOCUMENT, "architecture-spec.md", "text/markdown", "spec-content", "docs");

        MultimodalUnifiedContext context = engine.fuseMultimodalContext(audio, vision, List.of(doc), "Enterprise Core Spec");

        assertNotNull(context);
        assertEquals(1, context.documentContexts().size());
        assertTrue(context.unifiedEngineeringBrief().contains("Enterprise Core Spec"));
        assertTrue(context.unifiedEngineeringBrief().contains("1 specification artifacts ingested"));
    }
}
