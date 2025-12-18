# 🧪 Quick Test: AWS Credentials Fix

## Resumo da Correção

**Problema Original:**
```
SdkClientException: Unable to load credentials from any of the providers in the chain
```

**Causa:**
- Aplicação tentava buscar credenciais AWS mesmo em desenvolvimento local
- Docker Compose fornecia variáveis de ambiente, mas não eram usadas

**Solução:**
- Criado `LocalDataSourceConfig` para ambiente local (variáveis de ambiente)
- Modificado `DataSourceConfig` para produção apenas (AWS Secrets Manager)
- Ambos controlados por flag `datasource.use-env-vars`

---

## Arquivos Modificados

| Arquivo | Mudança |
|---------|---------|
| `AwsSecretsManager.java` | +`@ConditionalOnProperty` para carregar apenas em produção |
| `DataSourceConfig.java` | +`@ConditionalOnProperty` para carregar apenas em produção |
| **`LocalDataSourceConfig.java`** | ✨ Novo arquivo para desenvolvimento local |
| `application.yml` | +`datasource.use-env-vars` property |
| `docker-compose.yml` | +`DATASOURCE_USE_ENV_VARS=true` |

---

## ✅ Teste Rápido

### 1. Compilar

```bash
mvn clean compile -DskipTests
```

**Esperado:** BUILD SUCCESS ✅

### 2. Executar Docker Compose

```bash
docker-compose up -d
```

**Esperado:**
```
[LocalDataSourceConfig] ✅ DataSource criado a partir de variáveis de ambiente
JDBC URL: jdbc:postgresql://postgres:5432/auth_db_dev
Username: postgres
```

### 3. Testar Saúde da Aplicação

```bash
curl http://localhost:8088/actuator/health
```

**Esperado:**
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "redis": { "status": "UP" }
  }
}
```

### 4. Acessar Swagger

```
http://localhost:8088/swagger-ui.html
```

**Esperado:** Interface Swagger carregada com sucesso

---

## 🔄 Como Funciona Agora

### Desenvolvimento (Docker)

```yaml
# docker-compose.yml
auth-service:
  environment:
    DATASOURCE_USE_ENV_VARS: 'true'  # ← Ativa LocalDataSourceConfig
    DATABASE_URL: jdbc:postgresql://postgres:5432/auth_db_dev
    DATABASE_USER: postgres
    DATABASE_PASSWORD: postgres
```

```java
// Spring Boot
if (DATASOURCE_USE_ENV_VARS == true) {
  load LocalDataSourceConfig
  use DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD
} else {
  load DataSourceConfig
  use AWS Secrets Manager
}
```

### Produção (AWS)

```bash
# Sem definir DATASOURCE_USE_ENV_VARS (valor padrão = false)
java -jar auth-service-1.0.0.jar
```

```java
// Spring Boot
if (DATASOURCE_USE_ENV_VARS == false) {  // padrão
  load DataSourceConfig
  use AWS Secrets Manager (com IAM Role)
}
```

---

## 🎯 Próximos Passos

### Para Manter em Desenvolvimento
✅ Teste o docker-compose

### Para Produção
1. Certifique-se que AWS Secrets Manager está configurado
2. Anexe IAM Role correta à instância
3. Não defina `DATASOURCE_USE_ENV_VARS` (deixe padrão)

---

## 📝 Notas Importantes

- **Local:** Sempre use `DATASOURCE_USE_ENV_VARS=true`
- **Produção:** Deixe como padrão `false` (ou defina explicitamente)
- **Segurança:** Nunca commita credenciais no Git
- **AWS:** Use IAM Roles, nunca credentials hardcoded

---

## 🚀 Scripts Disponíveis

Dentro do projeto:
- `test-with-credentials.sh` - Teste completo com Docker Compose

```bash
./test-with-credentials.sh
```
