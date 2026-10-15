package ai.oenexa.documentation.web;

import ai.oenexa.documentation.domain.DiagramRequest;
import ai.oenexa.documentation.domain.DiagramResult;
import ai.oenexa.documentation.domain.DocumentationRequest;
import ai.oenexa.documentation.domain.DocumentationResult;
import ai.oenexa.documentation.service.DocumentationService;
import java.util.Objects;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/documentation")
public class DocumentationController {

    private final DocumentationService documentationService;

    public DocumentationController(DocumentationService documentationService) {
        this.documentationService = Objects.requireNonNull(documentationService, "documentationService must not be null");
    }

    @PostMapping("/generate")
    public ResponseEntity<DocumentationResult> generate(@RequestBody DocumentationRequest request) {
        return ResponseEntity.ok(documentationService.generateDocumentation(request));
    }

    @PostMapping("/diagram")
    public ResponseEntity<DiagramResult> diagram(@RequestBody DiagramRequest request) {
        return ResponseEntity.ok(documentationService.generateDiagram(request));
    }
}
