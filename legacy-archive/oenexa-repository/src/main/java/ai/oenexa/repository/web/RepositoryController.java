package ai.oenexa.repository.web;

import ai.oenexa.repository.domain.ArchitectureGraph;
import ai.oenexa.repository.domain.RepositoryMetadata;
import ai.oenexa.repository.domain.VcsProvider;
import ai.oenexa.repository.service.RepositoryService;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/repository")
public class RepositoryController {

    private final RepositoryService repositoryService;

    public RepositoryController(RepositoryService repositoryService) {
        this.repositoryService = Objects.requireNonNull(repositoryService, "repositoryService must not be null");
    }

    @PostMapping("/inspect")
    public ResponseEntity<RepositoryMetadata> inspect(@RequestBody Map<String, String> payload) {
        String url = payload.getOrDefault("url", "https://github.com/oenexa/oenexa-ai-engineer-backend");
        String providerStr = payload.getOrDefault("provider", "GITHUB");
        VcsProvider provider = VcsProvider.valueOf(providerStr);
        return ResponseEntity.ok(repositoryService.inspectRepository(url, provider));
    }

    @PostMapping("/architecture")
    @SuppressWarnings("unchecked")
    public ResponseEntity<ArchitectureGraph> architecture(@RequestBody Map<String, Object> payload) {
        String url = (String) payload.getOrDefault("url", "https://github.com/oenexa/oenexa-ai-engineer-backend");
        List<String> modules = (List<String>) payload.getOrDefault("modules", List.of());
        return ResponseEntity.ok(repositoryService.analyzeArchitecture(url, modules));
    }
}
