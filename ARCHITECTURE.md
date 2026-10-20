# OENEXA AI Core — System Architecture & Component Blueprint

> **System Identity:** `[AI-CORE] ⚡` OENEXA AI Core Backend Service  
> **Paired Frontend UI:** `[AI-WEB] 💻` [`oenexa-ai-web`](../oenexa-ai-web) (`http://localhost:3000` via Docker / `http://localhost:5173` via Vite)  
> **Repository:** `https://github.com/oenexa/oenexa-ai-core`  
> **Runtime Baseline:** Java 25+ (Virtual Threads) | Spring Boot 4.1.1 | Spring AI 2.0.1 | PostgreSQL 15 | Redis 7 | Gradle 9.8.0  
> **Architecture Pattern:** Clean Hexagonal Modular Monolith (`domain`, `application`, `infrastructure`, `api`)  
> **Master Ecosystem Directory:** [`AI_AGENT_DEVELOPER_REPO_MAPPING.md`](../AI_AGENT_DEVELOPER_REPO_MAPPING.md)  

---

## 1. High-Level Architectural Overview

OENEXA AI Core is the enterprise-grade AI Operating System and autonomous software engineering platform. It rivals systems such as Claude, ChatGPT, Gemini, Microsoft Copilot, and DeepSeek by combining deep Chain-of-Thought reasoning, a 10-Agent Autonomous DAG execution engine, a 7-Layer Memory Hierarchy with NVMe context caching, Model Context Protocol (MCP v1.0) interoperability, multimodal intelligence (Voice STT/TTS and Vision OCR/SVG), and banking-grade security.

The platform strictly adheres to a **Clean Hexagonal Architecture**:
1. **Core Business Logic (Inner Hexagon - `domain`):** Pure, framework-agnostic Java 25 domain objects, records, value objects, domain events, and boundary contracts.
2. **Application Orchestration Layer (`application`):** Autonomous agent ecosystem, memory hierarchy management, MCP protocol handlers, use-case workflows, and multi-model router orchestration.
3. **Infrastructure Layer (Outer Hexagon - `infrastructure`):** PostgreSQL JPA entities (Flyway migrations), zero-copy NVMe SSD memory-mapped buffer adapters (`MappedByteBuffer`), Redis context cache, and distributed/in-process event publishers.
4. **Primary Adapters (`api`):** Spring Boot 4.1.1 executable bootJar (`com.oenexa.api.OenexaApiApplication`) with Virtual Threads, REST controllers (`/api/v4/*`, `/api/v1/*`), and OpenAPI documentation.

---

## 2. System Context, Container & Hexagonal Architecture (C4 Model)

### 2.1 System Context Diagram (Level 1)

