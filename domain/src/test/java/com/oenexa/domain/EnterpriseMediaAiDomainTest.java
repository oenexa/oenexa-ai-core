package com.oenexa.domain;

import com.oenexa.domain.EnterpriseMediaAiDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EnterpriseMediaAiDomain Invariant Tests (Zero-Mock)")
class EnterpriseMediaAiDomainTest {

    @Test
    @DisplayName("Should create ImageProcessingResult with OCR and detected objects")
    void shouldCreateImageProcessingResult() {
        DetectedObject obj = new DetectedObject("ServerRack", 0.96, new BoundingBox(10, 20, 100, 200));
        ImageProcessingResult res = new ImageProcessingResult(
                "img-123",
                "data:image/png;base64,iVBORw...",
                "High-tech server room architecture",
                ImageResolution.RES_1024x1024,
                "Datacenter server cluster with 99.99% uptime",
                List.of(obj),
                "OENEXA CLUSTER NODE-01",
                450L
        );

        assertNotNull(res);
        assertEquals("img-123", res.mediaId());
        assertEquals(ImageResolution.RES_1024x1024, res.resolution());
        assertEquals("OENEXA CLUSTER NODE-01", res.extractedOcrText());
        assertEquals(1, res.detectedObjects().size());
        assertEquals(0.96, res.detectedObjects().get(0).confidence());
    }

    @Test
    @DisplayName("Should enforce bounding box invariants")
    void shouldEnforceBoundingBoxBounds() {
        assertThrows(IllegalArgumentException.class, () -> new BoundingBox(-1, 0, 10, 10));
        assertThrows(IllegalArgumentException.class, () -> new BoundingBox(0, -1, 10, 10));
        assertThrows(IllegalArgumentException.class, () -> new BoundingBox(0, 0, -5, 10));
    }

    @Test
    @DisplayName("Should create VideoProcessingResult with scenes and subtitles")
    void shouldCreateVideoProcessingResult() {
        VideoScene scene1 = new VideoScene(1, 0.0, 5.5, "Architecture Overview", "data:image/png;base64,frame1");
        SubtitleCue cue1 = new SubtitleCue(1, 0.0, 3.0, "Welcome to OENEXA autonomous engineering.");

        VideoProcessingResult video = new VideoProcessingResult(
                "vid-888",
                "https://cdn.oenexa.ai/video/demo.mp4",
                "OENEXA Platform Walkthrough",
                "Overview of 11 enterprise phases with live microservices.",
                List.of(scene1),
                List.of(cue1),
                60.0,
                1200L
        );

        assertEquals("vid-888", video.mediaId());
        assertEquals(60.0, video.durationSec());
        assertEquals(1, video.scenes().size());
        assertEquals(1, video.subtitles().size());
    }
}
