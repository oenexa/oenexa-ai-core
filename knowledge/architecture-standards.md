# 🏛️ OENEXA Enterprise Architecture Standards

## 1. Clean Hexagonal Monolith
- All backend repositories in the OENEXA ecosystem are structured as a Clean Hexagonal Modular Monolith (`domain`, `application`, `infrastructure`, `api`).
- **Domain Layer (`:domain`)**: Must remain 100% pure Java 25. Absolutely NO Spring or framework annotations allowed (`@Component`, `@Service`, `@Entity`, etc.).
- **Application Layer (`:application`)**: Orchestrates domain workflows, validators, services, and cognitive pipelines.
- **Infrastructure Layer (`:infrastructure`)**: Houses secondary adapters, JPA entities, database connections, Redis, Kafka, and external LLM client connectors.
- **API Layer (`:api`)**: Primary REST, SSE, and GraphQL adapters wrapping responses in `ApiResponse<T>` envelopes.

## 2. Invariants & Guardrails
- **SRP Line Limit**: Every file must remain strictly under 400 lines (<385 lines). Break down large classes into cohesive smaller components.
- **Zero-Mock Testing**: Synthetic Mockito mocks (`mock()`, `@Mock`, `@InjectMocks`) are strictly forbidden in unit tests. Instantiate real domain records and in-memory state.
- **Virtual Threads**: Java 25 Virtual Threads must be utilized for high-throughput, non-blocking I/O.
