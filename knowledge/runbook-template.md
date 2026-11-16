# Operational Runbook Template

## Runbook Identifier
RUNBOOK-[CATEGORY]-[NUMBER]: [Brief Operational Procedure Name]

## Severity & Alert Conditions
- **Severity**: [P1_CRITICAL | P2_HIGH | P3_MEDIUM | P4_LOW]
- **Trigger**: [Prometheus Alert / Error Rate Breach / Pod CrashLoop]

## Triage & Diagnosis
1. Step 1: Check pod status: `kubectl get pods -n oenexa-prod`
2. Step 2: Tail logs: `kubectl logs -f deployment/[service-name] -n oenexa-prod`
3. Step 3: Check memory / CPU metrics: `kubectl top pods -n oenexa-prod`

## Remediation Steps
1. Immediate Mitigation: ...
2. Canary Rollback: ...
3. Post-Incident Escalation: ...
