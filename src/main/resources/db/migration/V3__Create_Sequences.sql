-- ============================================================================
-- V3 - Criar Sequences para Auto-Incremento
-- ============================================================================

-- Sequence para USUARIO
CREATE SEQUENCE IF NOT EXISTS auth.seq_usuario
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- Sequence para PERFIL
CREATE SEQUENCE IF NOT EXISTS auth.seq_perfil
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- Sequence para MODULO
CREATE SEQUENCE IF NOT EXISTS auth.seq_modulo
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- Sequence para FUNCIONALIDADE
CREATE SEQUENCE IF NOT EXISTS auth.seq_funcionalidade
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- Sequence para UNIDADE
CREATE SEQUENCE IF NOT EXISTS auth.seq_unidade
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- Sequence para USUARIO_HISTORICO
CREATE SEQUENCE IF NOT EXISTS auth.seq_usuario_historico
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
