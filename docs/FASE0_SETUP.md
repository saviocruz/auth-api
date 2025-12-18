# FASE 0 - Foundational Setup & Infrastructure

**Status**: ✅ COMPLETED
**Date**: December 9, 2025

---

## 📋 Overview

FASE 0 establishes the foundational infrastructure for the Authentication System. This includes:

- ✅ Database schema with Flyway migrations
- ✅ Environment configuration profiles (dev, test, prod)
- ✅ Docker & Docker Compose setup
- ✅ Database initialization scripts
- ✅ Development environment

---

## 🗄️ US-000-1: Database Schema & Flyway Migrations

### Files Created

#### Migration Files
- `src/main/resources/db/migration/V1__Create_Initial_Schema.sql`
  - Complete PostgreSQL schema
  - 11 tables with relationships
  - 2 views for reporting
  - ENUM types for PostgreSQL
  - Indexes for performance
  - Comments for documentation

- `src/main/resources/db/migration/V2__Insert_Initial_Data.sql`
  - 5 Organizational Units (Unidades)
  - 5 System Modules (Módulos)
  - 12 Granular Permissions (Funcionalidades)
  - 9 Roles/Profiles (Perfis)
  - 8 Test Users with different roles and statuses
  - Relationships between entities
  - Initial audit trail entries

### Database Schema

#### Tables

```
┌─────────────────────────────────────────────────────────┐
│                  AUTHENTICATION SYSTEM                   │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  unidade                                                │
│  ├── id (PK)                                            │
│  ├── sigla (unique)                                     │
│  ├── unidade_superior_id (FK) [Hierarchical]           │
│  └── ... (descricao, email, telefone, endereco, etc)   │
│                                                          │
│  modulo                                                 │
│  ├── id (PK)                                            │
│  ├── nome (unique)                                      │
│  └── ... (descricao, email_responsavel, status)        │
│                                                          │
│  funcionalidade                                         │
│  ├── id (PK)                                            │
│  ├── nome (unique)                                      │
│  └── ... (descricao, status)                           │
│                                                          │
│  perfil                                                 │
│  ├── id (PK)                                            │
│  ├── nome (unique)                                      │
│  ├── modulo_id (FK)                                     │
│  └── ... (descricao, status)                           │
│                                                          │
│  perfil_funcionalidade (M:N)                            │
│  ├── perfil_id (FK)                                     │
│  └── funcionalidade_id (FK)                             │
│                                                          │
│  usuario                                                │
│  ├── id (PK)                                            │
│  ├── username (unique)                                  │
│  ├── cpf (unique)                                       │
│  ├── email (unique)                                     │
│  └── ... (nome, chave, status, tentativas_falhas)      │
│                                                          │
│  usuario_perfil (M:N)                                   │
│  ├── usuario_id (FK)                                    │
│  └── perfil_id (FK)                                     │
│                                                          │
│  usuario_unidade (M:N)                                  │
│  ├── usuario_id (FK)                                    │
│  └── unidade_id (FK)                                    │
│                                                          │
│  usuario_historico (Audit Trail - Immutable)           │
│  ├── id (PK)                                            │
│  ├── usuario_id (FK)                                    │
│  ├── tipo_evento                                        │
│  └── ... (descricao, data_sistema, modulo, unidade)    │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

#### Enum Types

```sql
ativo_inativo_enum: ATIVO, INATIVO, BLOQUEADO

tipo_evento_enum: LOGIN_SUCESSO, LOGIN_FALHA, CRIACAO, EDICAO,
                  DELECAO, BLOQUEIO, DESBLOQUEIO, ALTERACAO_SENHA,
                  RESET_SENHA, ALTERACAO_PERFIS, ALTERACAO_UNIDADES,
                  LOGOUT
```

#### Indexes

All tables have appropriate indexes for:
- Primary keys
- Foreign keys
- Unique constraints (username, cpf, email, sigla, nome)
- Common filter columns (status, situacao, tipo_evento)

---

## ⚙️ US-000-2: Configuration Profiles

### Application Configuration

Base configuration: `application.yml`
- PostgreSQL connection
- Flyway migration settings
- Hibernate/JPA configuration
- Redis cache setup
- JWT settings
- Logging configuration
- SpringDoc OpenAPI (Swagger)

### Environment Profiles

#### Development Profile (`application-dev.yml`)
- **Database**: PostgreSQL on localhost:5432
- **Redis**: localhost:6379
- **Log Level**: DEBUG
- **Features**: SQL showing, Swagger enabled, All actuator endpoints exposed
- **JWT Secret**: dev-secret-key (change in production)

**Usage**:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=dev
```

#### Test Profile (`application-test.yml`)
- **Database**: H2 in-memory (isolated from dev)
- **JPA**: create-drop mode (clean DB per run)
- **Flyway**: Disabled (H2 not supported)
- **Log Level**: WARN
- **Features**: H2 console enabled, Swagger disabled

