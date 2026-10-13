# OENEXA Developer & AI Agent Constitution: Rules, Standards & Architecture Log

> **MANDATE:** Strict compliance is required for all AI agents and software engineers contributing to `oenexa-ai-engineer-backend` and `oenexa-ai-engineer-frontend`.

---

## 1. Core Engineering Rules & Invariants

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

### 1.4 100% Code Coverage & Verification Gate
- **Rule:** All production logic (domain models, application services, controllers) must achieve 100% line and branch coverage measured via JaCoCo (backend) and Vitest (frontend).
- **How:** Verified via:
  - Backend: `./gradlew clean test jacocoTestReport assemble`
  - Frontend: `npm test && npm run build`

### 1.6 Mandatory TDD & BDD Framework Integration
- **Rule:** All features must implement both Test-Driven Development (TDD) unit specifications and Behavior-Driven Development (BDD) user scenarios.
- **Backend BDD Standard:** **Cucumber-JVM** (`cucumber-java` + `cucumber-junit-platform-engine` + `@Suite`). Feature files written in declarative Gherkin (`.feature`) executing real services with zero mocks.
- **Backend TDD Standard:** Pure **JUnit 5 Platform** with 100% boundary state coverage.
- **Frontend BDD & TDD Standard:** **Vitest + React Testing Library** structured with explicit `Feature` and `Scenario (Given/When/Then)` specifications verifying real DOM and state events without mocks.

---

## 2. Technical Stack & Standards Matrix

| Capability | Backend Standard | Frontend Standard |
|:---|:---|:---|
| **Language** | **Java 25+** (pure Java, zero Kotlin in prod) | **TypeScript** (strict mode) |
| **Framework** | **Spring Boot 4.1.1** (Latest GA) | **React 18** (Vite 5) |
| **Dependency Management** | **io.spring.dependency-management 1.1.7** | **npm / Vite** |
| **API Architecture** | **Spring MVC REST** (`@RestController`, Virtual Threads, NO WebFlux) | **Vite Proxy** (`/api` $\rightarrow$ `http://localhost:8080`) |
| **Testing (TDD & BDD)** | **Cucumber-JVM 7.18.1 (BDD) + JUnit 5 Platform (TDD)** (Zero mocks) | **Vitest + React Testing Library (BDD/TDD)** (Real DOM) |
| **Coverage Tool** | **JaCoCo** (100% line & branch coverage) | **Vitest Runner** |
| **Security** | Jakarta Validation, OWASP Top-10, Immutability | Strict CSP, XSS-safe React rendering |
| **Quality Gate** | **0 Warnings, 100% Coverage, 0 Failures** | **0 Warnings, 100% Tests passing** |

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
