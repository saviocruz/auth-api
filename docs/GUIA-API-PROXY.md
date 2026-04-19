# Guia: Separando URLs de Backend (IP Interno vs Público)

## Problema Original

Você tinha a mesma URL (`NEXT_PUBLIC_API_URL`) sendo usada tanto no servidor quanto no cliente:

```
❌ NextAuth no servidor → Tenta usar "http://IP_PUBLICO" (erro: não consegue resolver)
❌ Axios no cliente → Tenta usar "http://IP_PRIVADO" (erro: não consegue resolver)
```

## Solução Implementada

Agora você tem uma arquitetura de **proxy** usando API Routes do Next.js:

```
✅ NextAuth (Servidor) → INTERNAL_API_URL (IP interno) → seu-backend:8080
✅ Axios (Cliente) → NEXT_PUBLIC_API_URL (IP público) → seu-nextjs:3000/api/proxy/* → INTERNAL_API_URL (IP interno) → seu-backend:8080
```

## Mudanças Realizadas

### 1. Variáveis de Ambiente (`.env.local`)

```env
# URL pública para o cliente (browser) - acessa o proxy
NEXT_PUBLIC_API_URL=http://localhost:3000/api/proxy

# URL interna para o servidor (Node.js/Edge Runtime)
INTERNAL_API_URL=http://localhost:8080

# NextAuth
NEXTAUTH_URL=http://localhost:3000
NEXTAUTH_SECRET=sua-chave-secreta-32-caracteres-min
```

### 2. API Routes de Proxy

#### `/src/app/api/proxy/auth/login/route.ts`
Proxy para o endpoint de login. O cliente chama `POST /api/proxy/auth/login`, e o servidor encaminha para o backend interno.

#### `/src/app/api/proxy/auth/refresh/route.ts`
Proxy para renovação de tokens.

**Como adicionar mais endpoints:**

Crie novas rotas seguindo o padrão:
```
/src/app/api/proxy/[recurso]/[acao]/route.ts
```

Exemplo para listar usuários:
```typescript
// /src/app/api/proxy/usuarios/route.ts
export async function GET(request: NextRequest) {
    const backendUrl = `${process.env.INTERNAL_API_URL}/api/v1/usuarios`
    const response = await fetch(backendUrl, {
        method: "GET",
        headers: { "Content-Type": "application/json" },
    })
    return NextResponse.json(await response.json())
}
```

### 3. Configuração do NextAuth (`/src/lib/auth.ts`)

**Mudança Principal:** Usar `INTERNAL_API_URL` em vez de `NEXT_PUBLIC_API_URL`

- ✅ `authorize()` chama `${process.env.INTERNAL_API_URL}/api/v1/auth/login`
- ✅ `refreshAccessToken()` chama `${process.env.INTERNAL_API_URL}/api/v1/auth/refresh`

### 4. Cliente API (`/src/lib/api-client.ts`)

Novo módulo para usar no cliente. Configura o axios para usar o proxy público:

```typescript
import { apiClient } from '@/lib/api-client'

// No seu componente cliente
const { data } = await apiClient.get('/usuarios')
```

## Fluxo de Autenticação Detalhado

### Login
```
1. Cliente submete formulário com credenciais
2. NextAuth (servidor) → authorize() chamada
3. authorize() usa INTERNAL_API_URL → backend (IP interno)
4. Backend retorna { token, refreshToken, usuario }
5. NextAuth armazena em cookie httpOnly (seguro)
6. Cliente recebe sessão
```

### Chamadas de API Subsequentes
```
1. Cliente usa apiClient.get('/usuarios')
2. Request vai para: http://localhost:3000/api/proxy/usuarios
3. Proxy (/src/app/api/proxy/usuarios/route.ts) intercepta
4. Proxy chama: http://localhost:8080/api/v1/usuarios (IP interno)
5. Backend retorna dados
6. Proxy passa para cliente
```

### Renovação de Token
```
1. NextAuth detecta token expirado
2. Chama refreshAccessToken()
3. refreshAccessToken() usa INTERNAL_API_URL
4. Backend retorna novo token
5. NextAuth atualiza sessão
6. Cliente continua autenticado
```

## Rotas de Proxy Criadas

Conforme você adiciona endpoints no backend, crie os respectivos proxies:

