# OENEXA AI Engineer

## Overview
OENEXA AI Engineer is an **enterprise‑grade AI‑powered software‑engineering platform** that can automatically understand, analyze, debug, fix, improve, and document software projects.  It combines a modular Java backend, a modern React UI, and a multi‑agent AI orchestration layer.

## Technology Stack
| Layer                    | Technologies                                                                                                                            |
|--------------------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| Layer                    | Technologies                                                                                                                            |
| ------------------------ | -----------------------------------------------------------------------------------------------------------------------                 |
| **Backend**              | Java 25, Spring Boot 4.1.1, Spring AI 2.0.1, Spring MVC (Virtual Threads), Spring WebFlux, Flyway, PostgreSQL 15, Redis 7, Gradle 9.8.0 |
| **AI Layer**             | Spring AI 2.0.1, Multi-Agent Orchestration (15 Agents, 4 Workflows), Semantic RAG, Sliding Context Window Memory                        |
| **Testing**              | Cucumber-JVM 7.21.1 (BDD), JUnit 5 Platform (Zero-Mock Policy), JaCoCo 0.8.13 (80% Line/Branch Coverage Gate)                           |
| **Frontend**             | React 18, TypeScript, Material-UI 5, Tailwind CSS, Vite 5, Monaco Editor                                                                |
| **Infrastructure**       | Docker, Docker Compose, Nginx Alpine Reverse Proxy, Kubernetes, Helm, Argo CD, GitHub Actions                                           |
| **Architecture Style**   | Clean / Hexagonal Modular Monolith (23 Specialized Modules, Version Catalog in `gradle/libs.versions.toml`)                             |

## Quick Start (Linux & Git Bash)

The repository provides an automated, POSIX-compliant `./run.sh` script:

```bash
# Display help and available commands
./run.sh help

# Check system prerequisites (JDK 25+, Gradle wrapper)
./run.sh status

# Run the API Application (default port: 8080)
./run.sh api

# Or run on a custom port
./run.sh api --server.port=8089

# Run the Oenexa Gateway service (Modular Monolith with all 19 modules)
./run.sh gateway

# Run with hot reload (dev mode)
./run.sh dev

# Run comprehensive test suite across all 23 modules with JaCoCo coverage
./run.sh test

# Build the production executable JARs (:api and :oenexa-gateway)
./run.sh build
```

### Windows PowerShell Quick Start
```powershell
# Run the API
.\gradlew.bat :api:bootRun

# Run full test suite & coverage gate across all 23 modules
.\gradlew.bat check
```

## Docker Runner (Frontend + Backend Stack)

The backend repository includes automated Docker runner scripts to orchestrate the entire platform (Backend, Frontend, PostgreSQL 15, Redis 7):

### Linux & Git Bash
```bash
# Start all containers in background (Frontend :3000, Backend :8080, DB :5432, Redis :6379)
./docker-runner.sh up

# Or launch via the unified run.sh launcher
./run.sh docker up

# View container health and exposed URLs
./docker-runner.sh status

# Stream container logs in real time
./docker-runner.sh logs backend -f

# Start in development live-reload mode
./docker-runner.sh dev

# Stop and tear down all containers
./docker-runner.sh down
```

### Windows PowerShell
```powershell
# Start entire platform stack
.\docker-runner.ps1 up

# Check status
.\docker-runner.ps1 status

# Stream logs
.\docker-runner.ps1 logs

# Stop containers
.\docker-runner.ps1 down
```

## Verified Endpoints & API Capabilities

