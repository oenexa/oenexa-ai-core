package ai.oenexa.devops.service;

import ai.oenexa.devops.domain.ManifestAnalysisRequest;
import ai.oenexa.devops.domain.ManifestAnalysisResult;
import ai.oenexa.devops.domain.PipelineGenerationRequest;
import ai.oenexa.devops.domain.PipelineGenerationResult;

/**
 * Service port for Docker, Kubernetes, Helm manifest diagnosis and CI/CD generation.
 */
public interface DevopsService {

    ManifestAnalysisResult analyzeManifest(ManifestAnalysisRequest request);

    PipelineGenerationResult generatePipeline(PipelineGenerationRequest request);
}
