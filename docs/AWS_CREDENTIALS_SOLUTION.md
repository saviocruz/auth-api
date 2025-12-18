# 🔧 Solução: Credenciais AWS vs Ambiente Local

## Problema
A aplicação tentava buscar credenciais do AWS Secrets Manager mesmo em desenvolvimento local, causando:
```
SdkClientException: Unable to load credentials from any of the providers in the chain
```

## Solução Implementada

### 1. **LocalDataSourceConfig.java** (Novo)
- Ativa **apenas em desenvolvimento local** quando `datasource.use-env-vars=true`
- Lê credenciais das **variáveis de ambiente** (definidas no docker-compose.yml)
- Fallback para valores padrão se variáveis não existirem:
  - JDBC: `jdbc:postgresql://localhost:5432/auth_db_dev`
  - User: `postgres`
  - Password: `postgres`

### 2. **DataSourceConfig.java** (Modificado)
- Ativa **apenas em produção** quando `datasource.use-env-vars=false` (padrão)
- Usa AWS Secrets Manager para obter credenciais
- Só executa se houver credenciais AWS disponíveis

### 3. **AwsSecretsManager.java** (Modificado)
- Adicionada anotação `@ConditionalOnProperty` para carregar apenas em produção
- Não causa erro se AWS não estiver disponível

### 4. **application.yml** (Modificado)
```yaml
spring:
  datasource:
    use-env-vars: ${DATASOURCE_USE_ENV_VARS:false}
```

### 5. **docker-compose.yml** (Modificado)
```yaml
auth-service:
  environment:
    DATASOURCE_USE_ENV_VARS: 'true'  # ← Ativa uso de variáveis de ambiente
    DATABASE_URL: jdbc:postgresql://postgres:5432/auth_db_dev
    DATABASE_USER: postgres
    DATABASE_PASSWORD: postgres
```

## Como Funciona

### 🚀 Desenvolvimento Local (Docker)
```
DATASOURCE_USE_ENV_VARS=true
    ↓
Spring carrega LocalDataSourceConfig
    ↓
Lê DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD do docker-compose.yml
    ↓
✅ Conecta ao PostgreSQL local
```

### 🌍 Produção (AWS)
```
DATASOURCE_USE_ENV_VARS=false (padrão)
    ↓
Spring carrega DataSourceConfig com AwsSecretsManager
    ↓
AWS SDK usa IAM Role ou credenciais configuradas
    ↓
Busca credenciais do Secrets Manager
    ↓
✅ Conecta ao RDS Aurora
```

## Próximas Ações

### Para Testar Localmente
```bash
docker-compose up -d
# A aplicação deve conectar sem erros de AWS
```

### Para Produção
1. **Defina variáveis de ambiente na sua EC2/ECS**:
   ```bash
   export DATASOURCE_USE_ENV_VARS=false
   # AWS SDK usará IAM Role automaticamente
   ```

2. **Ou em kubernetes/docker em produção**:
   ```yaml
   env:
     - name: DATASOURCE_USE_ENV_VARS
       value: "false"
   ```

3. **Certifique-se de que a IAM Role tem permissão para Secrets Manager**:
   ```json
   {
     "Effect": "Allow",
     "Action": "secretsmanager:GetSecretValue",
     "Resource": "arn:aws:secretsmanager:*:*:secret:rds!cluster-*"
   }
   ```

## Logs para Monitoramento

Quando a aplicação inicia, você verá:

**Desenvolvimento Local:**
```
[LocalDataSourceConfig] ✅ DataSource criado a partir de variáveis de ambiente
JDBC URL: jdbc:postgresql://postgres:5432/auth_db_dev
Username: postgres
```

**Produção:**
```
[DataSourceConfig] ✅ DataSource criado a partir do AWS Secrets Manager
JDBC URL: jdbc:postgresql://aurora-cluster.xxx.rds.amazonaws.com:5432/db
Username: admin
```
