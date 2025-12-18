# VISÃO GERAL VISUAL
## Sistema Integrado de Autenticação, Autorização e Gestão de Usuários

---

## 🎯 EM UM SLIDE

```
┌─────────────────────────────────────────────────────────────────┐
│                                                                   │
│  SISTEMA: Autenticação + Autorização + Gestão de Usuários       │
│                                                                   │
│  TIPOS DE LOGIN:                                                 │
│  ├─ Tipo 1: user:pass (acesso geral)                            │
│  ├─ Tipo 2: user:pass:módulo (escopo módulo)                    │
│  └─ Tipo 3: user:pass:módulo:unidade (máximo escopo)            │
│                                                                   │
│  SEGURANÇA: JWT + BCrypt + Rate Limiting + Auditoria           │
│                                                                   │
│  TIMELINE: 10 semanas (Jan-Mar 2025)                            │
│  RECURSOS: 2-4 devs                                             │
│  INVESTIMENTO: 129 dev-days                                     │
│                                                                   │
│  STATUS: ✅ Pronto para Implementação                           │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📊 DIAGRAMA DE ARQUITETURA

```
┌────────────────────────────────────────────────────────────────────┐
│                          CLIENTE (Web/Mobile)                       │
│                                                                     │
│  POST /api/v1/auth/login                                          │
│  {username, password, [sistema], [unidade]}                       │
└──────────────────────────┬──────────────────────────────────────────┘
                           │
                           ▼
┌────────────────────────────────────────────────────────────────────┐
│                    SPRING SECURITY LAYER                            │
├────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  LoginController                                                    │
│    └─ Recebe login request                                        │
│    └─ Cria Token (Tipo 1, 2 ou 3)                                │
│                          │                                         │
│                          ▼                                         │
│  CustomAuthenticationProvider                                      │
│    └─ Detecta tipo de token                                       │
│    └─ Chama UserDetailsService                                    │
│                          │                                         │
│                          ▼                                         │
│  UserDetailsServiceImpl                                            │
│    └─ Busca usuário (BD)                                         │
│    └─ Valida senha (BCrypt)                                      │
│    └─ Valida módulo/unidade                                      │
│    └─ Carrega perfis e funcionalidades                           │
│                          │                                         │
│                          ▼                                         │
│  UserDetailsImpl                                                    │
│    └─ Implementa UserDetails                                      │
│    └─ Contém: id, username, perfis, autoridades                  │
│                          │                                         │
│                          ▼                                         │
│  TokenService                                                      │
│    └─ Gera JWT Token                                             │
│    └─ Claims: id, username, sistema, unidade, perfis             │
│    └─ Expiração: 24h                                             │
│                                                                     │
│  Resposta: 200 OK com JWT + dados do usuário                      │
│                                                                     │
└────────────────────────────────────────────────────────────────────┘
                           │
                           ▼
┌────────────────────────────────────────────────────────────────────┐
│                      DATABASE LAYER                                 │
├────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌────────────┐  ┌──────────┐  ┌─────────┐                        │
│  │  USUARIO   │  │ PERFIL   │  │ MODULO  │                        │
│  ├────────────┤  ├──────────┤  ├─────────┤                        │
│  │ id         │  │ id       │  │ id      │                        │
│  │ username   │  │ nome     │  │ descri. │                        │
│  │ cpf        │  │ descri.  │  │ status  │                        │
│  │ chave      │  │ status   │  └─────────┘                        │
│  │ status     │  │ modulo_id│                                      │
│  └────────────┘  └──────────┘                                      │
│       │                │                                           │
│       │                │           ┌──────────────────┐            │
│       │                │           │ FUNCIONALIDADE   │            │
│       │                │           ├──────────────────┤            │
│       │                │           │ id               │            │
│       │                │           │ nome             │            │
│       │                │           │ descri.          │            │
│       └─────┬──────────┘           └──────────────────┘            │
│             │                              ▲                       │
│      USUARIO_PERFIL                        │                      │
│      (N:N)                           PERFIL_FUNCIONALIDADE        │
│                                      (N:N)                        │
│  ┌──────────────┐         ┌──────────────────┐                   │
│  │  UNIDADE     │         │ USUARIO_HISTORICO│                   │
│  ├──────────────┤         ├──────────────────┤                   │
│  │ id           │         │ id               │                   │
│  │ sigla        │         │ usuario_id       │                   │
│  │ descri.      │         │ tipo_evento      │                   │
│  │ situacao     │         │ data             │                   │
│  │ superior_id  │         └──────────────────┘                   │
│  └──────────────┘                                                │
│                                                                     │
└────────────────────────────────────────────────────────────────────┘
```

---

## 🔐 FLUXO DE AUTENTICAÇÃO

```
REQUEST: POST /auth/login

