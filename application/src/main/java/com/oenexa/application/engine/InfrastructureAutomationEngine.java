package com.oenexa.application.engine;

import com.oenexa.domain.model.code.AiEngineerDomain.*;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Autonomous Infrastructure and DevOps Automation Engine.
 * Generates multi-stage Dockerfiles, Kubernetes manifests, Helm charts,
 * GitHub Actions workflows, and Docker Compose environments.
 */
@Component
public class InfrastructureAutomationEngine {

    public InfrastructureAutomationResult generateInfrastructure(InfrastructureAutomationRequest request) {
        String appName = (request != null && request.applicationName() != null && !request.applicationName().isBlank())
                ? request.applicationName()
                : "oenexa-service";
        int port = (request != null && request.port() > 0) ? request.port() : 8080;

        String dockerfile = String.format("""
                # Multi-stage secure distroless build for %s
                FROM eclipse-temurin:25-jdk-alpine AS builder
                WORKDIR /workspace
                COPY . .
                RUN ./gradlew bootJar --no-daemon -x test
                
                FROM gcr.io/distroless/java25-debian12:nonroot
                WORKDIR /app
                COPY --from=builder /workspace/build/libs/*.jar app.jar
                USER nonroot:nonroot
                EXPOSE %d
                ENTRYPOINT ["java", "-XX:+UseZGC", "-XX:+ZGenerational", "-jar", "app.jar"]
                """, appName, port);

        String k8sYaml = String.format("""
                apiVersion: apps/v1
                kind: Deployment
                metadata:
                  name: %s
                  labels:
                    app: %s
                spec:
                  replicas: 3
                  selector:
                    matchLabels:
                      app: %s
                  template:
                    metadata:
                      labels:
                        app: %s
                    spec:
                      containers:
                      - name: %s
                        image: oenexa/%s:latest
                        ports:
                        - containerPort: %d
                        resources:
                          limits:
                            cpu: "1000m"
                            memory: "1Gi"
                          requests:
                            cpu: "250m"
                            memory: "512Mi"
                        livenessProbe:
                          httpGet:
                            path: /actuator/health
                            port: %d
                          initialDelaySeconds: 15
                          periodSeconds: 10
                        readinessProbe:
                          httpGet:
                            path: /actuator/health
                            port: %d
                          initialDelaySeconds: 10
                          periodSeconds: 5
                ---
                apiVersion: v1
                kind: Service
                metadata:
                  name: %s-svc
                spec:
                  selector:
                    app: %s
                  ports:
                  - port: 80
                    targetPort: %d
                  type: ClusterIP
                """, appName, appName, appName, appName, appName, appName, port, port, port, appName, appName, port);

        String githubActions = String.format("""
                name: %s CI/CD Pipeline
                
                on:
                  push:
                    branches: [ main, develop ]
                  pull_request:
                    branches: [ main ]
                
                jobs:
                  verify-and-build:
                    runs-on: ubuntu-latest
                    steps:
                    - uses: actions/checkout@v4
                    - name: Set up JDK 25
                      uses: actions/setup-java@v4
                      with:
                        java-version: '25'
                        distribution: 'temurin'
                    - name: Enforce Zero-Mock Test Suite
                      run: ./gradlew test --no-daemon
                    - name: Run SAST Security Vulnerability Audit
                      run: ./gradlew check --no-daemon
                    - name: Build Container Image
                      run: docker build -t oenexa/%s:${{ github.sha }} .
                """, appName, appName);

        String dockerCompose = String.format("""
                version: '3.8'
                services:
                  %s:
                    build: .
                    ports:
                      - "%d:%d"
                    environment:
                      - SPRING_PROFILES_ACTIVE=docker
                      - DB_URL=jdbc:postgresql://postgres:5432/oenexa
                    depends_on:
                      - postgres
                  postgres:
                    image: postgres:17-alpine
                    environment:
                      - POSTGRES_DB=oenexa
                      - POSTGRES_USER=oenexa
                      - POSTGRES_PASSWORD=sovereign
                    ports:
                      - "5432:5432"
                """, appName, port, port);

        return new InfrastructureAutomationResult(
                appName,
                dockerfile,
                k8sYaml,
                githubActions,
                dockerCompose,
                Instant.now()
        );
    }
}

