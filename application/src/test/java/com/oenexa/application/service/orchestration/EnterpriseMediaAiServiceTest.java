package com.oenexa.application.service.orchestration;

import com.oenexa.application.port.*;
import com.oenexa.application.service.agent.*;
import com.oenexa.application.service.auth.*;
import com.oenexa.application.service.chat.*;
import com.oenexa.application.service.code.*;
import com.oenexa.application.service.knowledge.*;
import com.oenexa.application.service.memory.*;
import com.oenexa.application.service.orchestration.*;

import com.oenexa.domain.model.orchestration.EnterpriseMediaAiDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EnterpriseMediaAiService Zero-Mock Unit Tests")
class EnterpriseMediaAiServiceTest {

    private EnterpriseMediaAiService service;

    @BeforeEach
    void setUp() {
        service = new EnterpriseMediaAiService();
    }

    @Test
    @DisplayName("Should generate image with SVG data URI and detected objects")
    void shouldGenerateImage() {
        ImageProcessingResult res = service.generateImage(
                "Kubernetes Cluster Architecture",
                ImageResolution.RES_1024x1024,
                "BLUEPRINT"
        );

        assertNotNull(res);
        assertTrue(res.imageUri().startsWith("data:image/svg+xml;base64,"));
        assertEquals("Kubernetes Cluster Architecture", res.prompt());
        assertFalse(res.detectedObjects().isEmpty());
        assertTrue(res.processingLatencyMs() > 0);
    }

    @Test
    @DisplayName("Should perform image edit, background removal, and OCR extraction")
    void shouldPerformImageTransformationsAndOcr() {
        ImageProcessingResult edit = service.editImage("samplebase64", "Switch to dark theme");
        assertNotNull(edit);
        assertTrue(edit.description().contains("Switch to dark theme"));

        ImageProcessingResult noBg = service.removeBackground("samplebase64");
        assertNotNull(noBg);
        assertTrue(noBg.description().contains("transparent alpha"));

        ImageProcessingResult ocr = service.extractOcr("samplebase64");
        assertNotNull(ocr);
        assertTrue(ocr.extractedOcrText().contains("HTTP/2"));

        ImageProcessingResult analysis = service.analyzeImage("samplebase64");
        assertNotNull(analysis);
        assertEquals(4, analysis.detectedObjects().size());
    }

    @Test
    @DisplayName("Should summarize, generate video, detect scenes, and generate subtitles")
    void shouldProcessVideoWorkloads() {
        VideoProcessingResult summary = service.summarizeVideo(new byte[100], "Cloud Microservices Walkthrough");
        assertNotNull(summary);
        assertEquals(3, summary.scenes().size());
        assertEquals(3, summary.subtitles().size());
        assertTrue(summary.summary().contains("Cloud Microservices Walkthrough"));

        VideoProcessingResult gen = service.generateVideo("Deploy Sovereign Oenexa", 30.0);
        assertNotNull(gen);
        assertEquals(30.0, gen.durationSec());
        assertEquals(3, gen.scenes().size());

        List<SubtitleCue> cues = service.generateSubtitles(new byte[50]);
        assertEquals(3, cues.size());

        List<VideoScene> scenes = service.detectScenes(new byte[50]);
        assertEquals(3, scenes.size());
    }
}