**Usage**:
```bash
mvn test -Dspring-boot.run.arguments=--spring.profiles.active=test
```

#### Production Profile (`application-prod.yml`)
- **Database**: PostgreSQL (via environment variables)
- **Redis**: Redis (via environment variables)
- **Log Level**: WARN
- **Features**: No Swagger, Prometheus metrics, Compression enabled
- **HikariCP**: Optimized for production
  - max-pool-size: 20
  - idle-timeout: 10 minutes

**Usage**:
```bash
java -jar auth-service.jar --spring.profiles.active=prod \
  --DATABASE_URL=jdbc:postgresql://prod-host:5432/auth_db \
  --DATABASE_USER=admin \
  --DATABASE_PASSWORD=secure-password \
  --REDIS_HOST=redis-host \
  --JWT_SECRET=your-256-bit-secret-key
```

---

## 🐳 US-000-3: Docker & Docker Compose

### Dockerfile

Multi-stage Docker build:

**Stage 1: Builder**
- Uses Maven 3.8 + OpenJDK 21
- Downloads all dependencies
- Builds application JAR

**Stage 2: Runtime**
- Uses OpenJDK 21 slim image
- Installs curl for health checks
- Creates non-root user (appuser)
- Sets up health check endpoint
- Exposes port 8080

### Docker Compose Services

#### 1. PostgreSQL
```yaml
- Image: postgres:15-alpine
- Port: 5432
- Volumes: postgres_data
- Health Check: pg_isready
- Init Script: init-db.sql
```

#### 2. Redis
```yaml
- Image: redis:7-alpine
- Port: 6379
- Volumes: redis_data
- Health Check: redis-cli ping
- Persistence: appendonly mode
```

#### 3. Auth Service Application
```yaml
- Custom Docker image built from Dockerfile
- Port: 8080
- Environment: dev configuration
- Depends on: postgres and redis (healthy)
```

#### 4. pgAdmin (PostgreSQL UI)
```yaml
- Image: dpage/pgadmin4
- Port: 5050
- URL: http://localhost:5050
- Credentials: admin@instituicao.com.br / admin
```

#### 5. Redis Commander (Redis UI)
```yaml
- Image: rediscommander/redis-commander
- Port: 8081
- URL: http://localhost:8081
```

### Docker Network

All services connected via `auth-network` (bridge) for internal communication.

---

## 🚀 Quick Start Guide

### Prerequisites

- Docker 24+ and Docker Compose 2.0+
- Maven 3.8+ (if building locally)
- Java 21 (if running locally)
- PostgreSQL 15+ (if running locally)

### Option 1: Run with Docker Compose (Recommended)

```bash
# Clone repository
cd /home/savio/novo-1

# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f auth-service

# Verify services
docker-compose ps

# Stop services
docker-compose down
```

**Access Points**:
- Application: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- pgAdmin: http://localhost:5050
- Redis Commander: http://localhost:8081
- Actuator Health: http://localhost:8080/actuator/health

### Option 2: Run Locally (Development)

#### 1. Start PostgreSQL

```bash
# Install PostgreSQL 15
# Create database and user
psql -U postgres -c "CREATE DATABASE auth_db_dev;"
psql -U postgres -c "CREATE USER auth_user WITH PASSWORD 'auth_password';"
psql -U postgres -c "ALTER DATABASE auth_db_dev OWNER TO auth_user;"
```

#### 2. Start Redis

```bash
# Install Redis 7
# Run Redis server
redis-server
```

#### 3. Run Application

```bash
# Build project
mvn clean install

# Run with dev profile
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=dev
```

#### 4. Access Application

- Application: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

---

## 📊 Test Users (Seed Data)

The seed data (V2 migration) creates 8 test users:

| Username | Email | Role | Status | Password | Module |
|----------|-------|------|--------|----------|--------|
| admin | admin@instituicao.com.br | ROLE_ADMIN_GERAL | ATIVO | admin123 | ADMIN |
| rh_manager | rh.manager@instituicao.com.br | ROLE_RH_GERENTE | ATIVO | rh123 | RECURSOS_HUMANOS |
| rh_consultant | rh.consultant@instituicao.com.br | ROLE_RH_CONSULTOR | ATIVO | usuario123 | RECURSOS_HUMANOS |
| finance_manager | finance.manager@instituicao.com.br | ROLE_FINANCEIRO_GERENTE | ATIVO | financeiro123 | FINANCEIRO |
| finance_analyst | finance.analyst@instituicao.com.br | ROLE_FINANCEIRO_ANALISTA | ATIVO | usuario123 | FINANCEIRO |
| usuario | usuario@instituicao.com.br | ROLE_USUARIO | ATIVO | usuario123 | ADMIN |
| usuario_inativo | usuario.inativo@instituicao.com.br | ROLE_USUARIO | INATIVO | usuario123 | ADMIN |
| usuario_bloqueado | usuario.bloqueado@instituicao.com.br | ROLE_USUARIO | BLOQUEADO | usuario123 | ADMIN |

