# OENEXA AI Core

OENEXA AI Core is a Java/Spring backend for AI-assisted chat, agent workflows, code engineering, knowledge retrieval, and related platform services. It is a multi-module Gradle application organized around a hexagonal architecture.

## Capabilities

- Chat and conversation management, including streaming endpoints
- Agent execution, collaboration, autonomous goals, and workflow runs
- Code analysis, security scans, test generation, and engineering workflows
- Model routing and OpenAI-compatible API endpoints
- Retrieval-augmented generation, knowledge bases, and repository indexing
- Authentication, billing, voice, media, and MCP endpoints
- Health and application metrics through Spring Boot Actuator

The backend exposes these capabilities through REST APIs. Integrations and optional services depend on the active Spring profile and configuration; a route being present does not necessarily mean an external provider is configured.

## Architecture

| Module           | Responsibility                                                               |
|------------------|------------------------------------------------------------------------------|
| `domain`         | Framework-independent domain models and contracts                            |
| `application`    | Use cases, services, and application orchestration                           |
| `infrastructure` | Persistence and external-service adapters                                    |
| `api`            | Spring Boot application, REST controllers, configuration, and error handling |

The API is implemented with Java 25 and Spring Boot 4.1.1. The project uses Gradle Kotlin DSL, Spring MVC, Spring Data JPA, and Flyway. Local development uses H2; the Docker profile is configured for PostgreSQL and can enable Redis.

## Requirements

- JDK 25 (the Gradle build uses a Java 25 toolchain)
- Docker and Docker Compose for the containerized stack
- For the full Docker stack, a sibling checkout of [`oenexa-ai-web`](../oenexa-ai-web) is required by the Compose build context

## Run locally

The default `local` Spring profile uses an in-memory H2 database, so PostgreSQL and Redis are not required for a basic local run.

**Windows PowerShell**

```powershell
.\gradlew.bat :api:bootRun
```

**Linux, macOS, or Git Bash**

```bash
./gradlew :api:bootRun
```

The API listens on `http://localhost:8080` by default. A basic health endpoint is available at `GET /api/health`; Actuator health is available at `/actuator/health`.

To build the executable Spring Boot JAR:

```powershell
.\gradlew.bat :api:bootJar
```

The JAR is written to `api/build/libs/api-0.1.0-SNAPSHOT.jar`.

## Run with Docker Compose

The root Compose file starts PostgreSQL, Redis, the backend, and the web frontend. The frontend service expects `../oenexa-ai-web` to exist next to this repository.

```powershell
docker compose up -d --build
docker compose logs -f backend
```

By default, the frontend is exposed on port `3000`, the backend on `8080`, PostgreSQL on `5432`, and Redis on `6379`. Ports and service credentials can be overridden with environment variables. To load the provided template, copy `.env.example` to `.env` and edit it before starting Compose. Before using this stack outside local development, replace the sample database password and JWT signing secret with secure values.

## Tests and verification

Run the full Gradle test suite:

```powershell
.\gradlew.bat test
```

Run an individual test category across modules:

```powershell
.\gradlew.bat testUnit
.\gradlew.bat testBdd
.\gradlew.bat testTdd
```

Run the project verification task, including configured JaCoCo coverage checks:

```powershell
.\gradlew.bat check
```

The corresponding Linux/macOS commands use `./gradlew`. Coverage reports are generated under each module's `build/reports/jacoco/` directory.

## API areas

The main API groups are rooted at:

| API area                            | Base path                                                                 |
|-------------------------------------|---------------------------------------------------------------------------|
| Authentication                      | `/api/v1/auth`                                                            |
| Chat and conversations              | `/api/v1/chat`                                                            |
| Agents and workflows                | `/api/v1/agents`, `/api/v1/ecosystem`, `/api/v4/agents`                   |
| Code engineering                    | `/api/v1/code`, `/api/v1/security`, `/api/v1/testing`, `/api/v1/engineer` |
| Models and OpenAI-compatible routes | `/api/v1/models`, `/v1`                                                   |
| Knowledge and RAG                   | `/api/v1/knowledge`, `/api/v1/rag`                                        |
| Voice and media                     | `/api/v1/voice`, `/api/v1/media`                                          |
| Memory and MCP                      | `/api/v4/memory`, `/api/v4/mcp`                                           |
| Health                              | `/api/health`, `/actuator/health`                                         |

See the controllers in `api/src/main/java/com/oenexa/api/controller` for endpoint methods and request/response contracts.

## Configuration

Relevant environment variables include:

| Variable                                            | Purpose                                                                                     |
|-----------------------------------------------------|---------------------------------------------------------------------------------------------|
| `SPRING_PROFILES_ACTIVE`                            | Spring profile; the default is `local`                                                      |
| `SERVER_PORT`                                       | HTTP port; defaults to `8080`                                                               |
| `OENEXA_JWT_SECRET`                                 | JWT signing secret; set a high-entropy value of at least 32 bytes outside local development |
| `OENEXA_LLM_PROVIDER`                               | Selects the configured LLM provider; `echo` is the offline default                          |
| `OPENAI_API_KEY`                                    | API key for OpenAI-backed features when configured                                          |
| `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_DB` | PostgreSQL credentials and database name for Compose                                        |

`.env.example` is a template, not a production configuration. Copy and customize it only when you need environment-based settings.
