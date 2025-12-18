# 📋 Guia de Configuração: AWS Credentials

## 🎯 Resumo da Solução

A aplicação agora suporta automaticamente dois modos:

| Ambiente | Flag | Credenciais | Quando Usar |
|----------|------|-------------|------------|
| **Local** | `DATASOURCE_USE_ENV_VARS=true` | Variáveis de ambiente | Docker Compose em dev |
| **Produção** | `DATASOURCE_USE_ENV_VARS=false` | AWS Secrets Manager | AWS EC2/ECS/RDS |

---

## 🚀 Desenvolvimento Local

### Opção 1: Com Docker Compose (Recomendado)

```bash
# Iniciar tudo automaticamente
docker-compose up -d

# Verificar logs
docker-compose logs -f auth-service
```

**Como funciona:**
- `docker-compose.yml` define `DATASOURCE_USE_ENV_VARS=true`
- Spring carrega `LocalDataSourceConfig`
- Credenciais vêm de variáveis de ambiente definidas no compose

### Opção 2: Aplicação Local + Banco Remoto

```bash
# Iniciar apenas PostgreSQL
docker-compose up -d postgres redis

# Executar aplicação
java -Ddatasource.use-env-vars=true \
  -DDATABASE_URL=jdbc:postgresql://localhost:5432/auth_db_dev \
  -DDATABASE_USER=postgres \
  -DDATABASE_PASSWORD=postgres \
  -jar target/auth-service-1.0.0.jar
```

---

## ☁️ Produção (AWS)

### Pré-requisitos

1. **RDS Aurora PostgreSQL** já provisionado
2. **AWS Secrets Manager** com credenciais armazenadas:
   ```json
   {
     "username": "admin",
     "password": "sua-senha-segura",
     "host": "aurora-cluster.xxx.rds.amazonaws.com",
     "port": 5432,
     "dbname": "seu_banco"
   }
   ```
3. **IAM Role** anexada à instância EC2/ECS/Lambda com permissão:
   ```json
   {
     "Effect": "Allow",
     "Action": "secretsmanager:GetSecretValue",
     "Resource": "arn:aws:secretsmanager:us-east-1:123456789:secret:rds!cluster-*"
   }
   ```

### Configuração em EC2

```bash
# Copiar JAR para EC2
scp target/auth-service-1.0.0.jar ec2-user@seu-ip:/opt/app/

# Acessar EC2
ssh ec2-user@seu-ip

# Executar (sem definir DATASOURCE_USE_ENV_VARS, usa valor padrão false)
java -jar /opt/app/auth-service-1.0.0.jar

# Ou com variáveis de ambiente
export DATASOURCE_USE_ENV_VARS=false
export JWT_SECRET=sua-chave-longa
java -jar /opt/app/auth-service-1.0.0.jar
```

### Configuração em Docker (Produção)

```dockerfile
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/auth-service-1.0.0.jar app.jar

# NÃO definir DATASOURCE_USE_ENV_VARS (padrão é false)
# AWS SDK usará credenciais da IAM Role

EXPOSE 8088

ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
# Build
docker build -t minha-org/auth-service:latest .

# Run com IAM Role (ECS/Fargate)
docker run \
  -e JWT_SECRET=sua-chave-longa \
  -e SERVER_PORT=8088 \
  -p 8088:8088 \
  minha-org/auth-service:latest
```

### Configuração em ECS/Fargate

No arquivo `task-definition.json`:

```json
{
  "containerDefinitions": [
    {
      "name": "auth-service",
      "image": "123456789.dkr.ecr.us-east-1.amazonaws.com/auth-service:latest",
      "environment": [
        {
          "name": "JWT_SECRET",
          "value": "sua-chave-secreta-super-longa"
        },
        {
          "name": "REDIS_HOST",
          "value": "seu-redis.us-east-1.elasticache.amazonaws.com"
        },
        {
          "name": "REDIS_PORT",
          "value": "6379"
        }
      ],
      "taskRoleArn": "arn:aws:iam::123456789:role/ecsTaskRole",
      "executionRoleArn": "arn:aws:iam::123456789:role/ecsTaskExecutionRole"
    }
  ]
}
```

