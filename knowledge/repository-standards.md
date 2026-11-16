# 📁 OENEXA Repository Standards

## 1. Multi-Module Polyrepo Governance
- Every repository must have a clear Single Strategic Mandate.
- Frontend must never be colocated inside backend repository directories. `oenexa-ai-core` is backend-only; `oenexa-ai-web` is frontend-only.
- All repositories must maintain continuous synchronization between dual remotes:
  - `origin`: GitHub (`git@github.com:oenexa/...`)
  - `server`: Production VPS (`ubuntu@62.210.92.60:/opt/oenexa/repos/...`)

## 2. Git Invariants
- Direct commits to `main` must follow Conventional Commits format (`feat(...)`, `fix(...)`, `docs(...)`, `refactor(...)`).
- Branches must pass 100% green builds before merging.
- No secrets, certificates, or unencrypted `.env` files may ever be committed.