```mermaid
C4Context
    title System Context Diagram (C4 Level 1) - OENEXA AI Core Platform

    Person(developer, "Developer / Enterprise User", "Interacts with AI Chat, Agent Dashboard, Monaco Editor, Memory Telemetry")
    Person_Ext(cicd_runner, "CI/CD & MCP Client", "Interacts via Model Context Protocol (MCP v1.0) and REST API")

    Enterprise_Boundary(b_oenexa, "OENEXA AI Ecosystem Boundary") {
        System(oenexa_web, "OENEXA AI Web", "React 18 + Vite 5 SPA, Monaco Editor, 10-Agent DAG Inspector, Memory Telemetry (Port 3000/5173)")
        System(oenexa_core, "OENEXA AI Core", "Spring Boot 4.1.1 + Java 25 Clean Hexagonal Architecture (Port 8080)")
    }

    System_Ext(gemini_api, "Google Gemini & Cloud LLMs", "Claude 3.5, Gemini 2.5 Pro, GPT-4o, DeepSeek V3 for reasoning and generation")
    System_Ext(local_llm, "Local Ollama Engine", "Local-first air-gapped models (llama3.3, qwen2.5) via http://localhost:11434")
    System_Ext(postgres_db, "PostgreSQL 15 Store", "Persistent relational store for organizations, projects, memory, vector embeddings (Port 5432)")
    System_Ext(redis_cache, "Redis 7 Cache", "In-memory cache for sliding context windows, agent mutex locks (Port 6379)")
    System_Ext(mcp_clients, "External MCP Tools", "External tools and data sources integrated via MCP v1.0 JSON-RPC 2.0")

    Rel(developer, oenexa_web, "Uses via web browser", "HTTPS / WSS")
    Rel(cicd_runner, oenexa_core, "Invokes via MCP & REST", "JSON-RPC / REST (:8080)")
    Rel(oenexa_web, oenexa_core, "Proxies REST calls, chat SSE streams, MCP tools", "REST / SSE (:3000/api -> :8080)")
    
    Rel(oenexa_core, gemini_api, "Dispatches prompt chains & reasoning", "HTTPS / REST")
    Rel(oenexa_core, local_llm, "Executes air-gapped inference", "HTTP (:11434)")
    Rel(oenexa_core, postgres_db, "Reads & writes tenant records, workflows, memory", "JDBC / JPA (:5432)")
    Rel(oenexa_core, redis_cache, "Caches conversational turns & distributed locks", "RESP (:6379)")
    Rel(oenexa_core, mcp_clients, "Exchanges tool schemas and execution results", "MCP v1.0 Protocol")
```

### 2.2 Container Diagram (Level 2)

```mermaid
flowchart TD
    subgraph ClientZone ["Developer / Client Zone"]
        Browser["Web Browser / Client<br/>(Monaco Editor, Chat UI, 10-Agent DAG)"]
    end

    subgraph DockerBridge ["Docker Bridge Network (oenexa-network)"]
        subgraph FrontContainer ["Container: oenexa-frontend"]
            Nginx["Nginx 1.25 Alpine Reverse Proxy<br/>Port 80 / [::]:80 -> External 3000"]
            SPA["React 18 + Vite 5 Static Assets<br/>Node 22 Multi-stage Build"]
            Nginx --> SPA
        end

        subgraph BackContainer ["Container: oenexa-backend"]
            direction TB
            JVM["Eclipse Temurin 25 JRE<br/>Virtual Threads + Generational ZGC"]
            
            subgraph CoreRunner ["Executable Runtime"]
                ApiJar[":api bootJar<br/>(com.oenexa.api.OenexaApiApplication)"]
            end
            
            JVM --- CoreRunner
            Probes["Actuator Health Probes<br/>/actuator/health/liveness<br/>/actuator/health/readiness"]
        end

        subgraph DataContainers ["Data & Persistence Tier"]
            Postgres["Container: oenexa-postgres<br/>PostgreSQL 15 Alpine (Port 5432)<br/>DB: oenexa_db | Flyway V1-V6"]
            Redis["Container: oenexa-redis<br/>Redis 7.4 Alpine (Port 6379)<br/>TTL: 3600s Sliding Cache"]
            NvmeCache["NVMe SSD File Cache<br/>MappedByteBuffer Zero-Copy mmap"]
            Volumes["Persistent Named Volumes<br/>oenexa_pgdata | oenexa_redisdata"]
            Postgres --- Volumes
            Redis --- Volumes
        end
    end

    subgraph ExternalServices ["AI Model Providers & LLMs"]
        CloudLLMs["Cloud LLM Providers<br/>Claude 3.5 | Gemini 2.5 | GPT-4o | DeepSeek"]
        LocalOllama["Local Ollama<br/>llama3.3 | qwen2.5 | deepseek-r1"]
    end

    Browser -->|http://localhost:3000| Nginx
    Nginx -->|proxy_pass /api/*| BackContainer
    BackContainer -->|jdbc:postgresql://postgres:5432/oenexa_db| Postgres
    BackContainer -->|redis://redis:6379| Redis
    BackContainer -->|NIO FileChannel.map| NvmeCache
    BackContainer -->|HTTPS REST| CloudLLMs
    BackContainer -->|HTTP :11434| LocalOllama
```

