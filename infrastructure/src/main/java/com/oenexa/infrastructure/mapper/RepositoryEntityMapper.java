package com.oenexa.infrastructure.mapper;

import com.oenexa.domain.model.code.CodeEngineeringDomain.CodeAnalysisResponse;
import com.oenexa.infrastructure.persistence.entity.RepositoryEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Enterprise Entity Mapper Layer for Code Repositories.
 * Translates between JPA RepositoryEntity rows and domain repository representations.
 */
@Component
public class RepositoryEntityMapper {

    public RepositoryEntity toEntity(String projectId, String name, String cloneUrl, String defaultBranch) {
        String id = UUID.randomUUID().toString();
        String project = (projectId != null && !projectId.isBlank()) ? projectId : UUID.randomUUID().toString();
        String repoName = (name != null && !name.isBlank()) ? name : "oenexa-repo";
        String url = (cloneUrl != null && !cloneUrl.isBlank()) ? cloneUrl : "https://github.com/oenexa/" + repoName;
        String branch = (defaultBranch != null && !defaultBranch.isBlank()) ? defaultBranch : "main";

        return new RepositoryEntity(id, project, repoName, url, "GITHUB", branch);
    }

    public CodeAnalysisResponse toDomainSummary(RepositoryEntity entity, int fileCount) {
        if (entity == null) {
            return null;
        }
        return new CodeAnalysisResponse(
                "java",
                fileCount * 120,
                fileCount,
                fileCount * 4,
                fileCount * 5,
                List.of(entity.getName()),
                List.of("main()"),
                List.of("com.oenexa.*"),
                List.of(),
                true
        );
    }
}

