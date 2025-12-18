-- ============================================================================
-- FASE 0 - Initial Data for Authentication System
-- ============================================================================
-- Seed data for development and testing
-- ============================================================================

-- ============================================================================
-- 1. INSERT: UNIDADES (Organizational Structure)
-- ============================================================================

INSERT INTO auth.unidade (sigla, descricao, situacao, numero, email, telefone, endereco, cep, bairro, ordenacao)
VALUES
    ('PRESIDENCIA', 'Presidência da Instituição', 'ATIVO', '0001', 'presidencia@instituicao.com.br', '(11) 3000-0001', 'Avenida Principal, 1000', '01310-100', 'Centro', 1),
    ('DIRETORIA', 'Diretoria Geral', 'ATIVO', '0002', 'diretoria@instituicao.com.br', '(11) 3000-0002', 'Avenida Principal, 1000', '01310-100', 'Centro', 2),
    ('RH', 'Departamento de Recursos Humanos', 'ATIVO', '0003', 'rh@instituicao.com.br', '(11) 3000-0003', 'Avenida Principal, 1000', '01310-100', 'Centro', 3),
    ('TI', 'Departamento de Tecnologia da Informação', 'ATIVO', '0004', 'ti@instituicao.com.br', '(11) 3000-0004', 'Avenida Principal, 1000', '01310-100', 'Centro', 4),
    ('FINANCEIRO', 'Departamento Financeiro', 'ATIVO', '0005', 'financeiro@instituicao.com.br', '(11) 3000-0005', 'Avenida Principal, 1000', '01310-100', 'Centro', 5);

-- Atualizar relacionamentos hierárquicos
UPDATE auth.unidade SET unidade_superior_id = (SELECT id FROM auth.unidade WHERE sigla = 'PRESIDENCIA') WHERE sigla IN ('DIRETORIA', 'RH', 'TI', 'FINANCEIRO');

-- ============================================================================
-- 2. INSERT: MODULOS (System Modules)
-- ============================================================================

INSERT INTO auth.modulo (nome, descricao, email_responsavel, status)
VALUES
    ('ADMIN', 'Sistema de Administração Geral', 'admin@instituicao.com.br', 'ATIVO'),
    ('RECURSOS_HUMANOS', 'Sistema de Gestão de Recursos Humanos', 'rh@instituicao.com.br', 'ATIVO'),
    ('FINANCEIRO', 'Sistema de Gestão Financeira', 'financeiro@instituicao.com.br', 'ATIVO'),
    ('ACESSO_INFORMACAO', 'Sistema de Acesso à Informação (Lei de Acesso)', 'info@instituicao.com.br', 'ATIVO'),
    ('AUDITORIA', 'Sistema de Auditoria e Compliance', 'auditoria@instituicao.com.br', 'ATIVO');

-- ============================================================================
-- 3. INSERT: FUNCIONALIDADES (Granular Permissions)
-- ============================================================================

INSERT INTO auth.funcionalidade (nome, descricao, status)
VALUES
    ('CRIAR_USUARIO', 'Permissão para criar novos usuários', 'ATIVO'),
    ('EDITAR_USUARIO', 'Permissão para editar dados de usuários', 'ATIVO'),
    ('DELETAR_USUARIO', 'Permissão para deletar usuários', 'ATIVO'),
    ('VISUALIZAR_USUARIO', 'Permissão para visualizar dados de usuários', 'ATIVO'),
    ('GERENCIAR_PERFIS', 'Permissão para gerenciar perfis e roles', 'ATIVO'),
    ('GERENCIAR_UNIDADES', 'Permissão para gerenciar unidades organizacionais', 'ATIVO'),
    ('VISUALIZAR_AUDITORIA', 'Permissão para visualizar logs de auditoria', 'ATIVO'),
    ('EXPORTAR_DADOS', 'Permissão para exportar dados do sistema', 'ATIVO'),
    ('GERENCIAR_MODULOS', 'Permissão para gerenciar módulos do sistema', 'ATIVO'),
    ('ALTERAR_SENHA', 'Permissão para alterar sua própria senha', 'ATIVO'),
    ('VISUALIZAR_RELATORIOS', 'Permissão para visualizar relatórios', 'ATIVO'),
    ('GERAR_RELATORIOS', 'Permissão para gerar novos relatórios', 'ATIVO');

-- ============================================================================
-- 4. INSERT: PERFIS (Roles)
-- ============================================================================

-- ADMIN perfis
INSERT INTO auth.perfil (nome, descricao, status, id_modulo)
SELECT 'ROLE_ADMIN_GERAL', 'Administrador Geral do Sistema', 'ATIVO', id FROM auth.modulo WHERE nome = 'ADMIN';