### 2.3 Hexagonal Component Architecture (Level 3)

```mermaid
flowchart TB
    subgraph Ring4 ["Ring 4: Inbound REST Controllers (Primary Adapters)"]
        direction TB
        AgentCtrl["AutonomousAgentEcosystemController (/api/v4/agents/*)"]
        MemoryCtrl["EnterpriseMemoryHierarchyController (/api/v4/memory/*)"]
        McpCtrl["ModelContextProtocolController (/api/v4/mcp/*)"]
        ChatCtrl["IntelligentChatController (/api/v1/chat/*)"]
        BillingCtrl["EnterpriseSubscriptionController (/api/v1/billing/*)"]
        VoiceCtrl["EnterpriseVoiceController (/api/v1/voice/*)"]
        MediaCtrl["EnterpriseMediaController (/api/v1/media/*)"]
        EngineerCtrl["AiSoftwareEngineerController (/api/v1/engineer/*)"]
    end

    subgraph Ring3 ["Ring 3: Outer Persistence & System Adapters (Secondary Adapters)"]
        direction TB
        JPA["Spring Data JPA Repositories (PostgreSQL 15)"]
        Flyway["Flyway V1..V6 Database Migrations"]
        MmapAdapter["NIO MappedByteBuffer NVMe Context Cache"]
        RedisAdapter["Redis Client Adapter"]
    end

    subgraph Ring2 ["Ring 2: Application Orchestration Services"]
        direction TB
        AgentSvc["EnterpriseAgentEcosystemService (10 Personas, DAG, Reflection)"]
        MemorySvc["EnterpriseMemoryHierarchyService (7-Layer Memory Hierarchy)"]
        McpSvc["ModelContextProtocolService (MCP v1.0 Runtime)"]
        ChatSvc["IntelligentConversationService (Gemini CoT & Multi-Model)"]
        BillingSvc["EnterpriseSubscriptionService (Stripe Tiers & Quotas)"]
        VoiceSvc["EnterpriseVoiceService (Real-Time Audio STT/TTS)"]
        MediaSvc["EnterpriseMediaService (Multimodal Vision OCR & SVG)"]
        EngineerSvc["AiSoftwareEngineerService (AST Parsing & Code Generation)"]
    end

    subgraph Ring1 ["Ring 1: Inner Domain Core (Pure Java 25)"]
        direction TB
        AgentDomain["AutonomousAgentEcosystemDomain (Records, Roles, Invariants)"]
        MemoryDomain["EnterpriseMemoryHierarchyDomain (L1..L7 Layer Contracts)"]
        McpDomain["ModelContextProtocolDomain (MCP v1.0 Protocol Schemas)"]
        ChatDomain["ChatDomain (Messages, Sessions, Contexts)"]
        SubDomain["SubscriptionDomain (Tiers, Quotas, Usage Meters)"]
        VoiceDomain["VoiceDomain (Transcripts, Voice Commands, Audio)"]
        MediaDomain["MediaDomain (OCR Extracts, SVG Scenes, Video)"]
        EngineerDomain["AiEngineerDomain (AST Models, Patches, Diff)"]
    end

    %% Dependency flow follows the Dependency Inversion Principle (strictly inward)
    Ring4 --> Ring2
    Ring4 --> Ring1
    Ring3 --> Ring1
    Ring2 --> Ring1
    Ring2 --> Ring3
```

---

## 3. The 4 Clean Hexagonal Modules

The backend is organized into 4 strictly bounded Gradle modules:

