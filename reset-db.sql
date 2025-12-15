-- Script para resetar o banco de dados
-- Deletar o banco existente e recriar

DROP DATABASE IF EXISTS auth_db_dev;
CREATE DATABASE auth_db_dev 
    ENCODING 'UTF8'
    LC_COLLATE 'pt_BR.UTF-8'
    LC_CTYPE 'pt_BR.UTF-8'
    TEMPLATE template0;

-- Grant permissões
GRANT ALL PRIVILEGES ON DATABASE auth_db_dev TO postgres;
