# ==========================
# 🏗️ Stage 1 — Build all modules
# ==========================
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /workspace

# Copy entire multi-module project (parent + submodules)
COPY ../../ ./

# Build everything (shared, transaction-api, retry-service, etc.)
RUN mvn -B -q clean package -DskipTests

# ==========================
# 🚀 Stage 2 — Runtime
# ==========================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy built JAR from the builder stage
COPY --from=builder /workspace/retry-service/target/*.jar app.jar

# Expose port for retry service
EXPOSE 8081

# Set environment (optional but clean)
ENV SPRING_PROFILES_ACTIVE=prod

# Run the service
ENTRYPOINT ["java","-jar","/app/app.jar"]
