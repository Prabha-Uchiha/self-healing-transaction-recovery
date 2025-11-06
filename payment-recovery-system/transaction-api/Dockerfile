# ==========================
# 🏗️ Stage 1 — Build all modules
# ==========================
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /workspace

# Copy entire multi-module project (parent + submodules)
COPY ../../ ./

# Build everything (shared, transaction-api, retry-service, etc.)
#RUN mvn -B clean package -DskipTests -pl transaction-api -am
RUN mvn -B -q clean package -DskipTests


# ==========================
# 🚀 Stage 2 — Runtime
# ==========================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /workspace/transaction-api/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
