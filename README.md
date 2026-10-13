# OENEXA AI Engineer

## Overview
OENEXA AI Engineer is an **enterprise‑grade AI‑powered software‑engineering platform** that can automatically understand, analyze, debug, fix, improve, and document software projects.  It combines a modular Java backend, a modern React UI, and a multi‑agent AI orchestration layer.

## Technology Stack
| Layer                  | Technologies                                                                                                                           |
|------------------------|----------------------------------------------------------------------------------------------------------------------------------------|
| **Backend**            | Java 25, Spring Boot 3.x, Spring WebFlux, Spring Security, Spring AI, Spring Data JPA, PostgreSQL, Redis, Kafka, Elasticsearch, Flyway |
| **AI Layer**           | OpenAI, Claude, Gemini, Ollama (local models), RAG architecture, Agent orchestration                                                   |
| **Frontend**           | React, TypeScript, Material‑UI, TailwindCSS, Vite, Monaco editor                                                                       |
| **Infrastructure**     | Docker, Docker‑Compose, Kubernetes, Helm, Argo CD, GitHub Actions, Terraform                                                           |
| **Architecture Style** | Modular monolith (easily convertible to microservices), Hexagonal / Clean Architecture, Domain‑Driven Design                           |

## Quick Start (Local Development)
```bash
# Clone the repo (already created in C:\workspace\oenexa-ai-engineer)
cd C:\workspace\oenexa-ai-engineer

# Start dependent services
docker compose up -d

# Build and run the backend
./gradlew :api:bootRun   # runs the API on http://localhost:8080

# Start the UI (in a separate terminal)
cd ui
npm install
npm run dev   # opens http://localhost:3000
```
The UI proxies `/api/*` to the backend, so you can see the health check and the Monaco editor.

## Project Structure
See **[PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md)** for a detailed directory layout.

## Architecture
High‑level architecture is described in **[ARCHITECTURE.md](ARCHITECTURE.md)**.  It includes:
- Multi‑module backend layers (Domain, Application, Infrastructure, API) following Hexagonal architecture.
- AI Agent Orchestration Service that routes work to specialized agents (Repository, Architecture, Bug‑Fix, etc.).
- Observability stack (Prometheus, Grafana, OpenTelemetry, Loki).
- CI/CD pipeline (GitHub Actions → Docker → Helm → Argo CD).

## Documentation
Additional docs are generated as part of the build:
- Swagger / OpenAPI UI available at `http://localhost:8080/swagger-ui.html`
- Javadoc generated with `./gradlew javadoc`
- Architecture diagrams (Mermaid) are included in the markdown files.

## License
Apache‑2.0
"# oenexa-ai-engineer-backend"
# oenexa-ai-engineer-backend
