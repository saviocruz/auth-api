-- ============================================================================
-- V3 - Criar Sequences para Auto-Incremento
-- ============================================================================
-- Sequences alinhadas com dados inseridos em V2
-- Contagem de inserts em V2:
--   - UNIDADE: 5 registros
--   - MODULO: 5 registros
--   - FUNCIONALIDADE: 12 registros
--   - PERFIL: 10 registros
--   - USUARIO: 8 registros
--   - USUARIO_HISTORICO: 8+ registros (1 por usuário)
-- ============================================================================

-- Sequence para MODULO (5 registros em V2, próximo ID = 6)
CREATE SEQUENCE IF NOT EXISTS auth.seq_modulo
    START WITH 6
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE auth.modulo ALTER COLUMN id SET DEFAULT nextval('auth.seq_modulo');

-- Sequence para FUNCIONALIDADE (12 registros em V2, próximo ID = 13)
CREATE SEQUENCE IF NOT EXISTS auth.seq_funcionalidade
    START WITH 13
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE auth.funcionalidade ALTER COLUMN id SET DEFAULT nextval('auth.seq_funcionalidade');

-- Sequence para PERFIL (10 registros em V2, próximo ID = 11)
CREATE SEQUENCE IF NOT EXISTS auth.seq_perfil
    START WITH 11
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE auth.perfil ALTER COLUMN id SET DEFAULT nextval('auth.seq_perfil');

-- Sequence para UNIDADE (5 registros em V2, próximo ID = 6)
CREATE SEQUENCE IF NOT EXISTS auth.seq_unidade
    START WITH 6
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE auth.unidade ALTER COLUMN id SET DEFAULT nextval('auth.seq_unidade');

-- Sequence para USUARIO (8 registros em V2, próximo ID = 9)
CREATE SEQUENCE IF NOT EXISTS auth.seq_usuario
    START WITH 9
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE auth.usuario ALTER COLUMN id SET DEFAULT nextval('auth.seq_usuario');

-- Sequence para USUARIO_HISTORICO (8 registros em V2, próximo ID = 9)
CREATE SEQUENCE IF NOT EXISTS auth.seq_usuario_historico
    START WITH 9
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE auth.usuario_historico ALTER COLUMN id SET DEFAULT nextval('auth.seq_usuario_historico');

-- ============================================================================
-- END OF MIGRATION V3
-- ============================================================================
