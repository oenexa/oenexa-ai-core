# RB-001: Mitigating Service Degradation on oenexa-ai-core

## Symptoms
- Alert \High5xxRate\ or \HighLatency\ firing.
- Influx of HTTP 502/503 errors on the Ingress Gateway.

## Immediate Triage Steps
1. **Verify Pod Status:** \kubectl get pods -n oenexa-production -l app=oenexa-ai-core\
2. **Inspect Memory/CPU:** \kubectl top pods -n oenexa-production -l app=oenexa-ai-core\
3. **Check Downstream Connectivity:** Verify database connectivity and Redis cache response time.
4. **Trigger Pod Restart (if stuck):** \kubectl rollout restart deployment/oenexa-ai-core -n oenexa-production\
5. **Rollback (if post-deployment):** \rgocd app rollback oenexa-prod-oenexa-ai-core\
"@

# 7. GitOps K8s Manifests
Write-IfMissing "deploy/base/deployment.yaml" @"
apiVersion: apps/v1
kind: Deployment
metadata:
  name: oenexa-ai-core
  labels:
    app.kubernetes.io/name: oenexa-ai-core
    app.kubernetes.io/part-of: oenexa
spec:
  replicas: 2
  selector:
    matchLabels:
      app: oenexa-ai-core
  template:
    metadata:
      labels:
        app: oenexa-ai-core
    spec:
      containers:
        - name: oenexa-ai-core
          image: oenexa/
          ports:
            - containerPort: 8080
          readinessProbe:
            httpGet:
              path: /api/health/ready
              port: 8080
            initialDelaySeconds: 5
            periodSeconds: 10
          livenessProbe:
            httpGet:
              path: /api/health/live
              port: 8080
            initialDelaySeconds: 15
            periodSeconds: 20
          resources:
            requests:
              cpu: 100m
              memory: 128Mi
            limits:
              cpu: 500m
              memory: 512Mi