**Note**: All passwords are hashed with BCrypt. The plain text passwords shown above are for development only.

---

## 🔧 Database Operations

### Connect to PostgreSQL (Docker)

```bash
# Via psql
psql -h localhost -U postgres -d auth_db_dev

# Via Docker
docker-compose exec postgres psql -U postgres -d auth_db_dev
```

### View Flyway History

```sql
SELECT * FROM flyway_schema_history;
```

### Reset Database (Clean)

```bash
# Drop and recreate schema
docker-compose exec postgres psql -U postgres -d auth_db_dev \
  -c "DROP SCHEMA auth CASCADE; DROP SCHEMA public CASCADE; CREATE SCHEMA public;"

# Restart services (Flyway will re-run migrations)
docker-compose down
docker-compose up -d
```

### Backup Database

```bash
# Using pg_dump
docker-compose exec postgres pg_dump -U postgres auth_db_dev > backup.sql

# Using Docker volumes
docker run --rm -v auth-postgres_data:/data -v $(pwd):/backup \
  alpine tar czf /backup/postgres-backup.tar.gz /data
```

---

## 🔐 Environment Variables

### Development

```bash
SPRING_PROFILES_ACTIVE=dev
DATABASE_URL=jdbc:postgresql://localhost:5432/auth_db_dev
DATABASE_USER=postgres
DATABASE_PASSWORD=postgres
REDIS_HOST=localhost
REDIS_PORT=6379
JWT_SECRET=dev-secret-key
```

### Production

```bash
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=jdbc:postgresql://prod-host:5432/auth_db
DATABASE_USER=<secure-user>
DATABASE_PASSWORD=<secure-password>
REDIS_HOST=redis-host
REDIS_PORT=6379
REDIS_PASSWORD=<redis-password>
JWT_SECRET=<256-bit-secret-key>
SERVER_PORT=8080
```

---

## 📝 Flyway Migrations

### Migration Naming Convention

Flyway migrations follow naming pattern: `V<version>__<description>.sql`

- **V1**: Create initial schema (tables, relationships, indexes)
- **V2**: Insert seed data (units, modules, roles, users)

### Running Migrations

Migrations run automatically on application startup:

1. Connection established to database
2. Flyway checks `flyway_schema_history` table
3. New migrations executed in order
4. Baseline recorded

### Manual Migration

```bash
# Run migrations via Maven
mvn flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5432/auth_db_dev \
                   -Dflyway.user=postgres \
                   -Dflyway.password=postgres

# Check migration status
mvn flyway:info
```

---

## 📚 Documentation

See also:
- [FASE1_COMPLETADA.md](./FASE1_COMPLETADA.md) - FASE 1 implementation details
- [ROADMAP_EXECUCAO_COMPLETO.md](./ROADMAP_EXECUCAO_COMPLETO.md) - Full project roadmap
- [REQUISITOS_SISTEMA_CADASTRO_USUARIOS.md](./REQUISITOS_SISTEMA_CADASTRO_USUARIOS.md) - Detailed requirements

---

## ✅ Checklist

- [x] Database schema created (V1 migration)
- [x] Seed data inserted (V2 migration)
- [x] Development profile configured
- [x] Test profile configured
- [x] Production profile configured
- [x] Dockerfile created
- [x] Docker Compose configured
- [x] PostgreSQL service setup
- [x] Redis service setup
- [x] pgAdmin UI added
- [x] Redis Commander UI added
- [x] Documentation complete
- [x] Flyway integration working
- [x] Health checks configured

---

## 🐛 Troubleshooting

### Issue: "Connection refused" to PostgreSQL

**Solution**:
```bash
# Check if postgres service is running
docker-compose ps

# Restart postgres
docker-compose restart postgres

# Check logs
docker-compose logs postgres
```

### Issue: "Flyway validation failed"

**Solution**:
```bash
# Check migration history
docker-compose exec postgres psql -U postgres -d auth_db_dev \
  -c "SELECT * FROM flyway_schema_history;"

# If corrupted, reset
docker-compose down -v  # Delete volumes
docker-compose up -d     # Fresh start
```

### Issue: "Redis connection timeout"

**Solution**:
```bash
# Check if redis is running
docker-compose ps redis

# Restart redis
docker-compose restart redis

# Verify connectivity
docker-compose exec redis redis-cli ping
```

---

## 📞 Support

For issues or questions:

1. Check logs: `docker-compose logs <service>`
2. Verify configuration in `application-dev.yml`
3. Ensure all prerequisites are installed
4. Check database connectivity

---

**FASE 0 - Complete and Ready for FASE 1+ Development**

Generated: 2025-12-09
Version: 1.0.0
