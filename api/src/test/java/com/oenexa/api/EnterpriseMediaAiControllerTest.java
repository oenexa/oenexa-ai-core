package com.oenexa.api;

import com.oenexa.api.controller.*;
import com.oenexa.api.exception.*;
import com.oenexa.application.EnterpriseMediaAiService;
import com.oenexa.domain.EnterpriseMediaAiDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EnterpriseMediaAiController Zero-Mock Unit Tests")
class EnterpriseMediaAiControllerTest {

    private EnterpriseMediaAiController controller;
    private EnterpriseMediaAiService service;

    @BeforeEach
    void setUp() {
        service = new EnterpriseMediaAiService();
        controller = new EnterpriseMediaAiController(service);
    }

    @Test
    @DisplayName("Should generate, edit, remove background, and extract OCR via controller")
    void shouldHandleImageEndpoints() {
        var genReq = new EnterpriseMediaAiController.GenerateImageRequest("System Architecture", ImageResolution.RES_1024x1024, "TECH_BLUEPRINT");
        ResponseEntity<ImageProcessingResult> genResp = controller.generateImage(genReq);
        assertEquals(HttpStatus.OK, genResp.getStatusCode());
        assertNotNull(genResp.getBody());
        assertTrue(genResp.getBody().imageUri().contains("data:image/svg+xml;base64,"));

        var editReq = new EnterpriseMediaAiController.EditImageRequest("base64data", "Add glow effect");
        ResponseEntity<ImageProcessingResult> editResp = controller.editImage(editReq);
        assertEquals(HttpStatus.OK, editResp.getStatusCode());
        assertNotNull(editResp.getBody());

        var imgReq = new EnterpriseMediaAiController.ImageRequest("base64data");
        ResponseEntity<ImageProcessingResult> noBgResp = controller.removeBackground(imgReq);
        assertEquals(HttpStatus.OK, noBgResp.getStatusCode());

        ResponseEntity<ImageProcessingResult> ocrResp = controller.extractOcr(imgReq);
        assertEquals(HttpStatus.OK, ocrResp.getStatusCode());
        assertTrue(ocrResp.getBody().extractedOcrText().contains("HTTP/2"));

        ResponseEntity<ImageProcessingResult> analyzeResp = controller.analyzeImage(imgReq);
        assertEquals(HttpStatus.OK, analyzeResp.getStatusCode());
        assertEquals(4, analyzeResp.getBody().detectedObjects().size());
    }

    @Test
    @DisplayName("Should summarize video, generate video, scenes, and subtitles via controller")
    void shouldHandleVideoEndpoints() {
        var vidReq = new EnterpriseMediaAiController.VideoRequest("base64videodata", "Oenexa System Demo");
        ResponseEntity<VideoProcessingResult> sumResp = controller.summarizeVideo(vidReq);
        assertEquals(HttpStatus.OK, sumResp.getStatusCode());
        assertNotNull(sumResp.getBody());
        assertEquals(3, sumResp.getBody().scenes().size());

        var genReq = new EnterpriseMediaAiController.GenerateVideoRequest("Kubernetes Deployment Pipeline", 20.0);
        ResponseEntity<VideoProcessingResult> genResp = controller.generateVideo(genReq);
        assertEquals(HttpStatus.OK, genResp.getStatusCode());
        assertNotNull(genResp.getBody());
        assertEquals(20.0, genResp.getBody().durationSec());

        ResponseEntity<List<SubtitleCue>> subResp = controller.generateSubtitles(vidReq);
        assertEquals(HttpStatus.OK, subResp.getStatusCode());
        assertEquals(3, subResp.getBody().size());

        ResponseEntity<List<VideoScene>> scenesResp = controller.detectScenes(vidReq);
        assertEquals(HttpStatus.OK, scenesResp.getStatusCode());
        assertEquals(3, scenesResp.getBody().size());
    }
}
