# OENEXA AI Core — Backend Platform

> **Component:** `[AI-CORE] ⚡` OENEXA AI Core Backend  
> **Repository:** `oenexa-ai-core`  
> **Framework:** Java 25+ | Spring Boot 4.1.1 | Spring AI 2.0.1 | Gradle 9.8.0  
> **Architecture:** Clean Hexagonal Architecture (Domain, Application, Infrastructure, API)  

---

## 🚀 Overview

OENEXA AI Core is the **enterprise-grade AI-powered operating system engine**. It provides a 10-Agent Autonomous System, a 7-Layer Memory Hierarchy with NVMe SSD context caching, Model Context Protocol (MCP v1.0) compliance, multimodal understanding, semantic code indexing, and banking-grade security.

## 🛠️ Technology Stack

| Layer              | Technologies                                                                                                  |
|--------------------|---------------------------------------------------------------------------------------------------------------|
| **Backend Core**   | Java 25 (Virtual Threads, Structured Concurrency), Spring Boot 4.1.1, Spring MVC, Spring WebFlux              |
| **AI Layer**       | Spring AI 2.0.1, 10-Agent Autonomous Ecosystem, ReAct Reflection, MCP v1.0 Protocol, 7-Layer Memory Hierarchy |
| **Persistence**    | PostgreSQL 15, Redis 7 Alpine, Spring Data JPA, Flyway, Zero-Copy MappedByteBuffer                            |
| **Testing**        | JUnit 5 Platform, Zero-Mock Policy (100% In-Process Real Verification), JaCoCo (80%+ Line/Branch Gates)       |
| **Infrastructure** | Docker, Docker Compose, Kubernetes, Helm (`charts/oenexa-ai-core`)                                            |
| **Architecture**   | Hexagonal / Clean Architecture (`domain`, `application`, `infrastructure`, `api`)                             |

---

## 📁 Modular Hexagonal Architecture

- **`domain`**: Pure enterprise domain models, records, value objects, and ports with zero framework coupling.
- **`application`**: Core business orchestrations, agent coordination, 7-layer memory, and MCP runtime.
- **`infrastructure`**: Database persistence entities, Spring Data repositories, and NIO memory cache adapters.
- **`api`**: Spring Boot application entrypoint and REST API controllers (`/api/*`).

---

## ⚡ Quick Start

### Windows (PowerShell)
```powershell
# Run backend application on port 8080
.\gradlew.bat :api:bootRun

# Execute full verification test suite (Zero-Mock Policy)
.\gradlew.bat test

# Build production executable bootJar
.\gradlew.bat :api:bootJar
```

### Linux & macOS / Git Bash
```bash
# Check status
./run.sh status

# Run API application
./run.sh api

# Run in development mode
./run.sh dev

# Execute full test suite
./run.sh test

# Build production bootJar
./run.sh build
```

---

## 🐳 Docker Stack

```powershell
# Start unified multi-container stack (Backend, Frontend, Postgres, Redis)
docker compose up -d --build

# View container logs
docker compose logs -f backend
```
