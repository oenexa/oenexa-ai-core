package ai.oenexa.pipeline.service;

import ai.oenexa.pipeline.domain.PipelineAnalysisRequest;
import ai.oenexa.pipeline.domain.PipelineAnalysisResult;

/**
 * Service port for diagnosing broken CI/CD pipelines and proposing auto-repairs.
 */
public interface PipelineService {

    PipelineAnalysisResult analyzePipeline(PipelineAnalysisRequest request);

    String repairConfiguration(String brokenConfig, String failureReason);
}