| Protocol | Method     | Route                                       | Description                                                       |
|:---------|:-----------|:--------------------------------------------|:------------------------------------------------------------------|
| **HTTP** | `GET`      | `/actuator/health`                          | Spring Boot Actuator liveness and readiness probes                |
| **HTTP** | `GET`      | `/api/v1/modules`                           | Discovery list of all 20 active platform modules and capabilities |
| **HTTP** | `POST`     | `/api/v1/chat/messages`                     | AI conversational chat completion with context memory             |
| **HTTP** | `POST`     | `/api/v1/auth/login`                        | User authentication issuing RFC 7519 JWT tokens                   |
| **HTTP** | `POST`     | `/api/v1/agents/workflows/{workflowId}/run` | Multi-agent autonomous workflow pipeline execution                |
| **HTTP** | `POST`     | `/api/v1/rag/ingest`                        | Ingest, chunk and index documents into semantic knowledge base    |
| **HTTP** | `POST`     | `/api/v1/rag/query`                         | Vector similarity search and context building with citations      |
| **HTTP** | `POST`     | `/api/v1/code/ast/analyze`                  | AST node parsing, smell detection, and complexity analysis        |
| **HTTP** | `POST`     | `/api/v1/review/pull-requests`              | AI automated PR review, risk scoring, and diff hunk grading       |
| **HTTP** | `POST`     | `/api/v1/security/sast/scan`                | SAST vulnerability scanner, CVE checks, OWASP Top-10 audit        |
| **HTTP** | `POST`     | `/api/v1/testing/generate`                  | Automated test synthesis (JUnit 5, Cucumber BDD, Vitest, PyTest)  |
| **HTTP** | `POST`     | `/api/v1/documentation/generate`            | Architecture markdown documentation, ADRs, and Mermaid diagrams   |
| **HTTP** | `POST`     | `/api/v1/devops/manifest/analyze`           | Dockerfile, Kubernetes, Compose, Helm linting and security scan   |
| **HTTP** | `POST`     | `/api/v1/pipeline/diagnose`                 | CI/CD failure diagnosis and automated script repair synthesis     |
| **HTTP** | `GET`      | `/api/v1/search/query`                      | Full-text, semantic vector, and hybrid RRF codebase search        |
| **HTTP** | `POST`     | `/api/v1/workspaces`                        | Multi-tenant organization and team workspace sandbox isolation    |
| **HTTP** | `POST`     | `/api/v1/admin/audit`                       | Tamper-evident audit logging and tenant compute/token analytics   |
| **HTTP** | `POST`     | `/api/v1/repository/inspect`                | VCS remote inspection, dependency tree, and Mermaid graph mapping |
| **HTTP** | `POST`     | `/api/v1/observability/events`              | Cross-service error fingerprint grouping and incident correlation |
| **HTTP** | `GET/POST` | `/api/v1/core/*`                            | Multi-tenant kernel: organizations, teams, projects, and users    |
| **HTTP** | `GET`      | `/swagger-ui.html`                          | Interactive Swagger / OpenAPI 3.1 documentation                   |

## Master Documentation Directory

| Document                                             | Description                                                                                                            |
|:-----------------------------------------------------|:-----------------------------------------------------------------------------------------------------------------------|
| **[ARCHITECTURE.md](ARCHITECTURE.md)**               | Comprehensive C4 diagrams, 23 modules breakdown, multi-agent workflows, Docker topology, and 100% test coverage stats. |
| **[PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md)**     | Directory and file tree layout covering all 23 modules, configs, domain models, services, and tests.                   |
| **[DEVELOPMENT_NOTES.md](DEVELOPMENT_NOTES.md)**     | Chronological engineering log with symbolic tags (`[CODE]`, `[REVIEW]`, etc.) covering all development steps.          |
| **[DEVELOPMENT_PROCESS.md](DEVELOPMENT_PROCESS.md)** | 6-stage feature lifecycle, AI agent prompts library, and Stage 0 symbolic ledger mandate.                              |
| **[RULES_AND_STANDARDS.md](RULES_AND_STANDARDS.md)** | Rule 1.0 (Symbolic Ledger), strictly Zero-Mock testing policy, JaCoCo gates, and Docker conventions.                   |
| **[DESIGN_PROCESS.md](DESIGN_PROCESS.md)**           | System requirements, 12-week development roadmap, and risk management plan.                                            |

## License
Apache-2.0


