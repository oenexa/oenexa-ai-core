# OENEXA AI Engineer — Project Structure & Directory Blueprint

> **Component:** `[AI-BE] ⚡` OENEXA AI Engineer Backend  
> **Repository:** `C:\workspace\oenexarepo\oenexa-ai-engineer-backend`  
> **Target Framework:** Java 25+ | Spring Boot 4.1.1 | Spring AI 2.0.1 | Gradle 9.8.0  
> **Architecture:** 23 Modular Subsystems (Hexagonal Clean Architecture + 19 Platform Modules)  

---

## 1. High-Level Workspace Directory Layout

```text
oenexa-ai-engineer-backend/
├── .gitattributes               # LF line endings enforcement for *.sh, *.ps1, gradlew
├── .env.example                 # Environment configuration template
├── .dockerignore                # Build cache optimization for Docker
│
├── gradle/
│   ├── libs.versions.toml       # Central Version Catalog (Spring Boot 4.1.1, Java 25, Cucumber, Lombok)
│   └── wrapper/                 # Gradle Wrapper distribution (v9.8.0)
│
├── build.gradle.kts             # Root Gradle build script (23 modules, toolchains, JaCoCo gates)
├── settings.gradle.kts          # Multi-project module inclusion
├── gradle.properties            # Parallel compilation, configuration cache, JVM memory tuning
├── gradlew                      # POSIX wrapper script for Linux / macOS / Git Bash
├── gradlew.bat                  # Windows CMD / PowerShell wrapper script
│
├── run.sh                       # Unified POSIX runner script (api, gateway, dev, test, build, docker)
├── docker-runner.sh             # Linux / Git Bash Docker stack orchestrator
├── docker-runner.ps1            # Windows PowerShell Docker stack orchestrator
├── Dockerfile                   # Production multi-stage Eclipse Temurin 25 image (ZGC)
├── Dockerfile.fast              # Rapid container build using host-compiled bootJar
├── Dockerfile.frontend          # Multi-stage Node 22 + Nginx Alpine runner for paired UI
├── docker-compose.yml           # Multi-container stack (Backend, Frontend, Postgres 15, Redis 7)
├── docker-compose.dev.yml       # Development overrides with live-reload volume mounts
├── nginx.conf                   # Frontend SPA routing + /api/ reverse proxy (dual IPv4/IPv6)
│
├── ARCHITECTURE.md              # System design, C4 models, agent workflows, Docker topology
├── DESIGN_PROCESS.md            # Requirements, engineering lifecycle, milestone roadmap
├── DEVELOPMENT_NOTES.md         # Chronological architecture log and completed steps
├── DEVELOPMENT_PROCESS.md       # 6-stage engineering process and AI agent prompts library
├── PROJECT_STRUCTURE.md         # Comprehensive directory map (this document)
├── RULES_AND_STANDARDS.md       # Zero-mock testing rules, quality gates, and code conventions
└── README.md                    # Primary repository overview, quick start, and command guide
```

---

## 2. The 23 Modular Subsystems & Source Packages

