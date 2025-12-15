# ============================================================================
# Multi-stage Dockerfile for Auth Service
# ============================================================================

# Stage 1: Build
FROM maven:3-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code
COPY src src

# Build application
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM mosipid/openjdk-21-jdk




# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/* && mkdir /var/log/auth-service && chmod 777 /var/log/auth-service 

# Create non-root user for security
RUN useradd -m -u 1001 appuser

RUN touch /var/log/auth-service/application.log
WORKDIR /app

# Copy JAR from builder
COPY --from=builder /build/target/auth-service-*.jar auth-service.jar

# Set ownership
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "auth-service.jar"]
#CMD ["--spring.profiles.active=defaul"]

# Labels
LABEL description="Auth Service - User Authentication System" \
      version="1.0.0" \
      maintainer="LAR Institution"
