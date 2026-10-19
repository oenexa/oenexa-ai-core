package ai.oenexa.pipeline.web;

import ai.oenexa.pipeline.domain.PipelineAnalysisRequest;
import ai.oenexa.pipeline.domain.PipelineAnalysisResult;
import ai.oenexa.pipeline.service.PipelineService;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pipeline")
public class PipelineController {

    private final PipelineService pipelineService;

    public PipelineController(PipelineService pipelineService) {
        this.pipelineService = Objects.requireNonNull(pipelineService, "pipelineService must not be null");
    }

    @PostMapping("/analyze")
    public ResponseEntity<PipelineAnalysisResult> analyze(@RequestBody PipelineAnalysisRequest request) {
        return ResponseEntity.ok(pipelineService.analyzePipeline(request));
    }

    @PostMapping("/repair")
    public ResponseEntity<Map<String, String>> repair(@RequestBody Map<String, String> payload) {
        String config = payload.getOrDefault("config", "");
        String reason = payload.getOrDefault("reason", "GENERAL");
        String repaired = pipelineService.repairConfiguration(config, reason);
        return ResponseEntity.ok(Map.of("repairedConfig", repaired));
    }
}