```text
oenexa-ai-engineer-backend/
│
├── domain/                      # [Module 1/23] Pure Business Logic (Zero-Framework Inner Hexagon)
│   └── src/main/java/com/oenexa/domain/
│       ├── AgentContract.java          # Agent capability contracts and step definitions
│       ├── AgentType.java              # 15 specialized agent enum definitions
│       ├── AuthDomain.java             # User credentials, JWT claims, role definitions
│       ├── ChatDomain.java             # Chat messages, conversation sessions, token stats
│       ├── DomainEvent.java            # Immutable platform event contracts
│       ├── ErrorCode.java              # RFC 7807 standard error codes
│       ├── ExecutionContext.java       # Tenant and execution trace metadata
│       ├── OenexaException.java        # Structured domain exception
│       ├── PlannedModule.java          # Planned module metadata records
│       ├── PlatformModule.java         # Active platform module descriptors
│       ├── RagDocument.java            # RAG document, chunk, and vector records
│       ├── TenantContext.java          # ThreadLocal multi-tenant context holder
│       ├── User.java                   # User entity with business invariants
│       ├── UserRegistration.java       # User registration request record
│       └── Workflow.java               # 4 ordered multi-agent pipeline DAGs
│   └── src/test/java/com/oenexa/domain/# UserTest, DomainModelTest (100% Zero-Mock Line & Branch Coverage, 144/144 lines)
│
├── application/                 # [Module 2/23] Use-Case Orchestration (Hexagonal Middle Tier)
│   ├── src/main/java/com/oenexa/application/
│   │   ├── AgentCoordinatorService.java # Multi-agent workflow execution and delegation
│   │   ├── ChatService.java            # AI chat context window and reasoning
│   │   ├── PlatformEventPublisher.java # Domain event publishing abstraction
│   │   ├── RagService.java             # Document ingestion, chunking, and similarity search
│   │   ├── TokenService.java           # JWT token generation, parsing, and validation
│   │   └── UserService.java            # User registration and authentication use-cases
│   └── src/test/java/com/oenexa/application/ # (100% Zero-Mock Line & Branch Coverage, 179/179 lines)
│       ├── AgentCoordinatorServiceTest.java
│       ├── ChatServiceTest.java
│       ├── PlatformEventPublisherTest.java
│       ├── RagServiceTest.java
│       ├── TokenServiceTest.java
│       ├── UserServiceTest.java
│       └── bdd/                        # RunCucumberTest & UserRegistrationSteps (Cucumber-JVM 7.21.1)
│
├── infrastructure/              # [Module 3/23] Outgoing Adapters (Hexagonal Outer Tier)
│   ├── src/main/java/com/oenexa/infrastructure/
│   │   ├── InfrastructureConfig.java   # Flyway migrations, Redis, Kafka configuration beans
│   │   └── persistence/
│   │       ├── entity/                 # 14 Jakarta JPA Entities (scans, fixes, issues, deployments, etc.)
│   │       └── repository/             # 15 Spring Data JPA Repositories (CRUD & derived queries)
│   ├── src/main/resources/db/migration/
│   │   └── V1__init_oenexa_schema.sql  # Master PostgreSQL/H2 DDL schema migration
│   └── src/test/java/com/oenexa/infrastructure/ # (100% Zero-Mock Line & Branch Coverage, 374/374 lines)
│       ├── InfrastructureConfigTest.java
│       └── persistence/entity/EntityModelTest.java
│
├── api/                         # [Module 4/23] Incoming Adapter & Spring Boot MVC BootJar
│   ├── src/main/java/com/oenexa/api/
│   │   ├── ApiApplication.java         # Spring Boot entrypoint (@SpringBootApplication)
│   │   ├── AgentController.java        # /api/v1/agents endpoints
│   │   ├── AuthController.java         # /api/v1/auth endpoints (login, register, /me)
│   │   ├── ChatController.java         # /api/v1/chat endpoints (messages, history)
│   │   ├── GlobalErrorHandler.java     # RFC 7807 @ControllerAdvice ProblemDetail
│   │   ├── ModulesController.java      # /api/v1/modules platform discovery
│   │   └── RagController.java          # /api/v1/rag endpoints (documents, search)
│   └── src/main/resources/
│       ├── application.yml             # Local profile configuration (in-memory fallback)
│       └── application-docker.yml      # Container profile (Postgres, Redis, Actuator)
│
├── oenexa-common/               # [Module 5/23] Cross-Cutting Platform Kernel & Common Contracts
│   ├── src/main/java/ai/oenexa/common/
│   │   ├── error/                      # ErrorCode, OenexaException, GlobalErrorHandler
│   │   ├── event/                      # DomainEvent, EventPublisher
│   │   ├── module/                     # PlatformModule, PlannedModule
│   │   └── tenant/                     # TenantContext
│   └── src/test/java/ai/oenexa/common/ # GlobalErrorHandlerTest, PlatformModuleTest, TenantContextTest (100% Zero-Mock Coverage, 58/58 lines)
│
├── oenexa-auth/                 # [Module 6/23] Security, JJWT 0.12.6, RBAC Policies & Endpoints
│   ├── src/main/java/ai/oenexa/auth/
│   │   ├── domain/                     # Role, UserAccount, UserAccountPort, InMemoryUserAccountAdapter
│   │   ├── application/                # AuthService
│   │   ├── token/                      # JwtService (HS256 access & refresh tokens)
│   │   ├── web/                        # AuthController (/api/v1/auth/*), JwtAuthenticationManager
│   │   └── AuthModule.java             # PlatformModule definition (ACTIVE)
│   └── src/test/java/ai/oenexa/auth/   # AuthFlowTest (100% Zero-Mock Line & Branch Coverage, 112/112 lines)
│
├── oenexa-agent/                # [Module 7/23] Autonomous Multi-Agent Coordinator (15 Agents, 4 Workflows)
│   ├── src/main/java/ai/oenexa/agent/
│   │   ├── domain/                     # Agent (Task, Result, Delegation), AgentContext, AgentMemory, AgentReasoner, AgentType, Tool
│   │   ├── application/                # AgentCatalog (15 roles), Coordinator (Run, Step), RoleAgent, TemplateReasoner, Workflow
│   │   ├── web/                        # AgentController (/api/v1/agents/*)
│   │   └── AgentModule.java            # PlatformModule definition (ACTIVE)
│   └── src/test/java/ai/oenexa/agent/  # CoordinatorTest, web/AgentControllerTest (100% Zero-Mock Coverage, 153/153 lines, 100% branches)
│
├── oenexa-chat/                 # [Module 8/23] Conversational Assistant, Sliding Context Memory & SSE
│   ├── src/main/java/ai/oenexa/chat/
│   │   ├── domain/                     # ChatMessage, ConversationStore, LlmPort
│   │   ├── application/                # ChatService, ContextWindow
│   │   ├── infra/                      # EchoLlmAdapter, SpringAiLlmAdapter, InMemoryConversationStore
│   │   ├── web/                        # ChatController (/api/v1/chat/*)
│   │   └── ChatModule.java             # PlatformModule definition (ACTIVE)
│   └── src/test/java/ai/oenexa/chat/   # ChatServiceTest, ChatControllerTest (100% Zero-Mock Coverage, 80/80 lines)
│
├── oenexa-rag/                  # [Module 9/23] Semantic Embeddings, Chunking, Similarity Search
│   ├── src/main/java/ai/oenexa/rag/
│   │   ├── domain/                     # Chunk (record), Ports (EmbeddingPort, VectorStorePort, Scored)
│   │   ├── application/                # Chunker (paragraph/sentence splitter), RagService
│   │   ├── infra/                      # HashEmbeddingAdapter, InMemoryVectorStore
│   │   ├── web/                        # RagController (/api/v1/rag/*)
│   │   └── RagModule.java              # PlatformModule definition (ACTIVE)
│   └── src/test/java/ai/oenexa/rag/    # RagPipelineTest, RagControllerTest (100% Zero-Mock Coverage, 114/114 lines)
│
├── oenexa-core/                 # [Module 10/23] Relational Kernel, Domain Records, Ports, JPA & Web
│   ├── src/main/java/ai/oenexa/core/
│   │   ├── domain/                     # Organization, Team, Project, CoreUser records & Port interfaces
│   │   ├── persistence/                # InMemoryCorePersistenceAdapter, JpaCorePersistenceAdapter, JpaRepositories
│   │   ├── service/                    # CoreTenancyService, DefaultCoreTenancyService, PasswordHasher
│   │   ├── web/                        # CoreController (/api/v1/core/*)
│   │   └── CoreModule.java             # PlatformModule definition (ACTIVE)
│   └── src/test/java/ai/oenexa/core/   # CoreTenancyServiceTest (100% Zero-Mock Coverage, 281/281 lines)
│
├── oenexa-gateway/              # [Module 11/23] Executable Modular Monolith BootJar
│   ├── src/main/java/ai/oenexa/
│   │   ├── OenexaApplication.java      # Aggregated modular monolith launcher
│   │   └── gateway/                    # ModulesController (/api/v1/modules), SecurityConfig (Reactive Spring Security)
│   ├── src/main/resources/
│   │   └── application.yml             # Modular gateway configuration
│   └── src/test/java/ai/oenexa/gateway/# PlatformApiTest (100% Zero-Mock Coverage, 29/29 lines)
│
├── oenexa-code/                 # [Module 12/23] AST Analysis, Monaco Snippets, Code Generation
│   ├── src/main/java/ai/oenexa/code/
│   │   ├── model/                      # AstNode, AstAnalysisResult, CodeGenerationRequest, GeneratedCodeResult, RefactoringCandidate
│   │   ├── service/                    # CodeService, DefaultCodeService
│   │   └── web/                        # CodeController (/api/v1/code)
│   └── src/test/java/ai/oenexa/code/   # CodeServiceTest (100% Coverage, 172/172 lines)
│
├── oenexa-search/               # [Module 13/23] Codebase Full-Text and Symbol Search Engine
│   ├── src/main/java/ai/oenexa/search/
│   │   ├── model/                      # SearchType, SearchResultItem, SearchResult, ResearchCitation
│   │   ├── service/                    # SearchService, DefaultSearchService
│   │   └── web/                        # SearchController (/api/v1/search)
│   └── src/test/java/ai/oenexa/search/ # SearchServiceTest (100% Coverage, 71/71 lines)
│
├── oenexa-security/             # [Module 14/23] SAST Scanner, CVE Checks, OWASP Top-10 Audit
│   ├── src/main/java/ai/oenexa/security/
│   │   ├── model/                      # VulnerabilitySeverity, VulnerabilityFinding, SecretFinding, SecurityScanReport
│   │   ├── service/                    # SecurityService, DefaultSecurityService
│   │   └── web/                        # SecurityController (/api/v1/security)
│   └── src/test/java/ai/oenexa/security/ # SecurityServiceTest (100% Coverage, 132/132 lines)
│
├── oenexa-documentation/        # [Module 15/23] Markdown Spec, OpenAPI, and Diagram Generation
│   ├── src/main/java/ai/oenexa/documentation/
│   │   ├── model/                      # DocFormat, DocType, DocumentationResult, AdrDocument, DiagramResult
│   │   ├── service/                    # DocumentationService, DefaultDocumentationService
│   │   └── web/                        # DocumentationController (/api/v1/documentation)
│   └── src/test/java/ai/oenexa/documentation/ # DocumentationServiceTest (100% Coverage, 125/125 lines)
│
├── oenexa-review/               # [Module 16/23] AI Pull Request Reviewer, Risk Scoring
│   ├── src/main/java/ai/oenexa/review/
│   │   ├── model/                      # PullRequest, ReviewFinding, QualityScore, RiskScore, ReviewReport, DiffHunk
│   │   ├── service/                    # ReviewService, DefaultReviewService
│   │   └── web/                        # ReviewController (/api/v1/review)
│   └── src/test/java/ai/oenexa/review/ # ReviewServiceTest (100% Coverage, 113/113 lines)
│
├── oenexa-testing/              # [Module 17/23] Automated Test Generator, JaCoCo Coverage Gate
│   ├── src/main/java/ai/oenexa/testing/
│   │   ├── model/                      # TestFramework, TestCase, GeneratedTestSuite, CoverageReport
│   │   ├── service/                    # TestingService, DefaultTestingService
│   │   └── web/                        # TestingController (/api/v1/testing)
│   └── src/test/java/ai/oenexa/testing/ # TestingServiceTest (100% Coverage, 126/126 lines)
│
├── oenexa-devops/               # [Module 18/23] CI/CD Pipeline Generator, Dockerfiles, Helm
│   ├── src/main/java/ai/oenexa/devops/
│   │   ├── model/                      # ManifestType, DevopsFinding, ManifestAnalysisResult, CiPipelineResult
│   │   ├── service/                    # DevopsService, DefaultDevopsService
│   │   └── web/                        # DevopsController (/api/v1/devops)
│   └── src/test/java/ai/oenexa/devops/ # DevopsServiceTest (100% Coverage, 111/111 lines)
│
├── oenexa-pipeline/             # [Module 19/23] Task Orchestration DAG, Asynchronous Queues
│   ├── src/main/java/ai/oenexa/pipeline/
│   │   ├── model/                      # CiProvider, FailureCategory, PipelineFailureDiagnosis, PipelineRepairResult
│   │   ├── service/                    # PipelineService, DefaultPipelineService
│   │   └── web/                        # PipelineController (/api/v1/pipeline)
│   └── src/test/java/ai/oenexa/pipeline/ # PipelineServiceTest (100% Coverage, 86/86 lines)
│
├── oenexa-observability/        # [Module 20/23] OpenTelemetry Tracing, Prometheus Metrics
│   ├── src/main/java/ai/oenexa/observability/
│   │   ├── model/                      # LogLevel, IncidentSeverity, LogEvent, ErrorGroup, IncidentTimeline, LogAnalyticsSummary
│   │   ├── service/                    # ObservabilityService, DefaultObservabilityService
│   │   └── web/                        # ObservabilityController (/api/v1/observability)
│   └── src/test/java/ai/oenexa/observability/ # ObservabilityServiceTest (100% Coverage, 136/136 lines)
│
├── oenexa-repository/           # [Module 21/23] Git Clone, Tree Inspection, Diff Parsing
│   ├── src/main/java/ai/oenexa/repository/
│   │   ├── model/                      # VcsProvider, DependencyNode, RepositoryMetadata, ArchitectureGraph, CloneRepositoryRequest
│   │   ├── service/                    # RepositoryService, DefaultRepositoryService
│   │   └── web/                        # RepositoryController (/api/v1/repository)
│   └── src/test/java/ai/oenexa/repository/ # RepositoryServiceTest (100% Coverage, 90/90 lines)
│
├── oenexa-workspace/            # [Module 22/23] Tenant Workspace Sandboxes, Polyrepo Sync
│   ├── src/main/java/ai/oenexa/workspace/
│   │   ├── model/                      # WorkspaceRole, WorkspaceStatus, WorkspaceProject, Workspace, CreateWorkspaceRequest
│   │   ├── service/                    # WorkspaceService, DefaultWorkspaceService
│   │   └── web/                        # WorkspaceController (/api/v1/workspaces)
│   └── src/test/java/ai/oenexa/workspace/ # WorkspaceServiceTest (100% Coverage, 105/105 lines)
│
└── oenexa-admin/                # [Module 23/23] Tenant Onboarding, Audits, Quotas
    ├── src/main/java/ai/oenexa/admin/
    │   ├── model/                      # AuditAction, AuditLog, TenantUsageMetrics, TenantSettings
    │   ├── service/                    # AdminService, DefaultAdminService
    │   └── web/                        # AdminController (/api/v1/admin)
    └── src/test/java/ai/oenexa/admin/   # AdminServiceTest (100% Coverage, 87/87 lines)
```

