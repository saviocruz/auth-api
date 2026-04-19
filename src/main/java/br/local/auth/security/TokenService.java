package br.local.auth.security;

import java.util.Date;
import java.util.List;

import io.jsonwebtoken.Claims;

/**
 * Interface para geração e validação de tokens JWT.
 *
 * Responsabilidades:
 * 1. Gerar tokens JWT com claims específicos
 * 2. Validar tokens JWT
 * 3. Extrair claims do token
 * 4. Gerenciar expiração de tokens (access e refresh)
 */
public interface TokenService {

	/**
	 * Gera um token JWT com claims do usuário para Tipo 1 (sem módulo/unidade)
	 *
	 * @param usuarioId ID do usuário
	 * @param username Username do usuário
	 * @param email Email do usuário
	 * @param perfis Lista de perfis/autoridades
	 * @return Token JWT
	 */
	String gerarToken(Long usuarioId, String username, String email, List<String> perfis);

	/**
	 * Gera um token JWT com claims do usuário para Tipo 2 (com módulo)
	 *
	 * @param usuarioId ID do usuário
	 * @param username Username do usuário
	 * @param email Email do usuário
	 * @param sistema Nome do módulo/sistema
	 * @param perfis Lista de perfis/autoridades
	 * @return Token JWT
	 */
	String gerarTokenComSistema(Long usuarioId, String username, String email, String sistema, List<String> perfis);

	/**
	 * Gera um token JWT com claims do usuário para Tipo 3 (com módulo + unidade)
	 *
	 * @param usuarioId ID do usuário
	 * @param username Username do usuário
	 * @param email Email do usuário
	 * @param sistema Nome do módulo/sistema
	 * @param unidade Sigla da unidade
	 * @param perfis Lista de perfis/autoridades
	 * @return Token JWT
	 */
	String gerarTokenComSistemaEUnidade(Long usuarioId, String username, String email,
		String sistema, String unidade, List<String> perfis);

	/**
	 * Gera um refresh token com expiração maior
	 *
	 * @param usuarioId ID do usuário
	 * @param username Username do usuário
	 * @return Refresh token JWT
	 */
	String gerarRefreshToken(Long usuarioId, String username);

	/**
	 * Valida um token JWT
	 *
	 * @param token Token JWT
	 * @return true se válido, false caso contrário
	 */
	boolean validarToken(String token);

	/**
	 * Extrai o username do token JWT
	 *
	 * @param token Token JWT
	 * @return Username
	 */
	String extrairUsername(String token);

	/**
	 * Extrai o ID do usuário do token JWT
	 *
	 * @param token Token JWT
	 * @return ID do usuário
	 */
	Long extrairUsuarioId(String token);

	/**
	 * Extrai o email do token JWT
	 *
	 * @param token Token JWT
	 * @return Email do usuário
	 */
	String extrairEmail(String token);

	/**
	 * Extrai o sistema/módulo do token JWT, se presente
	 *
	 * @param token Token JWT
	 * @return Sistema/módulo ou null se não presente
	 */
	String extrairSistema(String token);

	/**
	 * Extrai a unidade do token JWT, se presente
	 *
	 * @param token Token JWT
	 * @return Unidade ou null se não presente
	 */
	String extrairUnidade(String token);

	/**
	 * Extrai a lista de perfis/autoridades do token JWT
	 *
	 * @param token Token JWT
	 * @return Lista de perfis
	 */
	List<String> extrairPerfis(String token);

	/**
	 * Extrai todos os claims do token JWT
	 *
	 * @param token Token JWT
	 * @return Claims do token
	 */
	Claims extrairClaims(String token);

	/**
	 * Obtém a data de expiração do token
	 *
	 * @param token Token JWT
	 * @return Data de expiração
	 */
	Date obterDataExpiracao(String token);

	/**
	 * Verifica se o token está expirado
	 *
	 * @param token Token JWT
	 * @return true se expirado, false caso contrário
	 */
	boolean estaExpirado(String token);

	/**
	 * Obtém o tempo até expiração em segundos
	 *
	 * @param token Token JWT
	 * @return Tempo em segundos até expiração
	 */
	Long obterTempoAteExpiracao(String token);
}
