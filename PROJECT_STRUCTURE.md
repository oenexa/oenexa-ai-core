# OENEXA AI Core — Project Structure & Directory Blueprint

> **Component:** `[AI-CORE] ⚡` OENEXA AI Core Backend  
> **Repository:** `C:\workspace\oenexarepo\oenexa-ai-core`  
> **Project Name:** `oenexa-ai-core`  
> **Target Framework:** Java 25+ | Spring Boot 4.1.1 | Spring AI 2.0.1 | Gradle 9.8.0  
> **Architecture:** Clean Hexagonal Architecture (Domain, Application, Infrastructure, API)  

---

## 1. High-Level Workspace Directory Layout

```text
oenexa-ai-core/
├── .gitattributes               # LF line endings enforcement for *.sh, *.ps1, gradlew
├── .env.example                 # Environment configuration template
├── .dockerignore                # Build cache optimization for Docker
│
├── gradle/
│   ├── libs.versions.toml       # Central Version Catalog (Spring Boot 4.1.1, Java 25, Cucumber, Lombok)
│   └── wrapper/                 # Gradle Wrapper distribution (v9.8.0)
│
├── charts/
│   └── oenexa-ai-core/          # Helm chart packaging for Kubernetes deployment
│
├── build.gradle.kts             # Root Gradle build script (4 hexagonal modules, JaCoCo gates)
├── settings.gradle.kts          # Multi-project module inclusion (rootProject.name = "oenexa-ai-core")
├── gradle.properties            # Parallel compilation, configuration cache, JVM memory tuning
├── gradlew                      # POSIX wrapper script for Linux / macOS / Git Bash
├── gradlew.bat                  # Windows CMD / PowerShell wrapper script
│
├── run.sh                       # Unified POSIX runner script (api, dev, test, build, docker)
├── docker-runner.sh             # Linux / Git Bash Docker stack orchestrator
├── docker-runner.ps1            # Windows PowerShell Docker stack orchestrator
├── Dockerfile                   # Production multi-stage Eclipse Temurin 25 image (ZGC)
├── Dockerfile.fast              # Rapid container build using host-compiled bootJar
├── docker-compose.yml           # Multi-container stack (Backend, Frontend, Postgres 15, Redis 7)
├── docker-compose.dev.yml       # Development overrides with live-reload volume mounts
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

## 2. Hexagonal Subsystems & Source Packages

```text
oenexa-ai-core/
│
├── domain/                      # [Module 1/4] Pure Business Logic (Zero-Framework Inner Hexagon)
│   └── src/main/java/com/oenexa/domain/
│       ├── AgentContract.java                    # Agent capability contracts and step definitions
│       ├── AutonomousAgentEcosystemDomain.java   # 10 Autonomous Agent personas, DAG, reflections
│       ├── EnterpriseMemoryHierarchyDomain.java  # 7-layer memory hierarchy contracts
│       ├── ModelContextProtocolDomain.java       # MCP v1.0 schema, resources, tools, prompts
│       ├── AiEngineerDomain.java                 # AI engineer models, analysis & generation types
│       ├── AuthDomain.java                       # User credentials, JWT claims, role definitions
│       ├── ChatDomain.java                       # Chat messages, conversation sessions, token stats
│       ├── DomainEvent.java                      # Immutable platform event contracts
│       ├── KnowledgeDomain.java                  # Knowledge base items, chunks, embeddings
│       ├── ProjectKnowledgeDomain.java           # AST parsing, code entity knowledge mapping
│       ├── SecurityAuditDomain.java              # Vulnerability reports, OWASP scan records
│       ├── SubscriptionDomain.java               # Enterprise billing, quotas, invoice records
│       ├── VoiceDomain.java                      # Audio transcription, TTS, meeting diarization
│       ├── MediaDomain.java                      # Image generation, vision OCR, video scenes
│       └── WorkspaceDomain.java                  # Multi-tenant workspaces and member roles
│
├── application/                 # [Module 2/4] Application Orchestration & Use Cases
│   └── src/main/java/com/oenexa/application/
│       ├── EnterpriseAgentEcosystemService.java  # 10-Agent autonomous collaboration engine
│       ├── EnterpriseMemoryHierarchyService.java # 7-layer memory with NVMe SSD mmap cache
│       ├── ModelContextProtocolService.java      # MCP v1.0 server & tool execution runtime
│       ├── AiSoftwareEngineerService.java        # Code understanding, refactoring, test generation
│       ├── AuthenticationService.java            # JWT authentication, Argon2id, RBAC verification
│       ├── IntelligentConversationService.java   # Gemini Pro chat, prompt templates, SSE streaming
│       ├── EnterpriseKnowledgeBaseService.java   # RAG vectorization, chunking, similarity search
│       ├── EnterpriseProjectAnalysisService.java # AST repository indexing, dependency graphs
│       ├── EnterpriseSecurityAuditService.java   # Static analysis, secret scanning, CVE checks
│       ├── EnterpriseSubscriptionService.java    # Tiered billing, Stripe checkout, usage quotas
│       ├── EnterpriseVoiceService.java           # Real-time STT, voice commands, TTS synthesizer
│       ├── EnterpriseMediaService.java           # Multimodal image editing, OCR, video synthesis
│       └── EnterpriseWorkspaceService.java       # Workspace tenancy lifecycle & role binding
│
├── infrastructure/              # [Module 3/4] External Adapters & Persistence
│   └── src/main/java/com/oenexa/infrastructure/
│       ├── persistence/entity/                   # JPA Entities for PostgreSQL
│       ├── persistence/repository/               # Spring Data JPA repositories
│       ├── memory/                               # Zero-copy memory-mapped file buffer adapter
│       └── event/                                # In-process & distributed event adapters
│
└── api/                         # [Module 4/4] Inbound REST API & Spring Boot Application
    └── src/main/java/com/oenexa/api/
        ├── OenexaApiApplication.java             # Executable Spring Boot 4 Application Entrypoint
        ├── AutonomousAgentEcosystemController.java # /api/v4/agents/*
        ├── EnterpriseMemoryHierarchyController.java# /api/v4/memory/*
        ├── ModelContextProtocolController.java   # /api/v4/mcp/*
        ├── IntelligentChatController.java        # /api/v1/chat/*
        ├── AuthController.java                   # /api/v1/auth/*
        ├── AiSoftwareEngineerController.java     # /api/v1/engineer/*
        ├── EnterpriseKnowledgeBaseController.java# /api/v1/knowledge/*
        ├── EnterpriseSubscriptionController.java # /api/v1/billing/*
        ├── EnterpriseVoiceController.java        # /api/v1/voice/*
        ├── EnterpriseMediaController.java        # /api/v1/media/*
        └── EnterpriseWorkspaceController.java    # /api/v1/workspaces/*
```