---

## 3. Configuration & Runner Files

| File | Purpose | Execution Scope |
| :--- | :--- | :--- |
| **`gradle/libs.versions.toml`** | Gradle Version Catalog | Centralizes all framework versions (`spring-boot 4.1.1`, `spring-ai 2.0.1`, `cucumber 7.21.1`, `lombok 1.18.42`, `jacoco 0.8.13`). |
| **`run.sh`** | POSIX Shell Launcher | Primary developer script for Git Bash / Linux / macOS. Supports `api`, `gateway`, `dev`, `build`, `test`, `clean`, `status`, `docker`, and `help`. |
| **`docker-runner.sh`** | Linux Docker Runner | Orchestrates `docker-compose.yml` (`up`, `down`, `dev`, `build`, `status`, `logs`, `backend`, `frontend`, `clean`). |
| **`docker-runner.ps1`** | Windows Docker Runner | Native PowerShell equivalent of `docker-runner.sh` for Windows hosts. |
| **`Dockerfile`** | Backend Image | Multi-stage build (`eclipse-temurin:25-jdk` builder + `eclipse-temurin:25-jre` runner with Generational ZGC). |
| **`Dockerfile.fast`** | Fast Backend Image | 2-second rapid container build using host-built `api/build/libs/api-0.1.0-SNAPSHOT.jar`. |
| **`Dockerfile.frontend`**| Frontend Image | Multi-stage build (`node:22-alpine` builder + `nginx:alpine` runner with SPA routing). |
| **`docker-compose.yml`** | Production Compose | Coordinates backend (:8080), frontend (:3000), postgres:15 (:5432), and redis:7 (:6379) on bridge network `oenexa-network`. |
| **`nginx.conf`** | Web Server Config | Configures Nginx with dual IPv4 (`listen 80`) and IPv6 (`listen [::]:80`) listeners, client SPA fallback, and `/api/` proxy. |
| **`.gitattributes`** | Line Ending Rules | Enforces `eol=lf` for shell scripts (`*.sh`) and `gradlew` to prevent Windows CRLF syntax errors in Docker/Linux. |
