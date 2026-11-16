# 🚀 OENEXA DevOps Standards

## 1. Container & Deployment Invariants
- **Base Images**: Mandate Google Distroless Java 25 or Alpine minimal images.
- **Security Context**: Containers must run as non-root user (`runAsNonRoot: true`, `runAsUser: 65534`).
- **Read-Only Root Filesystem**: `readOnlyRootFilesystem: true` with ephemeral `/tmp` mounted via emptyDir.
- **Probes**: Mandatory `livenessProbe` and `readinessProbe` on `/actuator/health`.

## 2. CI/CD & GitOps
- Automated GitHub Actions running `./gradlew test` and `npm test -- --run`.
- ArgoCD GitOps synchronizing Kubernetes deployment manifests from `deploy/` directory.
- Pre-flight blast radius audit blocking releases with unmitigated `DROP TABLE` or `TRUNCATE` operations.