┌─────────────────────────────────────┐
│  Tipo 1: {user, pass}               │
│  Tipo 2: {user, pass, sistema}      │
│  Tipo 3: {user, pass, sistema, un.} │
└──────────────┬──────────────────────┘
               │
               ▼
        ┌─────────────┐
        │ Validar     │
        │ Credenciais │
        └──────┬──────┘
               │
         ┌─────┴─────┐
         │           │
         ▼           ▼
      OK ✓        ✗ FALHA
         │           │
         │      ┌────────────┐
         │      │ Incrementar│
         │      │ Tentativas │
         │      └────────────┘
         │           │
         │      ┌────┴────┐
         │      │          │
         │      ▼          ▼
         │    < 3         = 3
         │      │         │
         │      │    ┌────────┐
         │      │    │Bloquear│
         │      │    │Usuário │
         │      │    └────────┘
         │      │         │
         │      └────┬────┘
         │           │
         ├───────────┤
         │           │
         ▼           ▼
      SUCESSO     401 ERRO
         │
         ├─────────────────────┐
         │                     │
         ▼                     ▼
    Carregar Perfis      Registrar
    e Funcionalidades    em Auditoria
    (baseado em tipo)
         │                     │
         │                     ▼
         │              USUARIO_HISTORICO
         │              .tipo_evento = LOGIN_FALHA
         │
         ▼
    Gerar JWT Token
    {
      id: 123,
      username: "user",
      sistema: "ADMIN" (se Tipo 2 ou 3),
      unidade: "TRE-MT" (se Tipo 3),
      perfis: ["ROLE_ADMIN"],
      exp: 1h
    }
         │
         ├─────────────────────┐
         │                     │
         ▼                     ▼
    Registrar             Response 200 OK
    em Auditoria          {
    .tipo_evento             token: "eyJ...",
      = LOGIN_SUCESSO        usuario: {...},
                             expiresIn: 3600
                           }
```

---

## 📈 TIMELINE - 4 FASES

```
JAN 2025                 FEV 2025                 MAR 2025
├─────────────────────────────────────────────────────────────┤
│  FASE 0       FASE 1              FASE 2        FASE 3       │
│  Setup        MVP                 Refinement    Production   │
│  2 sem        4 sem               2 sem         2 sem        │
│  18 dev-d     67 dev-d            29 dev-d      15 dev-d     │
├─────────────────────────────────────────────────────────────┤
│
│  Semana 1-2               Semana 7-8
│  ├─ Repo                  ├─ Testes e2e
│  ├─ BD local              ├─ Performance
│  ├─ CI/CD                 ├─ Security review
│  ├─ Spring boot           └─ Documentação
│  └─ Testes setup
│
│              Semana 3-6
│              ├─ Entidades JPA
│              ├─ 3 tipos login
│              ├─ JWT + Spring Security
│              ├─ CRUD usuários
│              └─ Auditoria
│
│                            Semana 9-10
│                            ├─ Monitoring (Prometheus)
│                            ├─ DR Plan
│                            ├─ Deploy K8s
│                            └─ Go-Live + suporte

                       ✅ PRODUCTION READY
```

---

## 🔑 3 TIPOS DE LOGIN

```
TIPO 1: Acesso Geral
┌─────────────────────────────────────┐
│ INPUT:  {user, pass}                │
├─────────────────────────────────────┤
│ Valida credenciais                  │
│ Carrega TODOS os perfis             │
│ Carrega TODAS as unidades           │
│ Sem escopo de módulo                │
├─────────────────────────────────────┤
│ JWT Payload:                        │
│  {                                  │
│   id: 123,                          │
│   username: "user",                 │
│   perfis: ["ADMIN", "RH", ...]      │
│  }                                  │
├─────────────────────────────────────┤
│ Uso: Portal geral, múltiplos módulos│
└─────────────────────────────────────┘


