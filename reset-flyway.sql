-- Script para resetar o histórico do Flyway
-- Execute isso no seu banco de dados PostgreSQL

-- Limpar o histórico do Flyway
DELETE FROM flyway_schema_history WHERE success = true OR success = false;

-- Verificar se foi limpo
SELECT * FROM flyway_schema_history;
