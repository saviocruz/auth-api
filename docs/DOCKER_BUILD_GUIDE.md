# Docker Build & Deployment Guide

**Date**: December 9, 2025
**Version**: 1.0.0

---

## 🐳 Building the Docker Image

### Prerequisites

- Docker 24+ installed
- `pom.xml` in project root
- `Dockerfile` in project root
- `.dockerignore` configured properly
- All source code in `src/` directory

### Build Commands

#### Build Image Locally

```bash
# Navigate to project directory
cd /home/savio/novo-1

# Build Docker image
docker build -t auth-service:1.0.0 .

# Tag with latest
docker tag auth-service:1.0.0 auth-service:latest

# Verify image created
docker images | grep auth-service
```

**Output**:
```
REPOSITORY      TAG         IMAGE ID      CREATED      SIZE
auth-service    1.0.0       abc123def     2 min ago    450MB
auth-service    latest      abc123def     2 min ago    450MB
```

#### Build with Docker Compose

```bash
# Build services
docker-compose build

# Build specific service
docker-compose build auth-service

# Build with no cache (fresh build)
docker-compose build --no-cache
```

---

## 🚀 Running the Container

### Option 1: Docker Run

```bash
# Run container with environment variables
docker run -d \
  --name auth-service \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e DATABASE_URL=jdbc:postgresql://postgres:5432/auth_db_dev \
  -e DATABASE_USER=postgres \
  -e DATABASE_PASSWORD=postgres \
  -e REDIS_HOST=redis \
  -e REDIS_PORT=6379 \
  -e JWT_SECRET=dev-secret-key \
  auth-service:latest

# View logs
docker logs -f auth-service

# Stop container
docker stop auth-service

# Remove container
docker rm auth-service
```

### Option 2: Docker Compose (Recommended)

```bash
# Start all services
docker-compose up -d

# View status
docker-compose ps

# View logs
docker-compose logs -f auth-service

# Stop services
docker-compose stop

# Stop and remove containers
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v
```

---

## 📊 Understanding the Dockerfile

### Multi-Stage Build Strategy

The Dockerfile uses a **2-stage build** for optimization:

```dockerfile
# Stage 1: Builder
FROM maven:3-eclipse-temurin-21-alpine AS builder
# - Downloads Maven 3.8 with OpenJDK 21
# - Downloads dependencies: mvn dependency:go-offline
# - Compiles source code: mvn clean package -DskipTests
# Result: auth-service-1.0.0.jar

# Stage 2: Runtime
FROM openjdk:21-jdk-slim
# - Starts with fresh OpenJDK 21 slim image
# - Installs curl for health checks
# - Creates non-root user (appuser)
# - Copies only the JAR from stage 1
# Result: Minimal ~450MB production image
```

### Benefits

- **Smaller Image**: Only runtime needed, not build tools
- **Security**: Non-root user (appuser)
- **Health**: Built-in health check
- **Efficiency**: Fast rebuilds (Docker layer caching)

---

## 🔧 Dockerfile Components

### Stage 1: Build Layer

```dockerfile
FROM maven:3-eclipse-temurin-21-alpine AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline  # Cache dependencies
COPY src src
RUN mvn clean package -DskipTests  # Build JAR
```

**Time**: ~3-5 minutes (first run)
**Size**: 1.5GB (includes Maven cache)

### Stage 2: Runtime Layer

```dockerfile
FROM openjdk:21-jdk-slim
RUN apt-get update && apt-get install -y curl  # Health checks
RUN useradd -m -u 1001 appuser  # Non-root user

WORKDIR /app
COPY --from=builder /build/target/auth-service-*.jar auth-service.jar
RUN chown -R appuser:appuser /app
USER appuser

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1
```

**Size**: ~450MB
**Runtime User**: appuser (UID 1001)

---

## ⚙️ .dockerignore Configuration