TIPO 2: Acesso por Módulo
┌─────────────────────────────────────┐
│ INPUT:  {user, pass, sistema}       │
├─────────────────────────────────────┤
│ Valida credenciais                  │
│ ✓ Valida acesso ao módulo           │
│ Carrega perfis DO módulo            │
│ Carrega unidades DO módulo          │
├─────────────────────────────────────┤
│ JWT Payload:                        │
│  {                                  │
│   id: 123,                          │
│   username: "user",                 │
│   sistema: "ADMIN",                 │
│   perfis: ["ROLE_ADMIN"] (do ADMIN) │
│  }                                  │
├─────────────────────────────────────┤
│ Uso: Acesso focado em um módulo     │
└─────────────────────────────────────┘


TIPO 3: Acesso Total Restrito
┌─────────────────────────────────────┐
│ INPUT:  {user, pass, sistema,unit.} │
├─────────────────────────────────────┤
│ Valida credenciais                  │
│ ✓ Valida acesso ao módulo           │
│ ✓ Valida acesso à unidade           │
│ Carrega perfis da combinação        │
│ Carrega funcionalidades específicas │
├─────────────────────────────────────┤
│ JWT Payload:                        │
│  {                                  │
│   id: 123,                          │
│   username: "user",                 │
│   sistema: "ADMIN",                 │
│   unidade: "TRE-MT",                │
│   perfis: ["ROLE_ADMIN_UNIDADE"],   │
│   funcs: ["CRIAR_USER", "EDIT..."]  │
│  }                                  │
├─────────────────────────────────────┤
│ Uso: Multi-tenancy, máxima segurança│
└─────────────────────────────────────┘
```

---

## 🛡️ CAMADAS DE SEGURANÇA

```
┌──────────────────────────────────────────────┐
│ 1. AUTENTICAÇÃO (Verificar quem é)           │
│    └─ Username + Password                    │
│    └─ Validação BCrypt (strength=10+)        │
│    └─ Histórico de tentativas                │
│    └─ Bloqueio após 3 falhas                 │
├──────────────────────────────────────────────┤
│ 2. AUTORIZAÇÃO (Verificar o que pode fazer) │
│    └─ RBAC (Role-Based Access Control)       │
│    └─ Perfis (ROLE_ADMIN, ROLE_RH, etc)     │
│    └─ Funcionalidades (granular)             │
│    └─ Escopo (Módulo + Unidade)             │
├──────────────────────────────────────────────┤
│ 3. RATE LIMITING (Evitar brute force)       │
│    └─ Max 5 logins / minuto / IP             │
│    └─ Resposta 429 Too Many Requests         │
├──────────────────────────────────────────────┤
│ 4. TOKEN (Credencial para requisições)       │
│    └─ JWT com expiração (24h)                │
│    └─ Refresh token (7 dias)                 │
│    └─ Assinado com HMAC256                   │
├──────────────────────────────────────────────┤
│ 5. AUDITORIA (Rastrear tudo)                 │
│    └─ Todos os logins registrados            │
│    └─ Todas as alterações registradas        │
│    └─ Imutável (não pode ser deletado)       │
│    └─ Retenção: 180+ dias                    │
├──────────────────────────────────────────────┤
│ 6. HTTPS (Criptografia em trânsito)          │
│    └─ TLS 1.2+                               │
│    └─ HSTS headers                           │
│    └─ Certificado válido                     │
└──────────────────────────────────────────────┘
```

---

## 📊 BANCO DE DADOS

```
                     ┌─────────────┐
                     │   USUARIO   │
                     ├─────────────┤
                     │ id          │◄─────┐
                     │ username    │      │
                     │ cpf         │      │
                     │ chave(hash) │      │
                     │ status      │      │
                     └──────┬──────┘      │
                            │ 1:N        │
                     ┌──────▼──────┐      │
                     │USUARIO_PERFIL
                     ├─────────────┤      │
                     │usuario_id◄──┼──────┘
                     │perfil_id │  │
                     │         │ N:1
                     └────┬────▼──┴──────┐
                          │             │
                       ┌──▼────────┐  ┌─▼──────────┐
                       │ PERFIL    │  │ MODULO     │
                       ├───────────┤  ├────────────┤
                       │ id        │  │ id         │
                       │ nome      │  │ descricao  │
                       │ modulo_id ◄──┤ status     │
                       └──┬────────┘  └────────────┘
                          │ N:M
                       ┌──▼──────────────┐
                       │PERFIL_FUNCTION. │
                       ├─────────────────┤
                       │perfil_id        │
                       │funcionalidade_id│
                       └────────┬────────┘
                                │ N:1
                        ┌───────▼────────┐
                        │FUNCIONALIDADE  │
                        ├────────────────┤
                        │ id             │
                        │ nome           │
                        │ descricao      │
                        └────────────────┘

    ┌────────────────┐              ┌─────────────────┐
    │    UNIDADE     │              │USUARIO_HISTORICO│
    ├────────────────┤              ├─────────────────┤
    │ id             │              │ id              │
    │ sigla          │              │ usuario_id      │
    │ descricao      │              │ tipo_evento     │
    │ superior_id◄───┼──┐           │ data            │
    │ (self-join)    │  │           └─────────────────┘
    └────────────────┘  │
                        │ 1:N (hierarquia)
                        │
                     (UNIDADE)
