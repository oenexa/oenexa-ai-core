# OENEXA AI Engineer - Development Notes & Architecture Log

> **Project:** OENEXA AI Engineer  
> **Backend Stack:** Java 25+ | Spring Boot 4.1.1 | Spring MVC REST (No WebFlux) | Spring Data JPA | Redis | Kafka | Elasticsearch | Flyway | Cucumber-JVM 7.18.1 | JUnit 5 Platform  
> **Frontend Stack:** React 18 | Vite 5 | TypeScript | Tailwind CSS | Material-UI 5 | Monaco Editor | Vitest  
> **Architecture Pattern:** Clean / Hexagonal Modular Monolith  

---

## 1. System Design & Architectural Decisions

### 1.1 Backend Modular Hierarchy (Hexagonal / Clean Architecture)
The backend is structured into 4 decoupled Java modules:

```
oenexa-ai-engineer-backend/
├── domain/            # Core business entities, value objects, domain logic (zero external framework lock-in)
├── application/       # Use-cases, application services, AI prompt workflows, orchestration
├── infrastructure/    # External adapters: PostgreSQL (JPA), Redis, Kafka, Elasticsearch, Flyway
└── api/               # Spring MVC REST endpoints, security, bootJar packaging (Standard MVC, NO WebFlux)
```

### 1.2 Technology Decisions
1. **Target Java Version: Java 25+**
   - Configured via `sourceCompatibility = JavaVersion.toVersion("25")` and `targetCompatibility = JavaVersion.toVersion("25")`.
   - Utilizes standard Java features (records, sealed classes, pattern matching, virtual threads).
   - Java 25 standard bytecode (major version 69) is compiled and verified cleanly.
2. **Spring Boot Version: Spring Boot 4.1.1 (with io.spring.dependency-management 1.1.7)**
   - Latest General Availability (GA) release, providing cutting-edge runtime performance, JDK 25+ bytecode optimization, and production-grade packaging.
3. **Rest API Framework: Spring MVC (Non-WebFlux by Design)**
   - Per requirements, **WebFlux has been removed**. The project uses **Spring MVC (`spring-boot-starter-web`)** with standard non-blocking virtual threads capabilities, simplifying debugging, exception handling, and JDBC/JPA compatibility.
4. **Testing Architecture: Real Tests (Strictly NO Mock Tests)**
   - **No mock frameworks (no Mockito / no PowerMock)** are used.
   - All tests execute against real instantiated domain models and services:
     - `UserTest` verifies real entity mutations, timestamps, and validation invariants.
     - `UserServiceTest` verifies actual use-case workflow executions.
   - Configured with `tasks.withType<Test> { useJUnitPlatform() }` and `junit-platform-launcher`.
5. **Decoupled Frontend Repository**
   - Frontend is completely separated in `oenexa-ai-engineer-frontend`, utilizing Vite 5, React 18, Tailwind CSS, and Monaco Editor with proxying to `/api/*`.

---

## 2. Step-by-Step Development & Resolution Log

| Step # | Module / Component     | Action Taken                                                                                    | Rationale & Outcome                                                                                                                          |
|:-------|:-----------------------|:------------------------------------------------------------------------------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------|
| **01** | Git / VCS              | Isolated frontend and backend directories into separate workspaces                              | Ensures single-responsibility repository layout.                                                                                             |
| **02** | Frontend UI            | Fixed Monaco Editor integration in `App.tsx`                                                    | Replaced nonexistent named export `MonacoEditor` with default import `Editor from '@monaco-editor/react'`. `npm run build` succeeds cleanly. |
| **03** | Backend Root           | Removed deprecated `:ui` reference in `settings.gradle.kts`                                     | Prevents Gradle multi-project resolution failure when UI lives in separate repo.                                                             |
| **04** | Backend Build          | Modernized build system to Spring Boot 3.3.4 & Gradle Java Library                              | Fixed `CopyProcessingSpec.getDirMode()` Gradle 9.6 incompatibility and resolved dependency scopes.                                           |
| **05** | Backend Domain         | Added `java-library` plugin and transitive `api("jakarta.persistence:jakarta.persistence-api")` | Eliminates compiler warnings in dependent modules using entity metadata.                                                                     |
| **06** | Backend Infrastructure | Stabilized `InfrastructureConfig.java`                                                          | Cleaned Spring Beans for PostgreSQL, Redis, Kafka, and Flyway with production-grade fallback parameters.                                     |
| **07** | Backend Compiler       | Configured explicit Java 25 source/target compatibility                                         | Successfully compiles and packages with **0 errors and 0 warnings** in 3 seconds.                                                            |
| **08** | Web Layer Refactoring  | Removed Spring WebFlux; migrated to Spring MVC (`spring-boot-starter-web`)                      | Follows enterprise REST best practices, removing reactive complexity where standard MVC with virtual threads is superior.                    |
| **09** | Testing Policy         | Enforced real tests; removed mock tests                                                         | Added JUnit 5 Platform tests without any mocks (`UserTest`, `UserServiceTest`).                                                              |
| **10** | Build & Test Gate      | Executed full `./gradlew clean test assemble`                                                   | **BUILD SUCCESSFUL in 4s** (19/19 actionable tasks passed).                                                                                  |
| **11** | Database Architecture  | Implemented Flyway `V1__init_oenexa_schema.sql`, 15 JPA entities, and zero-friction `local` profile | Resolved PostgreSQL host auth exception via embedded PostgreSQL-compatible H2 fallback with full Flyway seeding; full Docker/PostgreSQL config in `application-docker.yml`. |

---

## 3. Developer & AI Agent Guide

### Running Backend Locally
```bash
cd oenexa-ai-engineer-backend
./gradlew clean test assemble
java -jar api/build/libs/api-0.1.0-SNAPSHOT.jar
```

### Running Frontend Locally
```bash
cd oenexa-ai-engineer-frontend
npm install
npm run dev
```

### Security & Coding Standards
- **Zero Mock Policy:** Unit and service tests must test real logic and state transitions without mocking frameworks.
- **Immutability & Records:** Use Java records for DTOs and value objects.
- **Dependency Inversion:** Use-cases in `application` must depend only on abstractions defined in `domain`, never directly on database drivers or third-party SDKs.
- **Secrets Management:** Pass sensitive values (passwords, tokens, API keys) via environment variables or secret managers, never hardcoded in source.
