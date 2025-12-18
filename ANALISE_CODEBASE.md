# 📊 Análise Completa do Codebase - auth-api

## 🎯 Visão Geral do Projeto

**auth-api** é um microsserviço de autenticação totalmente desenvolvido para uma arquitetura Spring Boot. Implementa um sistema sofisticado de autenticação baseado em JWT com três tipos de login, controle de acesso baseado em papéis, escopo de unidades organizacionais e registro de auditoria abrangente.

---

## 📁 Estrutura do Projeto

```
auth-api/
├── src/main/java/br/lar/auth/
│   ├── config/              # Classes de configuração
│   ├── controller/          # Endpoints REST
│   ├── dto/                 # Objetos de Transferência de Dados
│   ├── mapper/              # Mapeadores MapStruct
│   ├── model/               # Entidades JPA
│   ├── repository/          # Repositórios Spring Data
│   ├── security/            # Autenticação & JWT
│   ├── service/             # Lógica de negócio
│   ├── utils/               # Classes utilitárias
│   └── AuthServiceApplication.java
├── src/main/resources/
│   ├── db/migration/        # Migrações Flyway
│   ├── application*.yml     # Configurações por perfil
│   └── META-INF/
├── docs/                    # Documentação extensiva
├── pom.xml                  # Dependências Maven
├── Dockerfile               # Build Docker multi-stage
├── docker-compose.yml       # PostgreSQL, Redis, Eureka
└── scripts/                 # Scripts de setup e manutenção
```

---

## 🔐 Componentes Principais Spring Boot

### Controllers (Endpoints REST)

#### **LoginController** (`/api/v1/auth`)

Gerencia autenticação com suporte a 3 tipos de login:

- **`POST /login`** - Autenticação com suporte a 3 tipos:
  - **Tipo 1**: username + password (acesso a todos os módulos)
  - **Tipo 2**: username + password + sistema (módulo específico)
  - **Tipo 3**: username + password + sistema + unidade (escopo de unidade)

- **`POST /refresh`** - Renovação de token de acesso usando refresh token
- **`POST /logout`** - Invalidar token e registrar evento de logout

#### **UsuarioController** (`/api/v1/usuarios`)

Gerenciamento completo de usuários:

- `GET /` - Listar usuários com paginação, filtro de status e busca
- `GET /{id}` - Obter detalhes do usuário
- `POST /` - Criar novo usuário
- `PUT /{id}` - Atualizar dados do usuário
- `DELETE /{id}` - Soft delete de usuário
- `PUT /{id}/perfis` - Alterar papéis (roles) do usuário
- `PUT /{id}/unidades` - Alterar unidades organizacionais
- `PUT /{id}/bloquear` - Bloquear usuário
- `PUT /{id}/desbloquear` - Desbloquear usuário
- `PUT /{id}/alterar-senha` - Alterar senha
- `GET /{id}/historico-logins` - Obter histórico de logins

#### **PerfilController** (`/api/v1/perfis`)

Gerenciamento de papéis (roles):

- `GET /` - Listar papéis
- `GET /{id}` - Obter detalhes do papel
- `POST /` - Criar novo papel
- `PUT /{id}` - Atualizar papel
- `DELETE /{id}` - Soft delete de papel

---

## 🛠️ Serviços (Services)

### **AuthService/AuthServiceImpl**
Orquestração da autenticação:
- Gerencia 3 tipos de login com geração apropriada de tokens
- Gerencia tentativas de login falhadas e bloqueio de conta
- Gera tokens JWT de acesso e refresh
- Valida credenciais e permissões
- Registra eventos de login/logout na trilha de auditoria
- Fluxo de renovação de tokens

### **UsuarioService/UsuarioServiceImpl**
Gerenciamento de usuários:
- Operações CRUD com validações
- Filtrar usuários por status, paginação
- Gerenciar papéis e unidades do usuário
- Bloquear/desbloquear usuários
- Gerenciamento de senhas (alteração, reset)
- Rastreamento de tentativas de login falhadas
- Validação de unicidade (username, CPF, email)

### **PerfilService/PerfilServiceImpl**
Gerenciamento de papéis:
- CRUD para papéis
- Associar funcionalidades aos papéis
- Mapear papéis para módulos

### **TokenService/TokenServiceImpl**
Operações com tokens JWT:
- Gerar tokens com assinatura HMAC256
- Validar integridade e expiração de tokens
- Extrair claims (username, ID, email, perfis, sistema, unidade)
- Gerenciar expiração de access token (24 horas)
- Gerenciar expiração de refresh token (7 dias)