INSERT INTO auth.perfil (nome, descricao, status, id_modulo)
SELECT 'ROLE_ADMIN_USUARIOS', 'Administrador de Usuários', 'ATIVO', id FROM auth.modulo WHERE nome = 'ADMIN';

INSERT INTO auth.perfil (nome, descricao, status, id_modulo)
SELECT 'ROLE_ADMIN_AUDITORIA', 'Administrador de Auditoria', 'ATIVO', id FROM auth.modulo WHERE nome = 'AUDITORIA';

-- RH perfis
INSERT INTO auth.perfil (nome, descricao, status, id_modulo)
SELECT 'ROLE_RH_GERENTE', 'Gerente de RH', 'ATIVO', id FROM auth.modulo WHERE nome = 'RECURSOS_HUMANOS';

INSERT INTO auth.perfil (nome, descricao, status, id_modulo)
SELECT 'ROLE_RH_CONSULTOR', 'Consultor de RH', 'ATIVO', id FROM auth.modulo WHERE nome = 'RECURSOS_HUMANOS';

-- FINANCEIRO perfis
INSERT INTO auth.perfil (nome, descricao, status, id_modulo)
SELECT 'ROLE_FINANCEIRO_GERENTE', 'Gerente de Financeiro', 'ATIVO', id FROM auth.modulo WHERE nome = 'FINANCEIRO';

INSERT INTO auth.perfil (nome, descricao, status, id_modulo)
SELECT 'ROLE_FINANCEIRO_ANALISTA', 'Analista de Financeiro', 'ATIVO', id FROM auth.modulo WHERE nome = 'FINANCEIRO';

-- ACESSO_INFORMACAO perfis
INSERT INTO auth.perfil (nome, descricao, status, id_modulo)
SELECT 'ROLE_ACESSO_GERENTE', 'Gerente de Acesso à Informação', 'ATIVO', id FROM auth.modulo WHERE nome = 'ACESSO_INFORMACAO';

-- Usuário comum (sem módulo específico, apenas acesso básico)
INSERT INTO auth.perfil (nome, descricao, status, id_modulo)
SELECT 'ROLE_USUARIO', 'Usuário Comum - Acesso Limitado', 'ATIVO', id FROM auth.modulo WHERE nome = 'ADMIN';

-- ============================================================================
-- 5. ASSOCIATE: PERFIS com FUNCIONALIDADES
-- ============================================================================

-- ROLE_ADMIN_GERAL (todas as permissões)
INSERT INTO auth.perfil_funcionalidade (id_perfil, id_funcionalidade)
SELECT p.id, f.id FROM auth.perfil p, auth.funcionalidade f
WHERE p.nome = 'ROLE_ADMIN_GERAL';

-- ROLE_ADMIN_USUARIOS (gerenciar usuários)
INSERT INTO auth.perfil_funcionalidade (id_perfil, id_funcionalidade)
SELECT p.id, f.id FROM auth.perfil p, auth.funcionalidade f
WHERE p.nome = 'ROLE_ADMIN_USUARIOS'
AND f.nome IN ('CRIAR_USUARIO', 'EDITAR_USUARIO', 'DELETAR_USUARIO', 'VISUALIZAR_USUARIO', 'GERENCIAR_PERFIS');

-- ROLE_RH_GERENTE (RH management)
INSERT INTO auth.perfil_funcionalidade (id_perfil, id_funcionalidade)
SELECT p.id, f.id FROM auth.perfil p, auth.funcionalidade f
WHERE p.nome = 'ROLE_RH_GERENTE'
AND f.nome IN ('VISUALIZAR_USUARIO', 'EDITAR_USUARIO', 'VISUALIZAR_RELATORIOS', 'GERAR_RELATORIOS', 'ALTERAR_SENHA');

-- ROLE_RH_CONSULTOR (RH read-only)
INSERT INTO auth.perfil_funcionalidade (id_perfil, id_funcionalidade)
SELECT p.id, f.id FROM auth.perfil p, auth.funcionalidade f
WHERE p.nome = 'ROLE_RH_CONSULTOR'
AND f.nome IN ('VISUALIZAR_USUARIO', 'VISUALIZAR_RELATORIOS', 'ALTERAR_SENHA');

-- ROLE_FINANCEIRO_GERENTE (Financeiro management)
INSERT INTO auth.perfil_funcionalidade (id_perfil, id_funcionalidade)
SELECT p.id, f.id FROM auth.perfil p, auth.funcionalidade f
WHERE p.nome = 'ROLE_FINANCEIRO_GERENTE'
AND f.nome IN ('VISUALIZAR_RELATORIOS', 'GERAR_RELATORIOS', 'EXPORTAR_DADOS', 'ALTERAR_SENHA');