```bash
src/app/api/proxy/
├── auth/
│   ├── login/route.ts          ✅ Criado
│   └── refresh/route.ts        ✅ Criado
├── usuarios/
│   ├── route.ts                ✅ GET, POST
│   └── [id]/route.ts           ✅ GET, PUT, DELETE
├── perfis/
│   ├── route.ts                ✅ GET, POST
│   └── [id]/route.ts           ✅ GET, PUT, DELETE
└── auditoria/
    └── route.ts                ✅ GET
```

## Variáveis de Ambiente em Produção

### Desenvolvimento Local
```env
NEXT_PUBLIC_API_URL=http://localhost:3000/api/proxy
INTERNAL_API_URL=http://localhost:8080
```

### Staging/Produção
```env
NEXT_PUBLIC_API_URL=https://seu-dominio.com/api/proxy
INTERNAL_API_URL=http://seu-backend-interno:8080
# Ou se o backend estiver em outro serviço interno da rede
INTERNAL_API_URL=http://backend-service:8080
```

## Segurança

✅ **IP interno nunca é exposto ao cliente**
- `INTERNAL_API_URL` é `process.env` (server-side only)
- Cliente só consegue acessar via proxy público

✅ **Tokens JWT em httpOnly cookies**
- NextAuth armazena automaticamente
- Não tem CSRF token

✅ **Validação de requisições**
- Você pode adicionar validação no proxy antes de encaminhar

## Exemplo: Adicionar Proxy para Usuários

```typescript
// /src/app/api/proxy/usuarios/route.ts
import { NextRequest, NextResponse } from "next/server"

export async function GET(request: NextRequest) {
    try {
        const backendUrl = `${process.env.INTERNAL_API_URL}/api/v1/usuarios`
        const response = await fetch(backendUrl, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                // Passar token se necessário
                "Authorization": request.headers.get("authorization") || "",
            },
        })

        const data = await response.json()
        return NextResponse.json(data, { status: response.status })
    } catch (error) {
        console.error("Erro no proxy de usuários:", error)
        return NextResponse.json(
            { error: "Falha ao buscar usuários" },
            { status: 500 }
        )
    }
}

export async function POST(request: NextRequest) {
    try {
        const body = await request.json()
        const backendUrl = `${process.env.INTERNAL_API_URL}/api/v1/usuarios`
        
        const response = await fetch(backendUrl, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": request.headers.get("authorization") || "",
            },
            body: JSON.stringify(body),
        })

        const data = await response.json()
        return NextResponse.json(data, { status: response.status })
    } catch (error) {
        console.error("Erro ao criar usuário:", error)
        return NextResponse.json(
            { error: "Falha ao criar usuário" },
            { status: 500 }
        )
    }
}
```

No cliente:
```typescript
import { apiClient } from '@/lib/api-client'

// GET
const { data: usuarios } = await apiClient.get('/usuarios')

// POST
const { data: novoUsuario } = await apiClient.post('/usuarios', {
    nome: 'João',
    email: 'joao@example.com'
})
```

## Próximos Passos

1. ✅ Revisar e testar o login
2. ✅ Criar proxies para os endpoints de usuários, perfis, auditoria
3. ✅ Atualizar hooks para usar as rotas de proxy corretas
4. ⏳ Testar chamadas de API com o novo apiClient
5. ⏳ Configurar variáveis de ambiente para produção
6. ⏳ Adicionar validações/autorizações no proxy se necessário

## Troubleshooting

### "Erro ao conectar com backend"
- Verifique se `INTERNAL_API_URL` está correto
- Teste: `curl http://localhost:8080/api/v1/auth/login`

### "CORS error no cliente"
- Isso significa que o cliente está tentando chamar o backend direto
- Use `apiClient` em vez de `axios` ou `fetch` direto

### "Token não está sendo renovado"
- Verifique se `refreshAccessToken()` está usando `INTERNAL_API_URL`
- Verifique o endpoint de refresh no seu backend

## Referências

- [NextAuth Credentials Provider](https://next-auth.js.org/providers/credentials)
- [Next.js API Routes](https://nextjs.org/docs/app/building-your-application/routing/route-handlers)
- [Environment Variables in Next.js](https://nextjs.org/docs/basic-features/environment-variables)
