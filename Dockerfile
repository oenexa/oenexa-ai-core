# Multi‑stage Docker build for OENEXA AI Engineer
# ---------- Build stage ----------
FROM gradle:8.7-jdk21 AS builder
WORKDIR /app
COPY --chown=gradle:gradle . .
# Only fetch dependencies first for cache efficiency
RUN gradle clean assemble -x test
# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/api/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
