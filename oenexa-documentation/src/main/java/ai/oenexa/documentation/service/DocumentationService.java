package ai.oenexa.documentation.service;

import ai.oenexa.documentation.domain.DiagramRequest;
import ai.oenexa.documentation.domain.DiagramResult;
import ai.oenexa.documentation.domain.DocumentationRequest;
import ai.oenexa.documentation.domain.DocumentationResult;

/**
 * Service port for generating technical documentation, architecture specs, and Mermaid diagrams.
 */
public interface DocumentationService {

    DocumentationResult generateDocumentation(DocumentationRequest request);

    DiagramResult generateDiagram(DiagramRequest request);
}