| Module | Architectural Role | Responsibilities & Capabilities | Test Gate |
|:---|:---|:---|:---|
| **`:domain`** | Inner Hexagon | Pure Java 25 business models, immutable records, sealed interfaces, and domain events. Zero external framework dependencies. Contains 10-Agent persona contracts, 7-Layer memory hierarchy definitions, MCP v1.0 protocol schemas, multimodal voice/media contracts, and subscription quota rules. | **100% Zero-Mock Verification (100% Lines & Branches)** |
| **`:application`** | Use-Cases & Orchestration | Orchestration services: `EnterpriseAgentEcosystemService` (10-agent DAG scheduler), `EnterpriseMemoryHierarchyService` (7 memory layers with NVMe SSD context cache), `ModelContextProtocolService` (MCP tool execution), `IntelligentConversationService`, `EnterpriseSubscriptionService`, `EnterpriseVoiceService`, `EnterpriseMediaService`. | **100% Zero-Mock Verification (100% Lines & Branches)** |
| **`:infrastructure`** | Outer Adapters | Spring Data JPA entities, Flyway database migrations (`V1` to `V6`), zero-copy `MappedByteBuffer` file cache for out-of-RAM context buffer, Redis adapters, and event publishers. | **100% Zero-Mock Verification (100% Lines & Branches)** |
| **`:api`** | Primary Inbound Adapter | Spring Boot 4.1.1 executable application (`OenexaApiApplication`) with Virtual Threads. Exposes REST controllers for all V4 endpoints (`/api/v4/*`), V1 endpoints (`/api/v1/*`), global RFC 7807 problem details error handling, and Actuator probes. | **100% Zero-Mock Verification (100% Lines & Branches)** |

---

## 4. 10-Agent Autonomous Ecosystem

The V4 agent system provides 10 specialized personas that collaborate via a Directed Acyclic Graph (DAG) with automated self-healing reflection loops:

```mermaid
graph TD
    UserGoal["User Goal / Prompt Input"] --> PM["1. Product Manager Agent<br/>(PRD, INVEST Stories, Acceptance Criteria)"]
    PM --> Arch["2. System Architect Agent<br/>(C4 Models, Modular Boundaries, ADRs)"]
    Arch --> Eng["3. Software Engineer Agent<br/>(AST Synthesis, Clean Code, Virtual Threads)"]
    Arch --> Data["7. Data Engineer Agent<br/>(Flyway Migrations, ER Schemas, Vector RAG)"]
    Eng --> QA["8. QA & Verification Agent<br/>(JUnit 5, Zero-Mock Tests, Coverage Gates)"]
    Data --> QA
    Eng --> Sec["4. Security Specialist Agent<br/>(OWASP Scans, Secrets Detection, RBAC)"]
    Eng --> DevOps["5. DevOps Specialist Agent<br/>(Docker, Kubernetes Helm, CI/CD Workflows)"]
    Sec --> DevOps
    DevOps --> QA
    QA --> Fin["9. Financial Analyst Agent<br/>(Cloud Compute Cost, Token Quotas, ROI)"]
    Fin --> CS["10. Customer Support Agent<br/>(User Guidance, Troubleshooting, Release Notes)"]
    Arch --> Res["6. Research Specialist Agent<br/>(Literature, Benchmarks, Competitive Intelligence)"]

    %% Self-Healing Reflection Loop
    QA -.->|Verification Failure| SelfHealing["Self-Healing Reflection Loop<br/>(Root-Cause Diagnosis, Unified Patch, Re-test)"]
    SelfHealing -.->|Apply Patch| Eng
```

