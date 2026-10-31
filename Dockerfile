# ==============================================================================
# OENEXA AI Core - Multi-Stage Container Image
# Targets Java 27 & Spring Boot 4.1.1 on Eclipse Temurin
# Repository: github.com/oenexa/oenexa-ai-core
# ==============================================================================

# Stage 1: Build the application with Gradle wrapper and Java 27 JDK
FROM eclipse-temurin:27-jdk-noble AS builder
WORKDIR /workspace/app

# Copy wrapper and configuration first for layer caching
COPY gradlew ./
COPY gradle/ ./gradle/
COPY gradle.properties settings.gradle.kts build.gradle.kts ./

# Ensure gradlew has POSIX permissions
RUN chmod +x gradlew

# Copy source trees for all modules
COPY . ./

# Compile and build the API executable bootJar
RUN ./gradlew :api:bootJar -x test --no-daemon --parallel

# Stage 2: Minimal, secure runtime image
FROM eclipse-temurin:27-jre-noble AS runner
WORKDIR /app

# Install curl for health probes
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

# Security: run as non-root user
RUN groupadd -r oenexa && useradd -r -g oenexa -m -d /app oenexa

# Copy the generated bootJar from the builder stage
COPY --from=builder --chown=oenexa:oenexa /workspace/app/api/build/libs/api-0.1.0-SNAPSHOT.jar /app/app.jar

USER oenexa

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=docker \
    SERVER_PORT=8080 \
    JAVA_TOOL_OPTIONS="-XX:+UseZGC -Xmx1024m"

HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Dfile.encoding=UTF-8", "-jar", "/app/app.jar"]
