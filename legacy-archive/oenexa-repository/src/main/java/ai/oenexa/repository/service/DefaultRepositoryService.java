package ai.oenexa.repository.service;

import ai.oenexa.repository.domain.ArchitectureGraph;
import ai.oenexa.repository.domain.DependencyNode;
import ai.oenexa.repository.domain.RepositoryMetadata;
import ai.oenexa.repository.domain.VcsProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class DefaultRepositoryService implements RepositoryService {

    @Override
    public RepositoryMetadata inspectRepository(String repositoryUrl, VcsProvider provider) {
        Objects.requireNonNull(repositoryUrl, "repositoryUrl must not be null");
        if (provider == null) {
            provider = VcsProvider.GITHUB;
        }

        List<String> languages = new ArrayList<>(List.of("Java", "TypeScript", "SQL", "Dockerfile"));
        if (repositoryUrl.contains("python")) {
            languages.add("Python");
        }

        return new RepositoryMetadata(
                repositoryUrl,
                provider,
                "main",
                128,
                languages
        );
    }

    @Override
    public ArchitectureGraph analyzeArchitecture(String repositoryUrl, List<String> knownModules) {
        Objects.requireNonNull(repositoryUrl, "repositoryUrl must not be null");

        String repoName = repositoryUrl.substring(repositoryUrl.lastIndexOf('/') + 1);
        if (repoName.endsWith(".git")) {
            repoName = repoName.substring(0, repoName.length() - 4);
        }
        if (repoName.isBlank()) {
            repoName = "oenexa-repo";
        }

        List<String> modules = knownModules != null && !knownModules.isEmpty()
                ? knownModules
                : List.of("domain", "application", "infrastructure", "api");

        List<DependencyNode> dependencies = new ArrayList<>();
        dependencies.add(new DependencyNode("spring-boot-starter-web", "4.1.1", "api", List.of()));
        dependencies.add(new DependencyNode("spring-ai-core", "2.0.1", "api", List.of()));
        dependencies.add(new DependencyNode("jjwt-api", "0.12.6", "api", List.of("jjwt-impl", "jjwt-jackson")));

        String mermaid = generateDependencyMermaid(dependencies);

        return new ArchitectureGraph(repoName, modules, dependencies, mermaid);
    }

    @Override
    public String generateDependencyMermaid(List<DependencyNode> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return "graph TD\n    Root[\"Empty Architecture\"]";
        }

        StringBuilder sb = new StringBuilder("graph TD\n");
        for (int i = 0; i < nodes.size(); i++) {
            DependencyNode node = nodes.get(i);
            String id = "D" + i;
            sb.append("    ").append(id).append("[\"").append(node.artifactId()).append(" (").append(node.version()).append(")\"]\n");
            for (int j = 0; j < node.transitiveDependencies().size(); j++) {
                String trans = node.transitiveDependencies().get(j);
                String transId = id + "_" + j;
                sb.append("    ").append(transId).append("[\"").append(trans).append("\"]\n");
                sb.append("    ").append(id).append(" --> ").append(transId).append("\n");
            }
        }
        return sb.toString().stripTrailing();
    }
}