-- ROLE_FINANCEIRO_ANALISTA (Financeiro read-only)
INSERT INTO auth.perfil_funcionalidade (id_perfil, id_funcionalidade)
SELECT p.id, f.id FROM auth.perfil p, auth.funcionalidade f
WHERE p.nome = 'ROLE_FINANCEIRO_ANALISTA'
AND f.nome IN ('VISUALIZAR_RELATORIOS', 'ALTERAR_SENHA');

-- ROLE_USUARIO (basic user)
INSERT INTO auth.perfil_funcionalidade (id_perfil, id_funcionalidade)
SELECT p.id, f.id FROM auth.perfil p, auth.funcionalidade f
WHERE p.nome = 'ROLE_USUARIO'
AND f.nome IN ('VISUALIZAR_USUARIO', 'ALTERAR_SENHA');

-- ============================================================================
-- 6. INSERT: USUARIOS (Test Users - Senhas em BCrypt)
-- ============================================================================
-- Nota: Todas as senhas neste arquivo são para DESENVOLVIMENTO APENAS
-- Senhas: admin123, usuario123, rh123, financeiro123

INSERT INTO auth.usuario (username, cpf, nome, email, chave, matricula, status, alterar)
VALUES
    -- Admin user (senha: admin123 -> BCrypt)
    ('admin', '12345678901234', 'Administrador Sistema', 'admin@instituicao.com.br',
     '$2a$10$6KVhDcd2dYPs3fTz3lamC.meKclmBO395suPexcGEVBZl4SKT8C9a', 'MAT001', 'ATIVO', FALSE),

    -- RH Manager (senha: rh123)
    ('rh_manager', '98765432101234', 'Gerente RH', 'rh.manager@instituicao.com.br',
     '$2a$10$6KVhDcd2dYPs3fTz3lamC.meKclmBO395suPexcGEVBZl4SKT8C9a', 'MAT002', 'ATIVO', FALSE),

    -- RH Consultant (senha: usuario123)
    ('rh_consultant', '11122233344455', 'Consultor RH', 'rh.consultant@instituicao.com.br',
     '$2a$10$6KVhDcd2dYPs3fTz3lamC.meKclmBO395suPexcGEVBZl4SKT8C9a', 'MAT003', 'ATIVO', FALSE),

    -- Finance Manager (senha: financeiro123)
    ('finance_manager', '55566677788899', 'Gerente Financeiro', 'finance.manager@instituicao.com.br',
     '$2a$10$6KVhDcd2dYPs3fTz3lamC.meKclmBO395suPexcGEVBZl4SKT8C9a', 'MAT004', 'ATIVO', FALSE),

    -- Finance Analyst (senha: usuario123)
    ('finance_analyst', '99988877766655', 'Analista Financeiro', 'finance.analyst@instituicao.com.br',
     '$2a$10$6KVhDcd2dYPs3fTz3lamC.meKclmBO395suPexcGEVBZl4SKT8C9a', 'MAT005', 'ATIVO', FALSE),

    -- Regular user (senha: usuario123)
    ('usuario', '44433322211100', 'Usuário Comum', 'usuario@instituicao.com.br',
     '$2a$10$6KVhDcd2dYPs3fTz3lamC.meKclmBO395suPexcGEVBZl4SKT8C9a', 'MAT006', 'ATIVO', FALSE),

    -- Inactive user (senha: usuario123)
    ('usuario_inativo', '77788899900011', 'Usuário Inativo', 'usuario.inativo@instituicao.com.br',
     '$2a$10$6KVhDcd2dYPs3fTz3lamC.meKclmBO395suPexcGEVBZl4SKT8C9a', 'MAT007', 'INATIVO', FALSE),

    -- Blocked user (senha: usuario123)
    ('usuario_bloqueado', '22233344455566', 'Usuário Bloqueado', 'usuario.bloqueado@instituicao.com.br',
     '$2a$10$6KVhDcd2dYPs3fTz3lamC.meKclmBO395suPexcGEVBZl4SKT8C9a', 'MAT008', 'BLOQUEADO', TRUE);

-- ============================================================================
-- 7. ASSOCIATE: USUARIOS com PERFIS
-- ============================================================================

-- Admin user - ROLE_ADMIN_GERAL
INSERT INTO auth.usuario_perfil (id_usuario, id_perfil)
SELECT u.id, p.id FROM auth.usuario u, auth.perfil p
WHERE u.username = 'admin' AND p.nome = 'ROLE_ADMIN_GERAL';

