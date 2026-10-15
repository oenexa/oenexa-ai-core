# OENEXA Developer & AI Agent Constitution: Rules, Standards & Architecture Log

> **MANDATE:** Strict compliance is required for all AI agents and software engineers contributing to `oenexa-ai-engineer-backend` and `oenexa-ai-engineer-frontend`.

---

## 1. Core Engineering Rules & Invariants

### 1.0 Inviolable AI Agent & Developer Law: Symbolic Step-by-Step Ledger & 100% Coverage
- **Rule:** For **each and every development step**, every AI agent and software engineer MUST record what was done, why it was implemented, and how it was structured in a concise, symbolic notation in `DEVELOPMENT_NOTES.md`.
- **Coverage Target:** All production code (domain models, application services, controllers) must be upgraded toward **100% real test coverage** (Zero Mock policy).
- **Symbolic Notation Standard:**
  - `[SYMBOL] TAG | TARGET`
  - `WHAT:` Exact feature, refactor, or architectural component built.
  - `WHY:` Precise technical, functional, or business rationale for writing the code.
  - `HOW:` Exact design patterns, algorithms, Java 25 idioms, and verification steps applied.
- **Approved Symbolic Tags:**
  - `[CORE] 🧱` Core Platform / Domain Foundations
  - `[CODE] 💻` Code Generation & AST Engine
  - `[TEST] 🧪` Test Automation & Coverage Engine
  - `[REVIEW] 🔍` Automated Code Review & Quality Gates
  - `[SEC] 🛡️` Security & Secret Scanning Subsystems
  - `[DOC] 📚` Documentation & Diagram Generators
  - `[DEVOPS] 🐳` CI/CD & Container Orchestration
  - `[PIPE] ⚡` Continuous Pipeline & Execution Workflows
  - `[SEARCH] 🔎` Semantic Code & Artifact Search
  - `[WORKSPACE] 📁` Multi-Tenant Project Workspaces
  - `[ADMIN] ⚙️` System Administration & Governance
  - `[OBS] 📊` Observability, Metrics & Telemetry
  - `[AGENT] 🤖` Multi-Agent Orchestration & AI Logic

### 1.1 Strictly NO Mock Tests (100% Real Verification)
- **Rule:** Do NOT use Mockito, PowerMock, `@MockBean`, or dummy test doubles in unit or service tests.
- **Why:** Mocked tests verify assumptions rather than reality, obscuring integration bugs, nullability mistakes, and type coercion failures.
- **How:** Test real instantiated classes, state transitions, domain records, and boundary invariants directly (`UserTest`, `UserServiceTest`, `HealthControllerTest`).

### 1.2 Immutability & Modern Java Records First (Zero Boilerplate)
- **Rule:** Never write mutable POJOs or manual getters/setters when a Java `record` or immutable pattern applies.
- **Why:** Eliminates boilerplate, guarantees thread safety across virtual threads, and provides compact validation in constructors.
- **How:** 
  - Domain value objects & DTOs $\rightarrow$ Java `record` (`UserRegistration`).
  - Entities $\rightarrow$ Immutable-first fields, compact accessors (`user.username()`, `user.email()`), and explicit transition methods (`user.withEnabled(false)`).

### 1.3 Zero Deprecations & Zero Compiler Warnings
- **Rule:** Production code and build scripts must compile with zero deprecation warnings and zero compiler warnings.
- **Why:** Deprecated APIs create technical debt, security vulnerabilities, and migration blockers on modern JDKs.
- **How:** Enforced via `JavaCompile` options (`-parameters`, UTF-8, strict type-checking).

### 1.4 Code Coverage & Verification Gate (JaCoCo 80% Gate)
- **Rule:** All production logic (domain models, application services, controllers) must achieve a minimum of 80% line and branch coverage measured via JaCoCo (backend) and Vitest (frontend).
- **Enforcement:** Enforced in Gradle `jacocoTestCoverageVerification` rule:
  ```kotlin
  tasks.withType<JacocoCoverageVerification>().configureEach {
      violationRules { rule { limit { minimum = "0.80".toBigDecimal() } } }
  }
  ```
- **Verification:** Verified via:
  - Backend: `./run.sh test` or `.\gradlew.bat check`
  - Frontend: `npm test && npm run build`