---

## 🔒 Componentes de Segurança

### **SecurityConfig**
Configuração do Spring Security:
- BCrypt password encoder (strength 10)
- Registro de AuthenticationProvider customizado
- Gerenciamento de sessão: STATELESS (JWT)
- Configuração CORS (permite todas as origens com restrições de header)
- Cadeia de filtros HTTP Security com autorização específica por endpoint
- Filtro JWT adicionado antes de UsernamePasswordAuthenticationFilter

### **CustomAuthenticationProvider**
Lógica de autenticação customizada:
- Valida credenciais contra a entidade Usuario
- Gerencia 3 tipos de login
- Incrementa tentativas de login falhadas
- Bloqueia conta após 5 tentativas falhadas
- Valida status do usuário (ATIVO/INATIVO/BLOQUEADO)
- Registra sucesso/falha de login na trilha de auditoria

### **JwtTokenFilter**
Filtro JWT para requisições:
- Extrai token do header Authorization (formato Bearer)
- Valida assinatura e expiração do token
- Verifica blacklist de tokens
- Popula SecurityContext com usuário autenticado

### **UserDetailsImpl**
Implementação customizada de UserDetails:
- Encapsula entidade Usuario
- Fornece autoridades dos Perfis associados
- Mapeia perfis para formato de authorities

---

## 🔑 Fluxo de Autenticação

### Processo Completo:

1. Usuário submete credenciais para `/api/v1/auth/login`
2. LoginController valida a requisição e detecta o tipo de login
3. LoginController cria token de autenticação apropriado
4. AuthenticationManager delega para CustomAuthenticationProvider
5. CustomAuthenticationProvider valida senha e status
6. Na autenticação bem-sucedida: AuthServiceImpl gera JWT de acesso + refresh
7. Resposta inclui tokens, dados do usuário e tempo de expiração

### Detalhes dos Tokens:

- **Access Token**: Expiração de 24 horas
- **Refresh Token**: Expiração de 7 dias
- **Algoritmo**: HMAC256
- **Claims**:
  - `usuarioId`, `username`, `email`, `perfis`, `tipo` (tipo de login)
  - `sistema`, `unidade` (para Tipo 2 e 3 de login)

### Autorização:

- Anotações `@PreAuthorize` em endpoints
- Autorização baseada em papéis: `hasAuthority('ADMIN_GERAL')` ou combinações
- Validação JWT via JwtTokenFilter
- Suporte a blacklist de tokens (Redis-ready)

---

## ⚙️ Configuração

### **application.yml** (Perfil Principal)

```yaml
spring:
  application:
    name: auth-service

  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/auth_db_dev}
    username: ${DATABASE_USER:postgres}
    password: ${DATABASE_PASSWORD:postgres}

  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

  jpa:
    hibernate:
      ddl-auto: none  # Apenas migrações
    properties.hibernate.dialect: PostgreSQLDialect

  data.redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    lettuce.pool:
      max-active: 8
      max-idle: 8

server:
  port: ${SERVER_PORT:8080}

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000      # 24h
  refresh.expiration: 604800000  # 7d
```

### **Perfis Disponíveis:**

- `application-dev.yml` - Desenvolvimento (H2 em memória, logs verbosos)
- `application-test.yml` - Testes
- `application-prod.yml` - Produção
- `application-repair.yml` - Utilitários de reparo de banco de dados

---

## 📦 Dependências (pom.xml)

### Principais Dependências:

| Dependência | Versão | Propósito |
|-------------|--------|----------|
| Spring Boot | 3.5.8 | Framework Web/Aplicação |
| Java | 21+ | Linguagem |
| PostgreSQL Driver | Latest | Banco de dados produção |
| H2 | Latest | Banco em memória (testes) |
| Spring Security | 6.1+ | Autenticação e autorização |
| Spring Data JPA | 3.5+ | ORM e persistência |
| JWT (jjwt) | 0.11.5 | Geração e validação de tokens |
| Redis (Lettuce) | 7+ | Cache e session store |
| Flyway | Latest | Migrações de banco de dados |
| MapStruct | 1.5.5 | Mapeamento de objetos |
| Lombok | Latest | Redução de boilerplate |
| SpringDoc OpenAPI | 2.3.0 | Swagger/OpenAPI |
| Spring Boot Actuator | 3.5+ | Métricas e health checks |
| Maven | 3.8+ | Gerenciador de build |

---

## 💾 Modelos de Banco de Dados

### Entidades Principais (PostgreSQL, schema `auth`)

