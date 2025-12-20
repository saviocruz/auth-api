# Correção: Tratamento de Erro de Conversão de Tipo em Parâmetros de Rota

## Problema Identificado

A aplicação estava retornando um erro genérico quando um parâmetro de rota inválido era passado:

```
2025-12-20 14:20:46 - o.s.security.web.FilterChainProxy - Secured PUT /api/v1/usuarios/undefined
2025-12-20 14:20:46 - o.s.w.s.m.s.DefaultHandlerExceptionResolver - Resolved [org.springframework.web.method.annotation.MethodArgumentTypeMismatchException: Method parameter 'id': Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'; For input string: "undefined"]
```

### Causa Raiz

O endpoint `PUT /api/v1/usuarios/{id}` espera um parâmetro `id` do tipo `Long`, mas estava recebendo a string "undefined" (provavelmente do frontend). O Spring não conseguia converter "undefined" para `Long` e retornava um erro genérico.

## Solução Implementada

Criado um `ControllerAdvice` global (`GlobalExceptionHandler`) que captura e trata a exceção `MethodArgumentTypeMismatchException` de forma elegante.

### Arquivo Criado

**`src/main/java/br/lar/auth/config/GlobalExceptionHandler.java`**

Este arquivo implementa:

1. **Tratamento de `MethodArgumentTypeMismatchException`**: Captura erros de conversão de tipo em parâmetros de rota e retorna uma resposta JSON estruturada com:
   - `erro`: Mensagem de erro clara
   - `detalhes`: Descrição detalhada do problema
   - `parametro`: Nome do parâmetro que causou o erro
   - `valorRecebido`: Valor que foi recebido

2. **Tratamento genérico de exceções**: Captura qualquer exceção não tratada e retorna um erro 500 com mensagem apropriada

### Exemplo de Resposta

Quando uma requisição é feita para `PUT /api/v1/usuarios/undefined`:

```json
{
  "erro": "Parâmetro inválido",
  "detalhes": "O parâmetro 'id' com valor 'undefined' não é válido. Esperado: Long",
  "parametro": "id",
  "valorRecebido": "undefined"
}
```

Status HTTP: **400 Bad Request**

## Benefícios

1. **Melhor experiência do usuário**: Mensagens de erro claras e estruturadas
2. **Facilita debug**: Identifica exatamente qual parâmetro está inválido e qual valor foi recebido
3. **Consistência**: Todas as exceções de conversão de tipo são tratadas da mesma forma
4. **Logging**: Registra avisos no log para monitoramento
5. **Reutilizável**: O handler é global e funciona para todos os endpoints da aplicação

## Endpoints Afetados

Todos os endpoints que usam parâmetros de rota do tipo `Long` agora retornarão uma resposta estruturada em caso de erro:

- `GET /api/v1/usuarios/{id}`
- `PUT /api/v1/usuarios/{id}`
- `DELETE /api/v1/usuarios/{id}`
- `PUT /api/v1/usuarios/{id}/perfis`
- `PUT /api/v1/usuarios/{id}/unidades`
- `PUT /api/v1/usuarios/{id}/bloquear`
- `PUT /api/v1/usuarios/{id}/desbloquear`
- `PUT /api/v1/usuarios/{id}/alterar-senha`
- `GET /api/v1/usuarios/{id}/historico-logins`

E qualquer outro endpoint que use parâmetros de rota com tipos numéricos.

## Compilação

A solução foi compilada com sucesso:

```bash
mvn clean compile -q
```

Sem erros ou avisos.