### 1.6 Mandatory TDD & BDD Framework Integration
- **Rule:** All features must implement both Test-Driven Development (TDD) unit specifications and Behavior-Driven Development (BDD) user scenarios.
- **Backend BDD Standard:** **Cucumber-JVM 7.21.1** (`cucumber-java` + `cucumber-junit-platform-engine` + `@Suite`). Feature files written in declarative Gherkin (`.feature`) executing real services with zero mocks.
- **Backend TDD Standard:** Pure **JUnit 5 Platform** with 100% boundary state coverage.
- **Frontend BDD & TDD Standard:** **Vitest + React Testing Library** structured with explicit `Feature` and `Scenario (Given/When/Then)` specifications verifying real DOM and state events without mocks.

### 1.7 POSIX Script & LF Line Ending Hygiene
- **Rule:** All shell scripts (`*.sh`) and Gradle wrappers (`gradlew`) must use strict POSIX LF line endings, never Windows CRLF.
- **Why:** Windows CRLF in shell scripts causes cryptic `\r: command not found` errors in Docker containers and Linux environments.
- **How:** Enforced globally in `.gitattributes` (`*.sh text eol=lf`, `gradlew text eol=lf`).

### 1.8 Docker Container Networking & Healthcheck Standards
- **Rule:** Nginx web containers must bind to both IPv4 and IPv6 (`listen 80; listen [::]:80;`). Docker healthchecks must target explicit IP addresses (`http://127.0.0.1:80/` or `/actuator/health/readiness`).
- **Why:** Alpine Linux resolves `localhost` to `::1` before `127.0.0.1`, which causes healthcheck failures if IPv6 is not explicitly bound.

---

## 2. Technical Stack & Standards Matrix

| Capability                | Backend Standard                                                     | Frontend Standard                                             |
|:--------------------------|:---------------------------------------------------------------------|:--------------------------------------------------------------|
| **Language**              | **Java 25+** (bytecode target 69, pure Java, zero Kotlin in prod)    | **TypeScript** (strict mode)                                  |
| **Framework**             | **Spring Boot 4.1.1** (Latest GA)                                    | **React 18** (Vite 5)                                         |
| **AI Orchestration**      | **Spring AI 2.0.1** (Multi-Agent, RAG, Conversational Memory)         | Monaco AI Overlay                                             |
| **Dependency Management** | **Gradle Version Catalog** (`gradle/libs.versions.toml`)             | **npm / Vite**                                                |
| **Build Tool**            | **Gradle 9.8.0** (with parallel compile & config cache)              | **Vite 5**                                                    |
| **API Architecture**      | **Spring MVC REST** (`@RestController`, Virtual Threads, NO WebFlux) | **Vite Proxy** (`/api` $\rightarrow$ `http://localhost:8080`) |
| **Testing (TDD & BDD)**   | **Cucumber-JVM 7.21.1 (BDD) + JUnit 5 Platform (TDD)** (Zero mocks)  | **Vitest + React Testing Library (BDD/TDD)** (Real DOM)       |
| **Compiler Plugin**       | **Lombok 1.18.42** (centralized in Version Catalog)                  | TypeScript Compiler (`tsc`)                                   |
| **Coverage Tool**         | **JaCoCo 0.8.13** ($\ge 80\%$ line & branch gate)                    | **Vitest Runner**                                             |
| **Security**              | JJWT 0.12.6, Jakarta Validation, OWASP Top-10, Immutability          | Strict CSP, XSS-safe React rendering                          |
| **Quality Gate**          | **0 Warnings, $\ge 80\%$ Coverage, 0 Failures**                       | **0 Warnings, 100% Tests passing**                            |

---

## 3. Verified Verification Logs

### Backend JaCoCo Verification
- **Command:** `./gradlew clean test jacocoTestReport assemble`
- **Output:** `BUILD SUCCESSFUL in 8s` (24 actionable tasks executed)
- **Coverage Status:**
  - `com.oenexa.domain.User`: **100% Line Coverage** (21/21 lines, 0 missed)
  - `com.oenexa.domain.UserRegistration`: **100% Line & Branch Coverage** (8/8 lines, 12/12 branches, 0 missed)
  - `com.oenexa.application.UserService`: **100% Line Coverage** (6/6 lines, 0 missed)
  - `com.oenexa.api.HealthController`: **100% Line Coverage** (3/3 lines, 0 missed)

### Frontend Vitest & Vite Build Verification
- **Command:** `npm test && npm run build`
- **Output:** 
  - `vitest run`: **100% passing** (2/2 test suites passed in 173ms)
  - `vite build`: **Built cleanly in 2.0s** (`dist/` ready for production)
