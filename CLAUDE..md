# CLAUDE.md - Desenvolvimento Spring Boot Microservices

## 🎯 Stack Tecnológica

### Core
- **Java**: 21+
- **Spring Boot**: 3.5+
- **Spring Framework**: 6.1+
- **Maven**: 3.8+ ou Gradle 8.0+

### Spring Ecosystem
- **Spring Data JPA**: Persistência e ORM
- **Spring Security**: Autenticação e autorização
- **Spring Cloud Config**: Configuração centralizada
- **Spring Cloud Circuit Breaker**: Resiliência (Resilience4j)

### Banco de Dados
- **PostgreSQL**: 15+ (produção recomendado)
- **MySQL**: 8.0+ (alternativa)
- **H2**: Database em memória (testes)
- **Flyway** ou **Liquibase**: Migrations

### Cache & Mensageria
- **Redis**: 7+ (cache, sessões, blacklist tokens)
- **RabbitMQ** ou **Apache Kafka**: Mensageria assíncrona (opcional)

### Segurança
- **JWT (jjwt)**: 0.11.5+ - Tokens de autenticação
- **BCrypt**: Hash de senhas
- **OAuth2**  e **OpenID**: Integração com provedores externos (opcional)

### Observabilidade
- **Spring Boot Actuator**: Métricas e health checks
- **Micrometer**: Métricas
- **ELK Stack** (Elasticsearch, Logstash, Kibana): Logs

### Containerização & Orquestração
- **Docker**: 24+
- **Docker Compose**: Ambiente local
- **Kubernetes**: Produção (opcional)

### Testes
- **JUnit 5**: Framework de testes
- **Mockito**: Mocks
- **TestContainers**: Testes de integração com containers
- **REST Assured**: Testes de API REST
- **WireMock**: Mock de APIs externas

### Ferramentas de Desenvolvimento
- **Lombok**: Redução de boilerplate
- **MapStruct**: Mapeamento de objetos
- **Swagger/OpenAPI**: Documentação de APIs
- **IntelliJ IDEA** ou **VS Code**: IDEs

### CI/CD
- **GitHub Actions**, **GitLab CI** ou **Jenkins**
- **SonarQube**: Qualidade de código

---

## 🚀 Criação do Projeto via Spring Initializr

### 1. Via Web (https://start.spring.io/)

**Configurações Base:**
- **Project**: Maven ou Gradle
- **Language**: Java
- **Spring Boot**: 3.5.x (última estável)
- **Packaging**: Jar
- **Java**: 21+

**Dependencies a selecionar:**

```
Spring Web
Spring Data JPA
Spring Security
PostgreSQL Driver
Lombok
Validation
Spring Boot Actuator
Config Client (Spring Cloud Config)
```

### 2. Via Spring Boot CLI

```bash
spring init \
  --dependencies=web,data-jpa,security,postgresql,lombok,validation,actuator \
  --type=maven-project \
  --java-version=21 \
  --packaging=jar \
  --group-id=br.com.local.api \
  --artifact-id=api-service \
  --name=api-service \
  api-service
```

### 3. Via IDE (IntelliJ IDEA)

```
File → New → Project → Spring Initializr
- Selecionar as mesmas configurações acima
- IDE gerará o projeto automaticamente
```

---

## 📦 Dependências Adicionais (pom.xml)

Adicione apenas as dependências que **não estão** no Spring Initializr:

```xml
<properties>
    <jjwt.version>0.11.5</jjwt.version>
</properties>

<dependencies>
    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>${jjwt.version}</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>${jjwt.version}</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>${jjwt.version}</version>
        <scope>runtime</scope>
    </dependency>

    <!-- Redis -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>

    <!-- MapStruct -->
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>1.5.5.Final</version>
    </dependency>
    
    <!-- SpringDoc OpenAPI -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.3.0</version>
    </dependency>
</dependencies>
 
```

---

## ⚙️ Configuração Mínima (application.yml)

```yaml
spring:
  application:
    name: ${SERVICE_NAME:service}
  
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/db}
    username: ${DATABASE_USER:postgres}
    password: ${DATABASE_PASSWORD:password}
  
  jpa:
    hibernate:
      ddl-auto: ${DDL_AUTO:validate}
    show-sql: ${SHOW_SQL:false}
  
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}

jwt:
  secret: ${JWT_SECRET}
  expiration: ${JWT_EXPIRATION:86400000}

server:
  port: ${SERVER_PORT:8080}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

---

## 🐳 Docker Compose (Infraestrutura Local)

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
      POSTGRES_DB: db
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

volumes:
  postgres_data:
```

---

## 📝 Estrutura de Pacotes Recomendada

```
src/main/java/com/project/service/
├── config/          # Configurações (Security, Redis, etc)
├── controller/      # REST Controllers
├── dto/             # DTOs e Requests/Responses
├── exception/       # Exception handlers
├── mapper/          # MapStruct mappers
├── model/           # Entidades JPA
├── repository/      # Spring Data Repositories
├── security/        # JWT, Filters, etc
└── service/         # Lógica de negócio
```

---

## 🔧 Comandos Úteis

```bash
# Gerar projeto
spring init --list  # Ver opções disponíveis

# Build
mvn clean install
./mvnw clean install  # Maven Wrapper

# Run
mvn spring-boot:run
./mvnw spring-boot:run

# Testes
mvn test
mvn verify  # Com testes de integração

# Docker Build
docker build -t service-name .

# Docker Compose
docker-compose up -d
docker-compose logs -f service-name
docker-compose down
```

---

## 📚 Documentação Oficial

- **Spring Initializr**: https://start.spring.io/
- **Spring Boot**: https://spring.io/projects/spring-boot
- **Spring Security**: https://spring.io/projects/spring-security
- **Spring Data JPA**: https://spring.io/projects/spring-data-jpa

---

## 💡 Dicas

1. **Use o Spring Initializr** para gerar a estrutura base - economiza tempo e garante versões compatíveis
2. **Deixe o Maven/Gradle gerenciar versões** - use dependencyManagement para Spring Cloud
3. **Properties externalizadas** - use variáveis de ambiente em produção
4. **Migrations obrigatórias** - nunca use `ddl-auto: create` ou `update` em produção
5. **Health checks** - configure Actuator desde o início
6. **Lombok** - reduz muito código boilerplate (getters, setters, builders)

---

**Nota**: Este guia foca na stack e criação automatizada. O Spring Initializr gera 90% do código necessário. Adicione apenas lógica de negócio específica.