**Current Configuration**:
```
target/           # Exclude build artifacts
.git              # Exclude git directory
.gitignore        # Exclude git config
*.md              # Exclude markdown (docs)
*.log             # Exclude log files
logs/             # Exclude log directory
.idea/            # Exclude IDE config
.vscode/          # Exclude VS Code config
*.iml             # Exclude IntelliJ files
node_modules/     # Exclude node packages
.env              # Exclude env files
.env.local        # Exclude local env
.dockerignore     # Exclude self
docker-compose.yml # Exclude compose file
```

**IMPORTANT**: `src/` is NOT ignored because it's needed for the build!

---

## 🐛 Troubleshooting

### Issue: "no such file or directory: src"

**Cause**: `src/` was in `.dockerignore`

**Solution**: Remove `src/` from `.dockerignore`

```dockerfile
# Wrong:
target/
src/          # ❌ DON'T IGNORE THIS!
.git

# Correct:
target/
.git          # ✅ src/ is not ignored
```

---

### Issue: "Docker image not found"

**Cause**: Image not built before running

**Solution**: Build first

```bash
docker build -t auth-service:1.0.0 .
docker run auth-service:1.0.0
```

---

### Issue: "Port 8080 already in use"

**Cause**: Another service using port 8080

**Solution**: Map to different port

```bash
docker run -p 8081:8080 auth-service:1.0.0
# Access via: http://localhost:8081
```

---

### Issue: "Connection refused to PostgreSQL/Redis"

**Cause**: Services not running or network issue

**Solution**: Ensure services are running

```bash
# If using Docker Compose
docker-compose ps
# Output should show:
# auth-postgres    Up (healthy)
# auth-redis       Up (healthy)
# auth-service     Up

# If using standalone, ensure network exists
docker network create auth-network
docker run --network auth-network -e DATABASE_URL=jdbc:postgresql://postgres:5432/auth_db_dev auth-service:1.0.0
```

---

## 📈 Build Statistics

### Build Time

| Stage | Time | Notes |
|-------|------|-------|
| Dependencies | 1-2 min | First run only (cached after) |
| Compilation | 2-3 min | Compiles all source |
| JAR Creation | 30s | Packages JAR |
| Runtime Stage | 1 min | Base image + configuration |
| **Total** | **4-6 min** | First run; ~1 min on subsequent |

### Image Size

```
Stage 1 (Builder): 1.5 GB (temporary, not in final image)
Stage 2 (Runtime): 450 MB (production image)
```

### Layers

The final image has ~10 layers:
1. openjdk:21-jdk-slim base
2. Update packages
3. Install curl
4. Create user
5. Set workdir
6. Copy JAR
7. Change ownership
8. Set user
9. Expose port
10. Health check

Each layer is cached independently for faster rebuilds.

---

## 🚢 Production Deployment

### Push to Registry

```bash
# Login to Docker Hub
docker login

# Tag with registry
docker tag auth-service:1.0.0 myregistry/auth-service:1.0.0

# Push image
docker push myregistry/auth-service:1.0.0
```

### Kubernetes Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: auth-service
spec:
  replicas: 2
  selector:
    matchLabels:
      app: auth-service
  template:
    metadata:
      labels:
        app: auth-service
    spec:
      containers:
      - name: auth-service
        image: myregistry/auth-service:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: auth-secrets
              key: database-url
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 40
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/ready
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 5
```

---

## 📋 Checklist

- [x] Dockerfile created (multi-stage)
- [x] .dockerignore configured (src/ NOT ignored)
- [x] pom.xml verified
- [x] Application compiles
- [x] Docker image builds successfully
- [x] Container starts and is healthy
- [x] Actuator endpoints respond
- [x] Database connection works
- [x] Redis connection works
- [x] Health checks pass
- [x] Non-root user configured
- [x] Environment variables work

---

## 🔗 Related Documentation

- [FASE0_SETUP.md](./FASE0_SETUP.md) - Full infrastructure setup
- [docker-compose.yml](./docker-compose.yml) - Compose configuration
- [application.yml](./src/main/resources/application.yml) - App configuration
- [pom.xml](./pom.xml) - Maven configuration

---

**Docker setup complete and ready for production deployment!**
