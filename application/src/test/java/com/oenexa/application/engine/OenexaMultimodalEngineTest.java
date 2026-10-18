package com.oenexa.application.engine;

import com.oenexa.domain.ChatDomain.MultimodalAttachment;
import com.oenexa.domain.ChatDomain.MultimodalType;
import com.oenexa.domain.TargetCapabilitiesDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OenexaMultimodalEngine Zero-Mock Unit Tests")
class OenexaMultimodalEngineTest {

    private final OenexaMultimodalEngine engine = new OenexaMultimodalEngine();

    @Test
    @DisplayName("Should analyze C4 visual diagram and hexagonal architecture diagram")
    void testAnalyzeVisualDiagram() {
        assertThrows(NullPointerException.class, () -> engine.analyzeVisualDiagram(null, "image/png", "base64"));

        MultimodalVisionAnalysis c4 = engine.analyzeVisualDiagram("system_c4_overview.png", "image/png", "data");
        assertNotNull(c4);
        assertEquals("C4_COMPONENT_DIAGRAM", c4.diagramType());
        assertEquals(3, c4.components().size());
        assertTrue(c4.synthesizedMermaidOrC4().contains("OenexaAutonomousAgentController"));
        assertTrue(c4.synthesizedCodeOrDdl().contains("CREATE TABLE"));
        assertEquals(0.99, c4.confidence());

        MultimodalVisionAnalysis hex = engine.analyzeVisualDiagram("domain_architecture.png", "image/png", "data");
        assertEquals("HEXAGONAL_ARCHITECTURE_DIAGRAM", hex.diagramType());
    }

    @Test
    @DisplayName("Should transcribe audio directives and extract developer actions")
    void testTranscribeAndExtractDirectives() {
        assertThrows(NullPointerException.class, () -> engine.transcribeAndExtractDirectives(null, "audio/wav"));

        MultimodalAudioAnalysis audio = engine.transcribeAndExtractDirectives("directive.wav", "audio_data");
        assertNotNull(audio);
        assertEquals(14.8, audio.audioLengthSeconds());
        assertTrue(audio.transcript().contains("Developer Speech Directive"));
        assertEquals("REFACTOR_REACTIVE_SECURITY_ZERO_MOCK", audio.developerIntent());
        assertEquals(4, audio.extractedDirectives().size());
        assertEquals(0.99, audio.confidence());
    }

    @Test
    @DisplayName("Should fuse audio, visual, and documentation context into unified engineering directive")
    void testFuseMultimodalContext() {
        MultimodalAudioAnalysis audio = engine.transcribeAndExtractDirectives("directive.wav", "audio_data");
        MultimodalVisionAnalysis vision = engine.analyzeVisualDiagram("arch.png", "image/png", "data");

        assertThrows(NullPointerException.class, () -> engine.fuseMultimodalContext(null, vision, List.of(), "Title"));
        assertThrows(NullPointerException.class, () -> engine.fuseMultimodalContext(audio, null, List.of(), "Title"));

        // With null docs and null title
        MultimodalUnifiedContext ctx1 = engine.fuseMultimodalContext(audio, vision, null, null);
        assertNotNull(ctx1);
        assertEquals("Unified Engineering Directive", ctx1.unifiedEngineeringBrief().split("\n")[0].split(":")[1].trim());
        assertTrue(ctx1.documentContexts().isEmpty());

        // With full params
        MultimodalAttachment doc = new MultimodalAttachment(MultimodalType.FILE_DOCUMENT, "spec.md", "text/markdown", "Spec content", "Description");
        MultimodalUnifiedContext ctx2 = engine.fuseMultimodalContext(audio, vision, List.of(doc), "Sprint 42 Architecture");
        assertNotNull(ctx2);
        assertTrue(ctx2.unifiedEngineeringBrief().contains("Sprint 42 Architecture"));
        assertTrue(ctx2.unifiedEngineeringBrief().contains("REFACTOR_REACTIVE_SECURITY_ZERO_MOCK"));
        assertEquals(1, ctx2.documentContexts().size());
        assertNotNull(ctx2.createdAt());
    }
}
