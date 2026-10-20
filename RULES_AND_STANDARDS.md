# OENEXA Developer & AI Agent Constitution: Rules, Standards & Architecture Log

> **MANDATE:** Strict compliance is required for all AI agents and software engineers contributing to `oenexa-ai-core` and `oenexa-ai-web`.
> **Component:** `[AI-CORE] ⚡` OENEXA AI Core Backend  
> **Repository:** `C:\workspace\oenexarepo\oenexa-ai-core`  
> **Architecture:** Clean Hexagonal Modular Monolith (`domain`, `application`, `infrastructure`, `api`)  
> **Target Runtime:** Java 25+ | Spring Boot 4.1.1 | Spring AI 2.0.1 | PostgreSQL 15 | Redis 7 | MCP v1.0  

---

## 1. Core Engineering Rules & Invariants

### 1.0 Inviolable AI Agent & Developer Law: Symbolic Step-by-Step Ledger & 100% Coverage
- **Rule:** For **each and every development step**, every AI agent and software engineer MUST record what was done, why it was implemented, and how it was structured in concise, symbolic notation in `DEVELOPMENT_NOTES.md`.
- **Coverage Target:** All production code (domain models, application services, controllers) must maintain **100% real test coverage** (Zero-Mock policy).
- **Symbolic Notation Standard:**
  - `[SYMBOL] TAG | TARGET`
  - `WHAT:` Exact feature, refactor, or architectural component built.
  - `WHY:` Precise technical, functional, or business rationale for writing the code.
  - `HOW:` Exact design patterns, algorithms, Java 25 idioms, and verification steps applied.
  - `VERIFICATION:` Concrete verification metrics and execution status.
- **Approved Symbolic Tags:**
  - `[AI-CORE] ⚡` Core Platform Engine & Orchestration
  - `[V4-ARCH] 🏛️` V4 Clean Hexagonal Monolith Architecture
  - `[AGENT] 🤖` 10-Agent Autonomous DAG & ReAct Ecosystem
  - `[MEMORY] 🧠` 7-Layer Memory Hierarchy & NVMe SSD Context Buffer
  - `[MCP] 🔌` Model Context Protocol (MCP v1.0) Server & Tools
  - `[BILLING] 💳` Enterprise Subscriptions, Quotas & Token Economics
  - `[VOICE] 🎙️` Real-Time Speech AI, Diarization & TTS Synthesis
  - `[MEDIA] 🎨` Multimodal Vision, Diagram OCR & Image Generation
  - `[CORE] 🧱` Relational Tenancy Kernel & Domain Foundations
  - `[CODE] 💻` Code Generation, AST Analysis & Atomic Diffs
  - `[TEST] 🧪` Deterministic Test Automation & Verification
  - `[REVIEW] 🔍` Automated Code Review & Blast Radius Analysis
  - `[SEC] 🛡️` Security, Argon2id, JWT & SAST Vulnerability Scanning
  - `[DOC] 📚` Architecture Specs & Mermaid Diagram Synthesis
  - `[DEVOPS] 🐳` Docker, Kubernetes & CI/CD Pipelines
  - `[WORKSPACE] 📁` Multi-Tenant Project Workspaces
  - `[ADMIN] ⚙️` System Administration & Governance
  - `[REPO] 📦` Polyrepo Standardization & Version Control

### 1.1 Strictly NO Mock Tests (100% Real Verification)
- **Rule:** Do NOT use Mockito, PowerMock, `@MockBean`, or dummy test doubles in unit or service tests.
- **Why:** Mocked tests verify assumptions rather than reality, obscuring integration bugs, nullability mistakes, and type coercion failures.
- **How:** Test real instantiated classes, state transitions, domain records, and boundary invariants directly (`UserTest`, `UserServiceTest`, `AutonomousAgentEcosystemTest`).

### 1.2 Single Responsibility Principle (SRP < 400 Lines Limit)
- **Rule:** Every Java source file must remain strictly under **400 lines** of clean, readable code.
- **Why:** Large classes accumulate technical debt, violate SRP, obscure architectural boundaries, and degrade developer maintainability.
- **How:** Decompose complex orchestrators into focused domain value objects, utility functions, and cohesive collaborator services.

### 1.3 Immutability & Modern Java Records First (Zero Boilerplate)
- **Rule:** Never write mutable POJOs or manual getters/setters when a Java `record` or immutable pattern applies.
- **Why:** Eliminates boilerplate, guarantees thread safety across virtual threads, and provides compact validation in canonical constructors.
- **How:** 
  - Domain value objects & DTOs $\rightarrow$ Java `record` (`AutonomousAgentEcosystemDomain.AgentGoal`).
  - Entities $\rightarrow$ Immutable-first fields, compact accessors (`user.username()`, `user.email()`), and explicit transition methods.