```

---

## 💾 USER STORIES - 4 FASES

```
FASE 0: SETUP (2 semanas, 18 dev-days)
├─ US-000.1: Repo + CI/CD
├─ US-000.2: Banco de Dados
├─ US-000.3: Spring Boot Base
├─ US-000.4: Documentação
└─ US-000.5: Estrutura de Testes

FASE 1: MVP (4 semanas, 67 dev-days)
├─ SPRINT 1 (Semana 3)
│  ├─ US-001: Entidades JPA
│  ├─ US-002: Repositories
│  ├─ US-003: DTOs + Mappers
│  └─ US-004: UserDetailsImpl
├─ SPRINT 2 (Semana 4)
│  ├─ US-005: CustomUserDetailsService
│  ├─ US-006: UserDetailsServiceImpl
│  ├─ US-007: Tokens Customizados
│  └─ US-008: CustomAuthenticationProvider
├─ SPRINT 3 (Semana 5)
│  ├─ US-009: TokenService (JWT)
│  ├─ US-010: AuthService
│  ├─ US-011: SecurityConfig
│  └─ US-012: JwtTokenFilter
└─ SPRINT 4 (Semana 6)
   ├─ US-013: LoginController
   ├─ US-014: UsuarioController
   └─ US-015: UsuarioService

FASE 2: REFINEMENT (2 semanas, 29 dev-days)
├─ US-016: Testes de Integração
├─ US-017: Testes de Performance
├─ US-018: Security Review
├─ US-019: Documentação
└─ US-020: Ajustes Pós-Testes

FASE 3: PRODUCTION (2 semanas, 15 dev-days)
├─ US-021: Monitoramento + Logging
├─ US-022: Disaster Recovery
├─ US-023: Deploy Produção
└─ US-024: Go-Live

