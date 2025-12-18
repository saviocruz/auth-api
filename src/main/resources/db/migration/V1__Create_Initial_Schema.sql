-- ============================================================================
-- FASE 0 - Database Schema for Authentication System
-- ============================================================================
-- Sistema de Autenticação com 3 Tipos de Login
-- Criado em: 2025-12-09
-- Versão: 1.0.0
-- ============================================================================

-- ============================================================================
-- 1. Criar SCHEMA
-- ============================================================================
CREATE SCHEMA IF NOT EXISTS auth;

-- ============================================================================
-- 2. ENUM TYPES (PostgreSQL)
-- ============================================================================
CREATE TYPE auth.ativo_inativo_enum AS ENUM ('ATIVO', 'INATIVO', 'BLOQUEADO');
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

-- ============================================================================
-- 3. TABLE: UNIDADE (Organizational Units with Hierarchy)
-- ============================================================================
CREATE TABLE auth.unidade (
    id BIGSERIAL PRIMARY KEY,
    sigla VARCHAR(50) NOT NULL UNIQUE,
    descricao VARCHAR(500),
    situacao VARCHAR(50) NOT NULL DEFAULT 'ATIVO',
    numero VARCHAR(10),
    email VARCHAR(255),
    telefone VARCHAR(20),
    endereco VARCHAR(255),
    cep VARCHAR(10),
    bairro VARCHAR(100),
    ordenacao INTEGER DEFAULT 0,
    unidade_superior_id BIGINT REFERENCES auth.unidade(id) ON DELETE SET NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_unidade_sigla ON auth.unidade(sigla);
CREATE INDEX idx_unidade_situacao ON auth.unidade(situacao);
CREATE INDEX idx_unidade_superior ON auth.unidade(unidade_superior_id);

-- ============================================================================
-- 4. TABLE: MODULO (System Modules/Subsystems)
-- ============================================================================
CREATE TABLE auth.modulo (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    descricao VARCHAR(500),
    email_responsavel VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'ATIVO',
    data_cadastro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0
);

CREATE INDEX idx_modulo_nome ON auth.modulo(nome);
CREATE INDEX idx_modulo_status ON auth.modulo(status);

-- ============================================================================
-- 5. TABLE: FUNCIONALIDADE (Granular Permissions)
-- ============================================================================
CREATE TABLE auth.funcionalidade (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    descricao VARCHAR(500),
    status VARCHAR(50) NOT NULL DEFAULT 'ATIVO'
);

CREATE INDEX idx_funcionalidade_nome ON auth.funcionalidade(nome);
CREATE INDEX idx_funcionalidade_status ON auth.funcionalidade(status);
-- ============================================================================
-- 6. TABLE: PERFIL (Roles)
-- ============================================================================
CREATE TABLE auth.perfil (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    descricao VARCHAR(500),
    status VARCHAR(50) NOT NULL DEFAULT 'ATIVO',
    id_modulo BIGINT NOT NULL REFERENCES auth.modulo(id) ON DELETE RESTRICT,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_perfil_nome ON auth.perfil(nome);
CREATE INDEX idx_perfil_modulo ON auth.perfil(id_modulo);
CREATE INDEX idx_perfil_status ON auth.perfil(status);

-- ============================================================================
-- 7. TABLE: PERFIL_FUNCIONALIDADE (Many-to-Many)
-- ============================================================================
CREATE TABLE auth.perfil_funcionalidade (
    id_perfil BIGINT NOT NULL REFERENCES auth.perfil(id) ON DELETE CASCADE,
    id_funcionalidade BIGINT NOT NULL REFERENCES auth.funcionalidade(id) ON DELETE CASCADE,
    PRIMARY KEY (id_perfil, id_funcionalidade)
);

CREATE INDEX idx_perf_func_funcionalidade ON auth.perfil_funcionalidade(id_funcionalidade);

-- ============================================================================
-- 8. TABLE: USUARIO (Users)
-- ============================================================================
CREATE TABLE auth.usuario (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    cpf VARCHAR(14) UNIQUE,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE,
    chave VARCHAR(255) NOT NULL,
    matricula VARCHAR(50),
    status VARCHAR(50) NOT NULL DEFAULT 'ATIVO',
    alterar BOOLEAN DEFAULT FALSE,
    tentativas_falhas INTEGER DEFAULT 0,
    data_cadastro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_ultimo_login TIMESTAMP,
    versao INTEGER DEFAULT 0
);

CREATE INDEX idx_usuario_username ON auth.usuario(username);
CREATE INDEX idx_usuario_cpf ON auth.usuario(cpf);
CREATE INDEX idx_usuario_email ON auth.usuario(email);
CREATE INDEX idx_usuario_status ON auth.usuario(status);

-- ============================================================================
-- 9. TABLE: USUARIO_PERFIL (Many-to-Many)
-- ============================================================================
CREATE TABLE auth.usuario_perfil (
    id_usuario BIGINT NOT NULL REFERENCES auth.usuario(id) ON DELETE CASCADE,
    id_perfil BIGINT NOT NULL REFERENCES auth.perfil(id) ON DELETE CASCADE,
    PRIMARY KEY (id_usuario, id_perfil)
);

CREATE INDEX idx_usuario_perfil_perfil ON auth.usuario_perfil(id_perfil);

-- ============================================================================
-- 10. TABLE: USUARIO_UNIDADE (Many-to-Many)
-- ============================================================================
CREATE TABLE auth.usuario_unidade (
    id_usuario BIGINT NOT NULL REFERENCES auth.usuario(id) ON DELETE CASCADE,
    id_unidade BIGINT NOT NULL REFERENCES auth.unidade(id) ON DELETE CASCADE,
    PRIMARY KEY (id_usuario, id_unidade)
);

CREATE INDEX idx_usuario_unidade_unidade ON auth.usuario_unidade(id_unidade);
-- ============================================================================
-- 11. TABLE: USUARIO_HISTORICO (Audit Trail - Immutable)
-- ============================================================================
CREATE TABLE auth.usuario_historico (
    id BIGSERIAL PRIMARY KEY,
    id_usuario BIGINT NOT NULL REFERENCES auth.usuario(id) ON DELETE RESTRICT,
    tipo_evento VARCHAR(20)  NOT NULL,
    descricao VARCHAR(500),
    data_sistema TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modulo VARCHAR(100),
    unidade VARCHAR(100),
    ip_address VARCHAR(50),
    user_agent VARCHAR(255)
);

CREATE INDEX idx_historico_usuario ON auth.usuario_historico(id_usuario);
CREATE INDEX idx_historico_tipo_evento ON auth.usuario_historico(tipo_evento);
CREATE INDEX idx_historico_data ON auth.usuario_historico(data_sistema);
CREATE INDEX idx_historico_modulo ON auth.usuario_historico(modulo);
CREATE INDEX idx_historico_usuario_data ON auth.usuario_historico(id_usuario, data_sistema DESC);

-- ============================================================================
-- 12. COMMENTS
-- ============================================================================
COMMENT ON SCHEMA auth IS 'Authentication and User Management Schema';
COMMENT ON TABLE auth.usuario IS 'Users table with support for 3 login types';
COMMENT ON TABLE auth.usuario_historico IS 'Immutable audit trail for compliance and LGPD';
COMMENT ON COLUMN auth.usuario.status IS 'ATIVO, INATIVO, or BLOQUEADO';
COMMENT ON COLUMN auth.usuario_historico.tipo_evento IS 'Event type for audit trail';

-- ============================================================================
-- END OF MIGRATION V1
-- ============================================================================