### 1.4 Zero Deprecations & Zero Compiler Warnings
- **Rule:** Production code and build scripts must compile with zero deprecation warnings and zero compiler warnings.
- **Why:** Deprecated APIs create technical debt, security vulnerabilities, and migration blockers on modern JDKs.
- **How:** Enforced via `JavaCompile` options (`-parameters`, UTF-8, strict type-checking).

### 1.5 Code Coverage & Verification Gate (JaCoCo 80% Gate)
- **Rule:** All production logic (domain models, application services, controllers) must achieve a minimum of 80% line and branch coverage measured via JaCoCo.
- **Enforcement:** Enforced in Gradle `jacocoTestCoverageVerification` rule.
- **Verification:** Verified via `.\gradlew.bat test` or `./run.sh test`.

### 1.6 POSIX Script & LF Line Ending Hygiene
- **Rule:** All shell scripts (`*.sh`) and Gradle wrappers (`gradlew`) must use strict POSIX LF line endings, never Windows CRLF.
- **Why:** Windows CRLF in shell scripts causes cryptic `\r: command not found` errors in Docker containers and Linux environments.
- **How:** Enforced globally in `.gitattributes` (`*.sh text eol=lf`, `gradlew text eol=lf`).

### 1.7 Docker Container Networking & Healthcheck Standards
- **Rule:** Nginx web containers must bind to both IPv4 and IPv6 (`listen 80; listen [::]:80;`). Docker healthchecks must target explicit IP addresses (`http://127.0.0.1:80/` or `/actuator/health/readiness`).
- **Why:** Alpine Linux resolves `localhost` to `::1` before `127.0.0.1`, which causes healthcheck failures if IPv6 is not explicitly bound.

---

## 2. Technical Stack & Standards Matrix

| Capability                | Backend Standard (`oenexa-ai-core`)                                  | Frontend Standard (`oenexa-ai-web`)                           |
|:--------------------------|:---------------------------------------------------------------------|:--------------------------------------------------------------|
| **Language**              | **Java 25+** (bytecode target 69, pure Java, virtual threads)       | **TypeScript** (strict mode)                                  |
| **Framework**             | **Spring Boot 4.1.1** (Latest GA)                                    | **React 18** (Vite 5)                                         |
| **AI Layer**              | **Spring AI 2.0.1**, 10-Agent Autonomous DAG, ReAct Reflection        | Monaco AI Overlay, Telemetry Modals                           |
| **Protocol**              | **Model Context Protocol (MCP v1.0)** (`/api/v4/mcp/*`)              | MCP JSON-RPC 2.0 Telemetry Explorer                           |
| **Memory Architecture**   | **7-Layer Memory Hierarchy** (L1-L7, NVMe SSD `MappedByteBuffer`)    | 7-Layer Memory Visualizer                                     |
| **Dependency Management** | **Gradle Version Catalog** (`gradle/libs.versions.toml`)             | **npm / Vite**                                                |
| **Build Tool**            | **Gradle 9.8.0** (with parallel compile & config cache)              | **Vite 5**                                                    |
| **API Architecture**      | **Spring MVC REST** (`@RestController`, Virtual Threads, NO WebFlux) | **Vite Proxy** (`/api` $\rightarrow$ `http://localhost:8080`) |
| **Testing (TDD & BDD)**   | **Pure JUnit 5 Platform (Zero Mocks)**                               | **Vitest + React Testing Library (Zero Mocks)**               |
| **Coverage Tool**         | **JaCoCo 0.8.13** ($\ge 80\%$ line & branch gate)                    | **Vitest Runner**                                             |
| **Security**              | Argon2id, RFC 7519 JJWT 0.12.6, RBAC, OWASP Top-10                   | Strict CSP, XSS-safe React rendering                          |
| **Quality Gate**          | **0 Warnings, 100% Tests passing, SRP < 400 lines**                 | **0 Warnings, 100% Tests passing**                            |

---

## 3. Verified Verification Logs

### Backend Zero-Mock Test Suite Verification
- **Command:** `.\gradlew.bat test`
- **Output:** `BUILD SUCCESSFUL in 9s` (22 actionable tasks executed up-to-date, 0 failures).
- **Zero-Mock Status:** 100% of tests verify real instantiated domain records, services, and memory buffers without Mockito.

### Frontend Vitest & Vite Build Verification
- **Command:** `npm test && npm run build`
- **Output:** 
  - `vitest run`: **100% passing** (10/10 tests passed in 3.18s).
  - `vite build`: **Built cleanly in 8.04s** (`dist/` ready for production).