### Persona Matrix
1. **Software Engineer (`SOFTWARE_ENGINEER`)**: Clean code generation, AST refactoring, zero compiler warnings.
2. **System Architect (`SYSTEM_ARCHITECT`)**: Hexagonal boundary design, C4 architecture diagrams, ADR generation.
3. **DevOps Specialist (`DEVOPS_SPECIALIST`)**: Multi-stage Dockerfiles, Helm charts, GitHub Actions CI/CD workflows.
4. **Security Specialist (`SECURITY_SPECIALIST`)**: SAST vulnerability scanning, secret detection, OWASP Top-10 compliance.
5. **Research Specialist (`RESEARCH_SPECIALIST`)**: Algorithmic benchmarking, academic literature analysis, competitive review.
6. **Product Manager (`PRODUCT_MANAGER`)**: PRD synthesis, INVEST user stories, Gherkin BDD criteria.
7. **Data Engineer (`DATA_ENGINEER`)**: PostgreSQL DDL, Flyway migrations, vector embedding pipelines.
8. **QA & Verification (`QA_VERIFICATION`)**: 100% zero-mock testing, mutation coverage, regression verification.
9. **Financial Analyst (`FINANCIAL_ANALYST`)**: Cloud compute cost estimation, LLM token budget optimization, subscription margins.
10. **Customer Support (`CUSTOMER_SUPPORT`)**: Interactive user troubleshooting, API documentation, release notes.

---

## 5. 7-Layer Memory Hierarchy & NVMe SSD Context Cache

```mermaid
graph TD
    subgraph MemoryHierarchy ["7-Layer Enterprise Memory Hierarchy"]
        L1["L1: Register / Virtual Thread Context<br/>(In-flight turn variables, ScopedValues, sub-millisecond)"]
        L2["L2: Conversational Cache<br/>(Sliding window of recent turns, Redis in-memory)"]
        L3["L3: Working Memory Scratchpad<br/>(Active goals, sub-task DAG state, reflection iterations)"]
        L4["L4: Fast NVMe SSD Context Buffer<br/>(MappedByteBuffer zero-copy out-of-RAM cache, up to 100MB)"]
        L5["L5: Relational Persistence<br/>(PostgreSQL 15 Flyway JPA tables, permanent user/tenant state)"]
        L6["L6: Semantic Vector Space<br/>(pgvector embeddings, cosine similarity, AST code knowledge)"]
        L7["L7: Cold Storage Archive<br/>(Compressed gzip JSON logs, immutable audit trail)"]
    end

    L1 --> L2 --> L3 --> L4 --> L5 --> L6 --> L7
```

### NVMe SSD Zero-Copy Memory Mapping
- **Mechanism:** Implemented in `EnterpriseMemoryHierarchyService` via Java NIO `FileChannel.map(FileChannel.MapMode.READ_WRITE, 0, capacity)`.
- **Purpose:** Allows large codebase context buffers (up to 100 MB per workspace) to be paged directly between NVMe SSD storage and the operating system page cache without JVM heap memory pressure or garbage collection pauses.

---

## 6. Model Context Protocol (MCP v1.0) Architecture

OENEXA AI Core acts as a compliant MCP v1.0 Server, enabling bi-directional tool calling, resource URI resolution, and prompt schema distribution:

- **Protocol Endpoints:**
  - `GET /api/v4/mcp/tools`: Lists all registered MCP tools with JSON schemas (`ast_analyzer`, `security_scanner`, `test_synthesizer`, `code_refactoring`, `architecture_diagram`, `voice_transcriber`, `svg_generator`).
  - `POST /api/v4/mcp/tools/execute`: Executes a tool invocation with structured JSON arguments.
  - `GET /api/v4/mcp/resources`: Discovers MCP workspace resources (`file://`, `workspace://`, `memory://`).
  - `GET /api/v4/mcp/prompts`: Retrieves standard prompt templates with parameter validation.
  - `POST /api/v4/mcp/rpc`: JSON-RPC 2.0 transport handler supporting `tools/list`, `tools/call`, `resources/list`, `resources/read`, and `prompts/list`.

---

## 7. Zero-Mock Quality & Verification Policy

1. **Strictly Real In-Process Verification**:
   - Zero Mockito, PowerMock, or mock test doubles allowed in test suites.
   - All tests instantiate real concrete records, domain services, and in-process databases.
2. **Coverage Gates**:
   - JaCoCo 80%+ mandatory line and branch coverage gate enforced on all builds.
3. **Single Responsibility Principle (SRP)**:
   - All source files across the platform are strictly kept below 400 lines of code.
