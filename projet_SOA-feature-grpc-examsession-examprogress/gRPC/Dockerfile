# ==============================================================================
# EduCampus - Smart Space & Exam Service (gRPC)
# Cloud-Native Multi-Stage Dockerfile (Standard section 2.6)
# ==============================================================================

# ------------------------------------------------------------------------------
# Stage 1: Build
# ------------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Cache dependency layer
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy sources and compile / package the application
COPY src ./src
RUN mvn clean package -DskipTests -B

# ------------------------------------------------------------------------------
# Stage 2: Minimal Distroless / Hardened Alpine Runtime
# ------------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine AS runner
WORKDIR /app

# Cloud-native security: create non-root service account
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy compiled executable JAR from builder stage
COPY --from=builder /app/target/smart-space-exam-service-*.jar /app/app.jar
RUN chown -R appuser:appgroup /app

# Switch to unprivileged non-root user
USER appuser

# Expose HTTP Actuator/Metrics (8082) and gRPC Server (9090)
EXPOSE 8082 9090

# Standardized cloud-native JVM flags for container limits & fast randomness
ENV PORT=8082 \
    GRPC_PORT=9090 \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Healthcheck leveraging Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
  CMD wget -qO- http://localhost:${PORT}/actuator/health | grep -q "UP" || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