**Importante:** Não defina `DATASOURCE_USE_ENV_VARS` em produção - deixe como padrão `false`

---

## 🔍 Verificação

### Local

```bash
# 1. Verificar logs
docker-compose logs auth-service | grep "DataSourceConfig"

# Esperado:
# [LocalDataSourceConfig] ✅ DataSource criado a partir de variáveis de ambiente
# JDBC URL: jdbc:postgresql://postgres:5432/auth_db_dev
# Username: postgres
```

### Produção

```bash
# 1. Via SSH na EC2
tail -f /var/log/app/app.log | grep "DataSourceConfig"

# Esperado:
# [DataSourceConfig] ✅ DataSource criado a partir do AWS Secrets Manager
# JDBC URL: jdbc:postgresql://aurora-cluster.xxx.rds.amazonaws.com:5432/db
# Username: admin
```

---

## 🚨 Troubleshooting

### Erro: "Unable to load credentials from any of the providers"

**Causa:** `DATASOURCE_USE_ENV_VARS=false` mas não há credenciais AWS

**Solução:**
```bash
# Opção 1: Ambiente local - defina a variável
export DATASOURCE_USE_ENV_VARS=true

# Opção 2: Produção - certifique-se que IAM Role está anexada
aws sts get-caller-identity
```

### Erro: "Unable to authenticate AWS Secrets Manager"

**Causa:** IAM Role não tem permissão

**Solução:**
```bash
# Adicionar policy à IAM Role
aws iam put-role-policy \
  --role-name your-role \
  --policy-name SecretsManagerAccess \
  --policy-document '{
    "Version": "2012-10-17",
    "Statement": [
      {
        "Effect": "Allow",
        "Action": "secretsmanager:GetSecretValue",
        "Resource": "arn:aws:secretsmanager:us-east-1:*:secret:rds!cluster-*"
      }
    ]
  }'
```

### Erro: "Connection refused"

**Causa:** PostgreSQL não está acessível

**Solução:**
```bash
# Verificar conectividade
telnet postgres 5432  # ou seu-rds-endpoint

# Se usando Docker Compose, verificar se está rodando
docker-compose ps
```

---

## 📊 Comparação de Fluxos

### Desenvolvimento Local

```
docker-compose up
        ↓
DATASOURCE_USE_ENV_VARS=true
        ↓
Spring carrega LocalDataSourceConfig
        ↓
Lê DATABASE_* do docker-compose.yml
        ↓
HikariCP cria pool de conexões
        ↓
✅ Conexão com PostgreSQL local
```

### Produção AWS

```
java -jar app.jar
        ↓
DATASOURCE_USE_ENV_VARS=false (padrão)
        ↓
Spring carrega DataSourceConfig
        ↓
AwsSecretsManager usa AWS SDK
        ↓
SDK usa IAM Role (automático em EC2/ECS)
        ↓
Busca credenciais em Secrets Manager
        ↓
HikariCP cria pool de conexões
        ↓
✅ Conexão com RDS Aurora
```

---

## 🔐 Segurança

### ✅ Boas Práticas Implementadas

- ✅ Credenciais **nunca** são hardcoded
- ✅ Em local, variáveis de ambiente (docker-compose)
- ✅ Em produção, AWS Secrets Manager (rotação automática)
- ✅ IAM Roles (sem credenciais em arquivo)
- ✅ JWT Secret configurável por ambiente

### 🛡️ Próximos Passos

1. **Rotação de Credenciais:** Configure no AWS Secrets Manager
2. **Encryption:** Use AWS KMS para cifrar secrets
3. **Auditing:** Configure CloudTrail para rastrear acessos
4. **Vaults:** Considere usar HashiCorp Vault para gerenciamento centralizado

---

## 📞 Suporte

Para dúvidas ou problemas:
1. Verifique os logs: `docker-compose logs auth-service`
2. Teste conectividade ao banco: `telnet host port`
3. Valide credenciais em Secrets Manager
4. Verifique IAM Role e policies
