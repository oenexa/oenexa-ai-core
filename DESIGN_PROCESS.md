# Design Process for OENEXA AI Engineer

## 1. Requirements & Stakeholder Alignment
- **Stakeholder workshops** with product owners, engineering leads, security team, and AI researchers.
- Capture **functional** (AI agents, UI, API generation) and **non‑functional** (scalability, latency, security, observability) requirements.
- Produce a **requirements backlog** in the `issues/` repository with clear acceptance criteria.

## 2. High‑Level Architecture Definition
- Draft **system context diagram** (external systems: Git providers, LLM providers, Cloud services).
- Choose **modular monolith** as the foundation; define **hexagonal boundaries** (`domain`, `application`, `infrastructure`, `api`) accompanied by 19 specialized platform modules (`oenexa-*`). The Web UI is completely decoupled in the companion repository `oenexa-ai-engineer-frontend`.
- Identify **cross‑cutting concerns** (security, multi-tenancy, logging, metrics, RFC 7807 error handling).
- Validate architecture against **C4 model** layers using Mermaid diagrams in `ARCHITECTURE.md`.

## 3. Detailed Component Design
### 3.1 Backend Modules (23 Modules)
- **Domain (`domain`)** – Pure Java 25 business records, immutable domain events, execution context, and zero framework lock-in.
- **Application (`application`)** – Use‑case orchestration, `AgentCoordinatorService`, `ChatService`, `RagService`, and `TokenService`.
- **Infrastructure (`infrastructure`)** – DataSource, PostgreSQL JPA entities, Flyway migrations, Redis cache, Kafka producer/consumer, Elasticsearch client.
- **API (`api`)** – Spring MVC REST controllers (`@RestController`) powered by Java virtual threads, OpenAPI 3.1 spec generation, ProblemDetail error handlers.
- **Platform Modules (`oenexa-*`)** – 19 focused subsystems (`common`, `auth`, `agent`, `chat`, `rag`, `core`, `gateway`, `code`, `search`, `security`, `documentation`, `review`, `testing`, `devops`, `pipeline`, `observability`, `repository`, `workspace`, `admin`).
- **AI Agent Layer** – 15 specialized agent contracts (`AgentContract`), 4 ordered workflow pipelines (`Workflow`), sliding context window manager.

### 3.2 UI Design (Decoupled Frontend Repository)
- **Component library**: React 18, Material‑UI 5, Tailwind CSS.
- **Editor integration**: Monaco editor with AI‑suggestion overlay and real-time backend health monitor.
- **Reverse Proxy**: Nginx Alpine container forwarding `/api/` traffic to the backend on port 8080.

### 3.3 Security Design
- **Authentication** – RFC 7519 JWT (access & refresh tokens) via JJWT 0.12.6.
- **Authorization** – RBAC policies, method security annotations, multi-tenant isolation via `TenantContext`.
- **Hardening** – OWASP Top‑10 sanitization, secret scanning, CSP headers, immutable record parameters.

### 3.4 Observability Design
- **Metrics** – Micrometer + Prometheus metrics for JVM, HTTP endpoints, agent execution queues (`/actuator/prometheus`).
- **Tracing** – OpenTelemetry 1.52.0 SDK distributed tracing.
- **Logging** – JSON logback to Loki/ELK.
- **Dashboards** – Grafana panels for request latency, error rates, AI agent queue depth.

## 4. Data Model & Persistence
- Create **ER diagram** (Mermaid) covering users, projects, repositories, scans, issues, fixes, PRs, tests, security findings, deployments, logs, agent tasks.
- Map to **Flyway migrations** (`V1__init.sql`).
- Use **Spring Data JPA** repositories with proper indexes.

## 5. AI Agent Orchestration Blueprint
- **Coordinator service** (Spring bean) receives a task request, selects the appropriate agent(s), persists context in Redis.
- Agents are **stateless**; they can be scaled horizontally.
- Define **communication contracts** (DTOs) between coordinator and agents.
- Implement **fallback & retry** logic for LLM API failures.

## 6. CI/CD & Deployment Architecture
- **GitHub Actions workflow**: lint → unit tests → integration tests (Testcontainers) → static analysis → build Docker image → push to GHCR → helm package → argo‑cd sync.
- **Docker‑Compose** for local development (Postgres, Redis, Kafka, Elasticsearch, API, UI).
- **Helm chart** with separate sub‑charts for each service; values file supports cloud‑specific overrides (GKE, AKS, EKS).
- **Argo CD** as GitOps engine to continuously deploy changes.

## 7. Review & Validation
- Conduct **architecture review** with security & performance specialists.
- Run **load testing** (k6, Gatling) against a staged environment.
- Perform **penetration testing** on authentication and API endpoints.
- Iterate based on findings.

---

# Development Plan (12‑Week Sprint Roadmap)

## Week 0 – Project Kick‑off
- Initialize repo, Gradle multi‑module scaffold (already done).
- Set up CI pipeline skeleton.
- Agree on coding standards, branch strategy (GitFlow).

