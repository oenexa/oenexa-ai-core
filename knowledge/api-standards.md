# 🌐 OENEXA API Standards

## 1. REST Envelope Invariants
All REST responses must be enveloped in `ApiResponse<T>`:
```json
{
  "success": true,
  "status": 200,
  "message": "Operation successful",
  "data": { ... },
  "correlationId": "corr-uuid-v4",
  "timestamp": "2026-11-16T08:00:00Z"
}
```

## 2. Error Handling & Problem Details
- Standard HTTP status codes (200, 201, 400, 401, 403, 404, 409, 429, 500, 504).
- Application errors mapped to RFC 7807 / RFC 9457 Problem Details envelopes.
- No internal stack traces or database driver exceptions exposed to clients in production.

## 3. Versioning
- Semantic URL versioning: `/api/v4/...`.
