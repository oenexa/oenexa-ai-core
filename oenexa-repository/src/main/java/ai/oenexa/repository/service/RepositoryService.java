package ai.oenexa.repository.service;

import ai.oenexa.repository.domain.ArchitectureGraph;
import ai.oenexa.repository.domain.DependencyNode;
import ai.oenexa.repository.domain.RepositoryMetadata;
import ai.oenexa.repository.domain.VcsProvider;
import java.util.List;

/**
 * Service port for inspecting remote repositories and synthesizing architectural graphs.
 */
public interface RepositoryService {

    RepositoryMetadata inspectRepository(String repositoryUrl, VcsProvider provider);

    ArchitectureGraph analyzeArchitecture(String repositoryUrl, List<String> knownModules);

    String generateDependencyMermaid(List<DependencyNode> nodes);
}