-- RH Manager - ROLE_RH_GERENTE
INSERT INTO auth.usuario_perfil (id_usuario, id_perfil)
SELECT u.id, p.id FROM auth.usuario u, auth.perfil p
WHERE u.username = 'rh_manager' AND p.nome = 'ROLE_RH_GERENTE';

-- RH Consultant - ROLE_RH_CONSULTOR
INSERT INTO auth.usuario_perfil (id_usuario, id_perfil)
SELECT u.id, p.id FROM auth.usuario u, auth.perfil p
WHERE u.username = 'rh_consultant' AND p.nome = 'ROLE_RH_CONSULTOR';

-- Finance Manager - ROLE_FINANCEIRO_GERENTE
INSERT INTO auth.usuario_perfil (id_usuario, id_perfil)
SELECT u.id, p.id FROM auth.usuario u, auth.perfil p
WHERE u.username = 'finance_manager' AND p.nome = 'ROLE_FINANCEIRO_GERENTE';

-- Finance Analyst - ROLE_FINANCEIRO_ANALISTA
INSERT INTO auth.usuario_perfil (id_usuario, id_perfil)
SELECT u.id, p.id FROM auth.usuario u, auth.perfil p
WHERE u.username = 'finance_analyst' AND p.nome = 'ROLE_FINANCEIRO_ANALISTA';

-- Regular user - ROLE_USUARIO
INSERT INTO auth.usuario_perfil (id_usuario, id_perfil)
SELECT u.id, p.id FROM auth.usuario u, auth.perfil p
WHERE u.username = 'usuario' AND p.nome = 'ROLE_USUARIO';

-- Inactive user - ROLE_USUARIO
INSERT INTO auth.usuario_perfil (id_usuario, id_perfil)
SELECT u.id, p.id FROM auth.usuario u, auth.perfil p
WHERE u.username = 'usuario_inativo' AND p.nome = 'ROLE_USUARIO';

-- Blocked user - ROLE_USUARIO
INSERT INTO auth.usuario_perfil (id_usuario, id_perfil)
SELECT u.id, p.id FROM auth.usuario u, auth.perfil p
WHERE u.username = 'usuario_bloqueado' AND p.nome = 'ROLE_USUARIO';

-- ============================================================================
-- 8. ASSOCIATE: USUARIOS com UNIDADES
-- ============================================================================

-- Admin - todas as unidades
INSERT INTO auth.usuario_unidade (id_usuario, id_unidade)
SELECT u.id, un.id FROM auth.usuario u, auth.unidade un
WHERE u.username = 'admin';

-- RH Manager - RH e DIRETORIA
INSERT INTO auth.usuario_unidade (id_usuario, id_unidade)
SELECT u.id, un.id FROM auth.usuario u, auth.unidade un
WHERE u.username = 'rh_manager' AND un.sigla IN ('RH', 'DIRETORIA');

-- RH Consultant - RH
INSERT INTO auth.usuario_unidade (id_usuario, id_unidade)
SELECT u.id, un.id FROM auth.usuario u, auth.unidade un
WHERE u.username = 'rh_consultant' AND un.sigla = 'RH';

-- Finance Manager - FINANCEIRO e DIRETORIA
INSERT INTO auth.usuario_unidade (id_usuario, id_unidade)
SELECT u.id, un.id FROM auth.usuario u, auth.unidade un
WHERE u.username = 'finance_manager' AND un.sigla IN ('FINANCEIRO', 'DIRETORIA');

-- Finance Analyst - FINANCEIRO
INSERT INTO auth.usuario_unidade (id_usuario, id_unidade)
SELECT u.id, un.id FROM auth.usuario u, auth.unidade un
WHERE u.username = 'finance_analyst' AND un.sigla = 'FINANCEIRO';

-- Regular user - PRESIDENCIA
INSERT INTO auth.usuario_unidade (id_usuario, id_unidade)
SELECT u.id, un.id FROM auth.usuario u, auth.unidade un
WHERE u.username = 'usuario' AND un.sigla = 'PRESIDENCIA';

-- ============================================================================
-- 9. INITIAL AUDIT ENTRIES
-- ============================================================================

-- Log de criação dos usuários
INSERT INTO auth.usuario_historico (id_usuario, tipo_evento, descricao, modulo, unidade)
SELECT u.id, 'CRIACAO'::auth.tipo_evento_enum, 'Usuário criado durante seed data', 'ADMIN', 'PRESIDENCIA'
FROM auth.usuario u;

-- ============================================================================
-- END OF MIGRATION V2
-- ============================================================================
