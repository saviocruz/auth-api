# Correção do Erro Flyway: "type already exists"

## Problema Identificado

O Flyway estava lançando o seguinte erro ao executar em ambientes AWS:

```
ERROR: type "ativo_inativo_enum" already exists
Caused by: org.postgresql.util.PSQLException
```

## Por que ocorre apenas em AWS?

O erro ocorre em AWS porque:

1. **Banco de dados em subnet privada**: O banco está separado da aplicação, permitindo múltiplas instâncias acessarem o mesmo banco
2. **Múltiplas instâncias da aplicação**: Quando há scaling horizontal, múltiplas VMs tentam executar as migrações simultaneamente
3. **Validação ativa em produção**: A configuração `validate-on-migrate: true` em [`application-prod.yml`](src/main/resources/application-prod.yml:34) força validação rigorosa
4. **Sem idempotência**: O script original não era idempotente - não podia ser executado múltiplas vezes com segurança

Em ambiente local com Docker, o banco é exclusivo do container, então não há conflito.

## Causa Raiz

No arquivo [`V1__Create_Initial_Schema.sql`](src/main/resources/db/migration/V1__Create_Initial_Schema.sql), os tipos ENUM eram criados sem proteção contra duplicação:

```sql
-- ❌ ANTES (Incorreto)
CREATE TYPE auth.ativo_inativo_enum AS ENUM ('ATIVO', 'INATIVO', 'BLOQUEADO');
CREATE TYPE auth.tipo_evento_enum AS ENUM (...);
```

Quando o Flyway tenta executar novamente (em cenários de retry, múltiplas instâncias ou redeployment), o PostgreSQL lança um erro porque o tipo já existe no banco de dados.

## Solução Implementada

Envolvemos os comandos `CREATE TYPE` em blocos `DO` com tratamento de exceção `duplicate_object`:

```sql
-- ✅ DEPOIS (Correto)
DO $$ BEGIN
    CREATE TYPE auth.ativo_inativo_enum AS ENUM ('ATIVO', 'INATIVO', 'BLOQUEADO');
EXCEPTION WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE auth.tipo_evento_enum AS ENUM (
        'LOGIN_SUCESSO',
        'LOGIN_FALHA',
        'CRIACAO',
        'EDICAO',
        'DELECAO',
        'BLOQUEIO',
        'DESBLOQUEIO',
        'ALTERACAO_SENHA',
        'RESET_SENHA',
        'ALTERACAO_PERFIS',
        'ALTERACAO_UNIDADES',
        'LOGOUT'
    );
EXCEPTION WHEN duplicate_object THEN null;
END $$;
```

## Como Funciona

1. **Bloco DO**: Executa código PL/pgSQL anônimo
2. **BEGIN...END**: Define o escopo do bloco
3. **EXCEPTION WHEN duplicate_object**: Captura o erro quando o tipo já existe
4. **THEN null**: Ignora silenciosamente o erro (não faz nada)

## Benefícios

- ✅ Permite re-execução segura do script de migração
- ✅ Funciona em ambientes com múltiplas instâncias
- ✅ Compatível com retry automático do Flyway
- ✅ Não afeta dados existentes
- ✅ Segue o padrão já utilizado no V3 para sequences (`CREATE SEQUENCE IF NOT EXISTS`)

## Arquivos Modificados

- [`src/main/resources/db/migration/V1__Create_Initial_Schema.sql`](src/main/resources/db/migration/V1__Create_Initial_Schema.sql) - Linhas 17-38

## Próximos Passos

1. Fazer deploy da aplicação com a migração corrigida
2. O Flyway executará a migração V1 novamente sem erros
3. Tipos ENUM já existentes serão ignorados silenciosamente
4. Migrações subsequentes (V2, V3) funcionarão normalmente

## Configurações Flyway Relevantes

Em [`application-prod.yml`](src/main/resources/application-prod.yml):

```yaml
flyway:
  enabled: true
  locations: classpath:db/migration
  baseline-on-migrate: true
  validate-on-migrate: true  # ← Força validação rigorosa em produção
  out-of-order: false
```

A configuração `validate-on-migrate: true` é importante para garantir integridade, mas exige que os scripts sejam idempotentes.

## Referências PostgreSQL

- [CREATE TYPE Documentation](https://www.postgresql.org/docs/current/sql-createtype.html)
- [PL/pgSQL Exception Handling](https://www.postgresql.org/docs/current/plpgsql-control-structures.html#PLPGSQL-EXCEPTION)
- [PostgreSQL Error Codes](https://www.postgresql.org/docs/current/errcodes-appendix.html)
