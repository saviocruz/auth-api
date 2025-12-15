package br.lar.auth.security;

import br.lar.auth.dto.LoginRequestDTO;
import br.lar.auth.dto.LoginResponseDTO;

/**
 * Interface para orquestração de autenticação e geração de tokens.
 *
 * Responsabilidades:
 * 1. Orquestrar o fluxo de autenticação (Tipo 1, 2 ou 3)
 * 2. Gerenciar tentativas falhas de login
 * 3. Validar credenciais e permissões
 * 4. Gerar tokens JWT (access e refresh)
 * 5. Registrar eventos de auditoria
 * 6. Renovar tokens (refresh token flow)
 */
public interface AuthService {

	/**
	 * Autentica um usuário baseado na requisição de login.
	 *
	 * Suporta 3 tipos de autenticação:
	 * 1. Tipo 1: username + password (acesso a todos os módulos)
	 * 2. Tipo 2: username + password + módulo
	 * 3. Tipo 3: username + password + módulo + unidade
	 *
	 * @param loginRequest Requisição contendo credenciais
	 * @return LoginResponseDTO com tokens e dados do usuário
	 * @throws AuthenticationException Em caso de falha
	 */
	LoginResponseDTO autenticar(LoginRequestDTO loginRequest);

	/**
	 * Renova o access token usando um refresh token válido
	 *
	 * @param refreshToken Refresh token válido
	 * @return LoginResponseDTO com novo access token
	 * @throws AuthenticationException Se refresh token inválido ou expirado
	 */
	LoginResponseDTO renovarToken(String refreshToken);

	/**
	 * Invalida um token adicionando-o a uma blacklist (Redis)
	 *
	 * @param token Token JWT a ser invalidado
	 */
	void invalidarToken(String token);

	/**
	 * Verifica se um token está na blacklist
	 *
	 * @param token Token JWT
	 * @return true se está na blacklist, false caso contrário
	 */
	boolean estaNaBlacklist(String token);

	/**
	 * Registra evento de logout em auditoria
	 *
	 * @param usuarioId ID do usuário
	 * @param username Username do usuário
	 */
	void registrarLogout(Long usuarioId, String username);
}