## Week 1 – Core Backend Foundations
- Implement **Domain** entities (`User`, `Project`, `Repository`).
- Create **Flyway** migrations for core tables.
- Configure **Infrastructure** beans (DataSource, Kafka, Redis, Elasticsearch).
- Add **unit tests** for repositories.

## Week 2 – Application Layer & Security Basics
- Build **Application services** (e.g., `UserService`, `ProjectService`).
- Implement **JWT authentication** (login, refresh).
- Add **RBAC** annotations to services.

## Week 3 – API Layer & OpenAPI
- Write **WebFlux controllers** for CRUD endpoints.
- Generate **OpenAPI spec** via SpringDoc and expose Swagger UI.
- Add **integration tests** using Testcontainers (Postgres, Kafka).

## Week 4 – UI Scaffold & Editor Integration
- Finalize **React/Vite** scaffold (already done).
- Connect UI health check to backend.
- Integrate **Monaco editor** with basic AI suggestion placeholder.

## Week 5 – AI Agent Framework
- Define `Agent` interface and **CoordinatorService**.
- Implement **RepositoryIntelligenceAgent** (clone repo, build dependency graph stub).
- Implement **ArchitectureUnderstandingAgent** (parse source, generate UML stub).
- Add **unit tests** for agent contracts.

## Week 6 – Expanded Agent Capabilities
- Add **BugFixAgent** (parse compiler errors, generate patch stub).
- Add **DocumentationAgent** (auto‑generate README, Swagger docs). 
- Wire agents into the **Coordinator** with a simple routing map.

## Week 7 – Observability & Telemetry
- Enable **Micrometer** Prometheus exporter, expose `/actuator/prometheus`.
- Add **OpenTelemetry** auto‑instrumentation (Java agent).
- Configure **logback** to output JSON for Loki.
- Provide **Grafana dashboards** (import JSON). 

## Week 8 – CI/CD Pipeline Completion
- Complete **GitHub Actions** workflow (lint, test, static analysis, Docker build, Helm release).
- Add **secret scanning** (GitHub secret scanning, Gitleaks step).
- Validate pipeline on a feature branch.

## Week 9 – Helm Chart & Kubernetes Deployment
- Create **Helm chart** with sub‑charts for API, UI, Postgres, Redis, Kafka, Elasticsearch.
- Deploy to a **dev GKE cluster** via Argo CD.
- Verify health, logs, and metrics collection.

## Week 10 – Security Hardening & Pen‑Test
- Run **OWASP ZAP** scan against the deployed API.
- Harden **CORS**, **Content‑Security‑Policy**, **Rate Limiting**.
- Add **MFA** flow for admin users (TOTP placeholder).

## Week 11 – Performance & Load Testing
- Execute **k6** scripts simulating concurrent AI‑agent requests.
- Tune **JVM**, **thread pools**, **Kafka producer configs**.
- Optimize **database indexes** based on query logs.

## Week 12 – Release Candidate & Documentation
- Generate **final documentation** (architecture diagrams, API docs, ADRs).
- Freeze **release branch**, tag `v0.1.0`.
- Publish Docker images, Helm chart, and update **README** with deployment steps.
- Conduct a **post‑mortem** to capture lessons learned.

---

## Milestones & Deliverables
| Milestone                                 | Expected Output                                                 | Status |
|-------------------------------------------|-----------------------------------------------------------------|:---|
| **M1 – Core Backend** (Hexagonal Core)    | Pure Java 25 domain records, Flyway migrations, Spring MVC REST API with JJWT. | ✅ **COMPLETED** |
| **M2 – UI & Editor** (Decoupled Repo)     | React 18, Vite 5, Monaco code editor, real-time health probe.   | ✅ **COMPLETED** |
| **M3 – AI Agent MVP** (Orchestration)     | 15 agent types, 4 ordered workflow pipelines, RAG retrieval.    | ✅ **COMPLETED** |
| **M4 – Observability Stack**              | OpenTelemetry 1.52.0, Prometheus Micrometer, Actuator readiness. | ✅ **COMPLETED** |
| **M5 – Platform Monolith Migration**      | 19 platform modules ported, tested, and verified under Gradle.  | ✅ **COMPLETED** |
| **M6 – Docker Stack & Dual Runners**      | Multi-stage Temurin 25 JRE, Nginx SPA proxy, Compose orchestrators (`run.sh`, `docker-runner.sh`). | ✅ **COMPLETED** |
| **M7 – Release Candidate & Verification** | Zero-mock test suite, 80% JaCoCo coverage gate, 100% healthy containers. | ✅ **COMPLETED** |

---

## Risk Management
- **LLM API rate limits** – implement exponential back‑off and local cache.
- **Schema drift** – enforce Flyway baseline and CI migration checks.
- **Agent stability** – run agents in separate thread pools, monitor queue depth.
- **Compliance** – ensure data handling follows GDPR/CCPA (audit logs, data masking).

---

*All diagrams are expressed in Mermaid syntax and can be rendered directly in the Markdown files.*
