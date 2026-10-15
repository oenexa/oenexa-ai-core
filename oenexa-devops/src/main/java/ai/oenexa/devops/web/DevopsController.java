package ai.oenexa.devops.web;

import ai.oenexa.devops.domain.ManifestAnalysisRequest;
import ai.oenexa.devops.domain.ManifestAnalysisResult;
import ai.oenexa.devops.domain.PipelineGenerationRequest;
import ai.oenexa.devops.domain.PipelineGenerationResult;
import ai.oenexa.devops.service.DevopsService;
import java.util.Objects;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/devops")
public class DevopsController {

    private final DevopsService devopsService;

    public DevopsController(DevopsService devopsService) {
        this.devopsService = Objects.requireNonNull(devopsService, "devopsService must not be null");
    }

    @PostMapping("/analyze")
    public ResponseEntity<ManifestAnalysisResult> analyze(@RequestBody ManifestAnalysisRequest request) {
        return ResponseEntity.ok(devopsService.analyzeManifest(request));
    }

    @PostMapping("/pipeline")
    public ResponseEntity<PipelineGenerationResult> pipeline(@RequestBody PipelineGenerationRequest request) {
        return ResponseEntity.ok(devopsService.generatePipeline(request));
    }
}
