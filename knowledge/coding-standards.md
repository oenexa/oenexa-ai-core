# 💻 OENEXA Coding Standards

## 1. Java 25 LTS Best Practices
- **Records**: Prefer immutable Java records for domain data transfer and state.
- **Pattern Matching**: Utilize Java 21+ switch pattern matching and destructuring over verbose `instanceof` casts.
- **Null Safety**: Validate all arguments defensively using `Objects.requireNonNull(arg, "arg must not be null")`. Never return raw null collections; return `List.of()`, `Set.of()`, or `Map.of()`.
- **Concurrency**: Use `StructuredTaskScope` and virtual threads (`Executors.newVirtualThreadPerTaskExecutor()`).

## 2. Formatting & Clean Code
- File length ceiling: $< 385$ lines.
- Method length ceiling: $< 50$ lines.
- High cohesion and single responsibility principle (SRP).