TOTAL: 24 User Stories = 129 dev-days
```

---

## 🎯 KPIs DE SUCESSO

```
NEGÓCIO                          TÉCNICO
├─ 100% LGPD Compliance          ├─ < 500ms login (P95)
├─ 100% Migração Usuários        ├─ 99.9% disponibilidade
├─ 95%+ Taxa de Adoção           ├─ ≥80% test coverage
├─ 15min Onboarding              ├─ SonarQube ≥ A
│  (vs 4h antes)                 ├─ Zero vulns críticas
├─ 0 Incidentes Segurança        ├─ < 100ms permission check
│  (30 dias pós-launch)          └─ Cache hit > 80%
└─ 0 Violações Regulatórias
```

---

## 📋 DOCUMENTAÇÃO GERADA

```
┌──────────────────────────────────────────────────────────────┐
│ 1. RESUMO_EXECUTIVO.md (500 linhas)                          │
│    └─ O que, por quê, quando, por quanto                    │
│                                                               │
│ 2. REQUISITOS_SISTEMA_CADASTRO_USUARIOS.md (700 linhas)     │
│    └─ Especificação técnica detalhada                       │
│    └─ Fluxos de autenticação                                │
│    └─ Modelo de dados                                       │
│    └─ 20 endpoints REST                                     │
│                                                               │
│ 3. PRD_SISTEMA_AUTENTICACAO_USUARIOS.md (400 linhas)        │
│    └─ Decisões de negócio                                   │
│    └─ Personas e use cases                                  │
│    └─ Wireframes de telas                                   │
│    └─ KPIs e métricas                                       │
│                                                               │
│ 4. ROADMAP_EXECUCAO_COMPLETO.md (1000 linhas)              │
│    └─ 4 fases em detalhe                                    │
│    └─ 24 user stories                                       │
│    └─ Tasks breakdown                                       │
│    └─ Estimativas de tempo                                  │
│                                                               │
│ 5. INDICE_DOCUMENTACAO.md (400 linhas)                     │
│    └─ Índice de navegação                                   │
│    └─ Quick guides por perfil                               │
│    └─ Referências cruzadas                                  │
│                                                               │
│ 6. VISAO_GERAL_VISUAL.md (este arquivo)                    │
│    └─ Diagramas e fluxos visuais                            │
│    └─ Resumos executivos                                    │
│                                                               │
│ TOTAL: ~3000 linhas de documentação                         │
└──────────────────────────────────────────────────────────────┘
```

---

## ✅ CHECKLIST DE INÍCIO

```
ANTES DE COMEÇAR A IMPLEMENTAR:

□ Todos leram RESUMO_EXECUTIVO.md
□ Tech leads leram REQUISITOS_SISTEMA_CADASTRO_USUARIOS.md
□ Product managers leram PRD_SISTEMA_AUTENTICACAO_USUARIOS.md
□ Desenvolvedores entendem os 3 tipos de login
□ QA entende os acceptance criteria
□ Security team revisou e aprovou
□ Stakeholders assinaram PRD
□ Repositório Git criado
□ CI/CD pipeline configurado
□ BD local pronta
□ IDE configurada
□ Kick-off meeting realizado

DURANTE IMPLEMENTAÇÃO:

□ Usar ROADMAP para planejar sprints
□ Consultar REQUISITOS para detalhes técnicos
□ Validar contra Acceptance Criteria
□ Manter coverage de testes ≥ 80%
□ Registrar decisões em ADRs
□ Comunicar progresso regularmente
□ Resolver impedimentos rapidamente

ANTES DE GO-LIVE:

□ Testes de integração E2E completos
□ Load testing bem-sucedido
□ Security review aprovado
□ Documentação finalizada
□ Monitoramento pronto
□ DR plan testado
□ Runbooks preparados
□ Team treinado
□ Comunicação com usuários feita
```

---

## 🚀 PRÓXIMOS PASSOS (IMEDIATOS)

```
DIA 1-2: Leitura
├─ [ ] Todos leem RESUMO_EXECUTIVO.md
└─ [ ] Tech lead lê REQUISITOS_SISTEMA_CADASTRO_USUARIOS.md

DIA 3-4: Alinhamento
├─ [ ] Tech lead lê PRD_SISTEMA_AUTENTICACAO_USUARIOS.md
├─ [ ] Todos leem ROADMAP_EXECUCAO_COMPLETO.md (FASE 0)
└─ [ ] Meeting com stakeholders

DIA 5: Aprovação
├─ [ ] CTO aprova abordagem técnica
├─ [ ] Product manager aprova escopo
└─ [ ] Segurança aprova arquitetura

SEMANA 2: Começar FASE 0
├─ [ ] US-000.1: Setup Repo + CI/CD
├─ [ ] US-000.2: Configurar BD
├─ [ ] US-000.3: Spring Boot Base
├─ [ ] US-000.4: Documentação
└─ [ ] US-000.5: Estrutura Testes
```

---

**Data**: 2024-12-09
**Status**: ✅ COMPLETO
**Documentação**: 6 arquivos = ~3000 linhas
**Pronto para**: IMPLEMENTAÇÃO IMEDIATA

---

*Use INDICE_DOCUMENTACAO.md para navegação rápida*
