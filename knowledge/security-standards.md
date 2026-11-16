# 🔒 OENEXA Security Standards

## 1. Zero-Trust Architecture
- Every internal and external request must be authenticated and authorized.
- Multi-tenant boundary isolation enforced at ingress via `X-Oenexa-Tenant-Id`.
- No implicit trust between microservices.

## 2. Cryptographic Invariants
- Post-Quantum Cryptography (PQC): State-level operations sign with NIST ML-DSA-65.
- Classical operations: RS256 with 4096-bit keys or Argon2id for password hashing.
- TLS 1.3 mandated across all ingress and egress boundaries.

## 3. OWASP Top 10 Mitigation
- Parameterized SQL / JPA queries only. Zero raw string concatenation in SQL queries.
- Mandatory input validation via Jakarta Bean Validation and explicit domain validators.
- Automatic rate-limiting and IP throttling via AI Gateway.
