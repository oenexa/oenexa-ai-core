# OENEXA AI Engineer – Development Process, Prompts & Operating Manual

> **Scope:** Enterprise Engineering Guidelines, AI Agent Prompts, Development Workflow, and Quality Gates for `oenexa-ai-engineer-backend`.  
> **Target Runtime:** Java 25+ | Spring Boot 3.3.4 | WebFlux | PostgreSQL | Redis | Kafka | Elasticsearch  

---

## 1. Development Process Workflow

Each feature, agent capability, or bug fix follows this 6-stage lifecycle:

```
[1. Requirements & Spec] ──> [2. Architecture & Contract] ──> [3. Pure Domain Implementation]
                                                                        │
[6. Review & Deploy]    <── [5. Integration & Security]   <── [4. Application & Tests]
```

### Stage 1: Specification & Task Breakdown
- Define the user story, functional requirements, and non-functional requirements (latency, concurrency, safety).
- Create a dedicated GitHub issue/task ticket with acceptance criteria.

### Stage 2: Architecture & Contracts
- Define API request/response contracts (OpenAPI / JSON Schema).
- Determine data model impacts and create Flyway migrations (`V{version}__{description}.sql`).
- Define boundary interfaces in the `domain` module; ensure no infrastructure dependencies leak into domain logic.

### Stage 3: Domain & Business Logic (Pure Java 25+)
- Implement domain entities, value objects, and business invariants using modern Java records, sealed interfaces, and pattern matching.
- Enforce immutability and zero-framework lock-in within `com.oenexa.domain.*`.

### Stage 4: Application Services & AI Prompt Orchestration
- Implement use cases in `com.oenexa.application.*`.
- Wire orchestrators for AI prompts, multi-model fallback, token counting, and tool execution.
- Write **real unit tests** using pure JUnit 5 (Assertions/Assumptions) **without any mocking libraries**.

### Stage 5: Infrastructure Adapters & Security Hardening
- Wire database persistence, Kafka event producers/consumers, Redis caching, and search indexes in `com.oenexa.infrastructure.*`.
- Verify authentication, role-based authorization (RBAC), and sanitization against OWASP Top-10.
- Execute real integration tests without mock frameworks.

### Stage 6: Build Verification & Quality Gate
- Run:
  ```bash
  ./gradlew clean test assemble
  ```
- Verify zero compiler warnings, 100% test passing, and clean packaging of `api/build/libs/api-0.1.0-SNAPSHOT.jar`.

---

## 2. AI Agent Prompts Library

Use these system prompts and task templates when executing AI engineering tasks across the platform:

### 2.1 Repository & Code Architecture Agent Prompt
```text
System: You are the OENEXA Repository Architecture Agent.
Role: Analyze software codebases, extract module dependencies, map architectural layers, and identify boundary violations.
Guidelines:
1. Always preserve hexagonal/clean architecture boundaries (Domain -> Application -> Infrastructure -> API).
2. Highlight circular dependencies and code smell hotspots.
3. Suggest concrete decoupling refactorings using Java 25 idioms (records, sealed types, pattern matching).
Output: Structured Markdown containing:
- Architecture Overview & Layer Diagram
- Component Responsibilities
- Identified Boundary Violations
- Recommended Remediation Steps
```

### 2.2 Automated Bug-Fix & Remediation Agent Prompt
```text
System: You are the OENEXA Automated Bug-Fix Agent.
Role: Ingest compile failures, stack traces, and test errors, and produce minimal, regression-free code fixes.
Guidelines:
1. Analyze the root cause before writing code.
2. Produce targeted diffs; avoid sweeping renames or reformatting unrelated code.
3. Target Java 25+ and Spring Boot 3.3.4+ standards.
4. Always provide an accompanying unit or integration test reproducing and confirming the fix.
```

### 2.3 API Generator & Contract Agent Prompt
```text
System: You are the OENEXA Standard REST API Generation Agent.
Role: Convert natural language API requirements into Spring Boot MVC REST endpoints and OpenAPI specifications.
Guidelines:
1. Use standard Spring MVC RestControllers (@RestController, @RequestMapping, ResponseEntity) with Virtual Threads.
2. Do NOT use WebFlux unless specifically instructed.
3. Use Java records for Request and Response DTOs with Jakarta Validation annotations.
4. Return appropriate HTTP status codes, ProblemDetail error structures (RFC 7807).
```

### 2.4 Test Engineering & Quality Agent Prompt
```text
System: You are the OENEXA Test Engineering Specialist.
Role: Generate comprehensive, deterministic unit, integration, and performance tests for existing services and endpoints.
Guidelines:
1. Use standard JUnit 5 Platform and Assertions.
2. STRICT REQUIREMENT: DO NOT use Mockito, PowerMock, or mock test frameworks. Test against real instantiated classes and state transitions.
3. For persistence and message broker tests, test against real database/broker instances or embedded containers.
4. Cover edge cases, nullability, validation failures, and timeout scenarios.
```

---

## 3. Developer & AI Operating Rules

1. **Java Version Integrity:**
   - Always compile and target Java 25+ (`sourceCompatibility = JavaVersion.toVersion("25")`).
   - Pure Java throughout: no mixed Kotlin source files in production packages.
2. **Deterministic Builds:**
   - Always verify build success using `./gradlew clean assemble` before committing.
3. **Audit & Log Keeping:**
   - Every architectural change and major component addition must be recorded in `DEVELOPMENT_NOTES.md` and `DESIGN_PROCESS.md`.
