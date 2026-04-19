# Atualização: Método atualizarUsuario com Suporte a Perfis

## Mudança Implementada

O método [`atualizarUsuario`](src/main/java/br/lar/auth/service/UsuarioServiceImpl.java:154) em `UsuarioServiceImpl.java` foi atualizado para salvar os perfis fornecidos no DTO, com suporte a remoção de perfis não presentes na lista.

## Comportamento

### Antes
O método apenas atualizava os campos básicos do usuário (nome, email, matrícula) e ignorava completamente a lista de perfis fornecida no DTO.

### Depois
O método agora:

1. **Atualiza campos básicos**: Nome, email e matrícula (comportamento anterior mantido)

2. **Processa perfis do DTO**:
   - Se `usuarioDTO.getPerfis()` contém perfis: 
     - Carrega cada perfil do banco de dados
     - Valida se o perfil existe (lança exceção se não encontrado)
     - Substitui completamente o conjunto de perfis do usuário
     - Remove automaticamente perfis que não estão na lista fornecida
   
   - Se `usuarioDTO.getPerfis()` é uma lista vazia:
     - Remove todos os perfis do usuário
   
   - Se `usuarioDTO.getPerfis()` é `null`:
     - Mantém os perfis existentes (não faz alteração)

3. **Registra em auditoria**: Evento de edição é registrado normalmente

## Exemplo de Uso

### Requisição PUT para atualizar usuário com perfis

```json
{
  "nome": "João Silva",
  "email": "joao@example.com",
  "matricula": "12345",
  "perfis": [
    { "id": 1 },
    { "id": 3 }
  ]
}
```

**Resultado**: O usuário terá apenas os perfis com IDs 1 e 3. Qualquer perfil anterior que não esteja nesta lista será removido.

### Requisição para remover todos os perfis

```json
{
  "nome": "João Silva",
  "email": "joao@example.com",
  "matricula": "12345",
  "perfis": []
}
```

**Resultado**: Todos os perfis do usuário serão removidos.

### Requisição sem alterar perfis

```json
{
  "nome": "João Silva",
  "email": "joao@example.com",
  "matricula": "12345"
}
```

**Resultado**: Os perfis existentes são mantidos, apenas os campos básicos são atualizados.

## Validações

- ✅ Valida se cada perfil fornecido existe no banco de dados
- ✅ Lança exceção com mensagem clara se um perfil não for encontrado
- ✅ Registra em log a quantidade de perfis atualizados
- ✅ Mantém transação atômica (tudo ou nada)

## Logging

O método agora registra:
- Quando perfis são atualizados: `"Perfis atualizados para usuário: {id} - Total: {quantidade}"`
- Quando todos os perfis são removidos: `"Todos os perfis removidos do usuário: {id}"`

## Compilação

A solução foi compilada com sucesso:

```bash
mvn clean compile -q
```

Sem erros ou avisos.

## Impacto

- ✅ Endpoint `PUT /api/v1/usuarios/{id}` agora suporta atualização de perfis
- ✅ Compatível com versões anteriores (se `perfis` não for fornecido, mantém comportamento anterior)
- ✅ Transacional: garante consistência dos dados