#### **usuario** (Usuários)
```
id              BIGSERIAL (PK)
username        VARCHAR (UNIQUE)
cpf             VARCHAR (UNIQUE)
nome            VARCHAR
email           VARCHAR (UNIQUE)
chave           VARCHAR (password hash BCrypt)
matricula       VARCHAR
status          ENUM (ATIVO/INATIVO/BLOQUEADO)
alterar         BOOLEAN (força alteração de senha)
tentativas_falhas INT (contador de tentativas falhadas)
data_cadastro   TIMESTAMP
data_ultimo_login TIMESTAMP
```

#### **perfil** (Papéis/Roles)
```
id              BIGSERIAL (PK)
nome            VARCHAR (UNIQUE)
descricao       TEXT
status          ENUM
id_modulo       BIGINT (FK para modulo)
```

#### **funcionalidade** (Permissões Granulares)
```
id              BIGSERIAL (PK)
nome            VARCHAR (UNIQUE)
descricao       TEXT
status          ENUM
```

#### **modulo** (Módulos do Sistema)
```
id              BIGSERIAL (PK)
nome            VARCHAR (UNIQUE)
descricao       TEXT
email_responsavel VARCHAR
status          ENUM
data_cadastro   TIMESTAMP
```

#### **unidade** (Unidades Organizacionais - Hierárquicas)
```
id                    BIGSERIAL (PK)
sigla                 VARCHAR (UNIQUE)
descricao             TEXT
unidade_superior_id   BIGINT (auto-referência para hierarquia)
situacao              ENUM
numero                VARCHAR
email                 VARCHAR
telefone              VARCHAR
endereco              VARCHAR
cep                   VARCHAR
bairro                VARCHAR
ordenacao             INT
```

#### **usuario_historico** (Trilha de Auditoria Imutável)
```
id              BIGSERIAL (PK)
id_usuario      BIGINT (FK, RESTRICT on delete)
tipo_evento     ENUM (LOGIN_SUCESSO, LOGIN_FALHA, CRIACAO, EDICAO, etc.)
descricao       TEXT
data_sistema    TIMESTAMP
modulo          VARCHAR
unidade         VARCHAR
ip_address      VARCHAR
user_agent      VARCHAR
```

### Relacionamentos Many-to-Many:

- **usuario_perfil** - Usuário ↔ Papel
- **usuario_unidade** - Usuário ↔ Unidade
- **perfil_funcionalidade** - Papel ↔ Permissão

### Índices para Performance:

- usuario: username, cpf, email, status
- perfil: nome, modulo, status
- usuario_historico: usuario+data, tipo_evento, modulo
- Queries hierárquicas de unidades otimizadas

---

## 🎯 Lógica de Negócio e Padrões

### Padrões de Autenticação:

#### **Tipo 1 - Login Básico**
- Apenas username + password
- Usuário obtém acesso a todos os módulos atribuídos
- Token inclui apenas claims básicos

#### **Tipo 2 - Login Específico de Módulo**
- username + password + sistema/módulo
- Usuário restrito àquele módulo
- Token inclui claim `sistema`

#### **Tipo 3 - Login com Escopo de Unidade**
- username + password + módulo + unidade organizacional
- Usuário restrito a unidade específica dentro do módulo
- Token inclui claims `sistema` e `unidade`

### Gerenciamento de Falhas de Login:

- Rastreia `tentativas_falhas` por usuário
- Bloqueio automático de conta após 5 tentativas falhadas
- Mudança de status para BLOQUEADO
- Desbloqueio manual pelo admin necessário

### Trilha de Auditoria:

- Registra todos os eventos em `usuario_historico` (logins, criações, atualizações, etc.)
- Imutável (RESTRICT on delete)
- Indexada para queries eficientes
- Suporta conformidade LGPD

### DTOs com Validação:

- LoginRequestDTO, LoginResponseDTO
- UsuarioDTO, PerfilDTO, etc.
- MapStruct para conversão entidade ↔ DTO
- Deserializador customizado PerfilDTODeserializer (manipula objetos completos e IDs numéricos)

### Gerenciamento de Status:

- Enum: AtivoInativo (ATIVO, INATIVO, BLOQUEADO)
- Usuários podem fazer login apenas se ATIVO
- Padrão soft delete (define status como INATIVO)

---

## 🧪 Testes e Utilitários

### Utilitários:

- `AtivoInativo` - Enum para gerenciamento de status de usuário
- `PerfilDTODeserializer` - Deserializador Jackson customizado

### Scripts Disponíveis:

- `rebuild.sh` - Limpeza e rebuild do projeto
- `repair-flyway.sh` - Utilitários de reparo do banco de dados
- `reset-db.sql`, `reset-flyway.sql` - Limpeza do banco de dados
- `init-db.sql` - Inicialização do schema PostgreSQL

### Perfis de Teste:

- `application-test.yml` configurado com banco de dados H2 em memória
- Console H2 disponível em `/h2-console`
- Flyway habilitado para migrações consistentes

---

## 📊 Estado Atual de Desenvolvimento

### Status de Conclusão:

#### **Fase 0 (Setup)**: ✅ COMPLETA
- Inicialização do projeto com Spring Initializr
- Schema do banco de dados com migrações (V1, V2, V3)
- Configuração de segurança e implementação JWT

#### **Fase 1 (Funcionalidades Principais)**: ✅ COMPLETA
- Sistema de autenticação (3 tipos de login)
- Autorização com papéis e permissões
- CRUD de gerenciamento de usuários
- Gerenciamento de papéis/perfis
- Implementação de trilha de auditoria
- Fluxo de renovação de tokens
- Mecanismo de bloqueio de conta

### Infraestrutura:

- Build Docker multi-stage (Maven builder + OpenJDK 21 runtime)
- Setup Docker Compose com:
  - PostgreSQL 15 (banco de dados)
  - Redis 7 (cache/session store)
  - Spring Cloud Eureka (service discovery opcional)
  - Redis Commander (monitoramento)

### Documentação:

- Guias extensivos no diretório `/docs`
- RESUMO_EXECUTIVO.md - Resumo executivo
- FASE0_SETUP.md, FASE1_COMPLETADA.md - Documentação das fases
- IMPLEMENTATION_COMPLETE.md - Status de implementação
- Modelos de banco de dados e diagramas de arquitetura

### Pronto Para:

- Testes e validação
- Integração com outros microsserviços
- Deployment em produção
- Refinamento da Fase 2 (se necessário)

---

## 🔧 Stack Tecnológico Resumido

| Componente | Tecnologia | Versão |
|-----------|-----------|--------|
| Linguagem | Java | 21+ |
| Framework | Spring Boot | 3.5.8 |
| Banco de Dados | PostgreSQL | 15+ |
| ORM | Hibernate/JPA | Jakarta API |
| Segurança | Spring Security + JWT | jjwt 0.11.5 |
| Cache | Redis | 7+ |
| Mapeamento | MapStruct | 1.5.5 |
| Migrações | Flyway | Latest |
| Build | Maven | 3.8+ |
| Containerização | Docker | 24+ |
| Banco de Testes | H2 | Em memória |
| Logging | SLF4J | Built-in |

---

## 📝 Referência de Caminhos de Arquivos

### Arquivos Fonte Principais:

- `src/main/java/br/lar/auth/AuthServiceApplication.java` - Classe principal
- `src/main/java/br/lar/auth/config/SecurityConfig.java` - Configuração de segurança
- `src/main/java/br/lar/auth/security/AuthServiceImpl.java` - Serviço de autenticação
- `src/main/java/br/lar/auth/security/TokenServiceImpl.java` - Serviço de tokens JWT
- `src/main/java/br/lar/auth/controller/LoginController.java` - Controller de autenticação
- `src/main/java/br/lar/auth/controller/UsuarioController.java` - Controller de usuários
- `src/main/java/br/lar/auth/model/Usuario.java` - Entidade de usuário

### Arquivos de Configuração:

- `src/main/resources/application.yml` - Configuração principal
- `src/main/resources/application-dev.yml` - Perfil desenvolvimento
- `pom.xml` - Dependências Maven

### Migrações de Banco de Dados:

- `src/main/resources/db/migration/V1__Create_Initial_Schema.sql` - Schema inicial
- `src/main/resources/db/migration/V2__Insert_Initial_Data.sql` - Dados iniciais
- `src/main/resources/db/migration/V3__Create_Sequences.sql` - Sequências

---

## ✨ Conclusão

**auth-api** é um microsserviço de autenticação **pronto para produção** com:

✅ Funcionalidades de segurança abrangentes
✅ Sistema de auditoria imutável para conformidade LGPD
✅ Suporte a multi-tenancy através de escopo de unidades organizacionais
✅ Mecanismo sofisticado de controle de acesso
✅ Migrações de banco de dados automatizadas
✅ Containerização completa com Docker
✅ Documentação extensiva
✅ Padrões Spring Boot estabelecidos

O projeto está na **Fase 1 Completa** com todas as funcionalidades principais de autenticação e autorização totalmente implementadas e prontas para testes de integração ou deployment.
