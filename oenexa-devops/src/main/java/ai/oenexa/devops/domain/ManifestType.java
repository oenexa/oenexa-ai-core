package ai.oenexa.devops.domain;

/**
 * Supported DevOps infrastructure manifest formats.
 */
public enum ManifestType {
    DOCKERFILE,
    DOCKER_COMPOSE,
    KUBERNETES_MANIFEST,
    HELM_VALUES
}
