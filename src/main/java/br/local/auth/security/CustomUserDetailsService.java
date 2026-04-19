package br.local.auth.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Interface que estende UserDetailsService com métodos customizados
 *
 * Suporta 3 tipos de autenticação:
 * 1. Tipo 1: loadUserByUsername (usuário + senha)
 * 2. Tipo 2: loadUserByUsernameAndPasswordAndSistema (usuário + senha + módulo)
 * 3. Tipo 3: loadUserByUsernameAndPasswordAndSistema (usuário + senha + módulo + unidade)
 */
public interface CustomUserDetailsService extends UserDetailsService {

	/**
	 * Método padrão do UserDetailsService
	 * Tipo 1: Username + Password (sem módulo/unidade)
	 *
	 * @param username Nome de usuário ou e-mail
	 * @return UserDetails com todos os perfis
	 * @throws UsernameNotFoundException Se usuário não encontrado
	 */
	@Override
	UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

	/**
	 * Tipo 2: Username + Password + Módulo
	 * Carrega usuário com escopo de módulo específico
	 *
	 * @param username Nome de usuário
	 * @param password Senha
	 * @param sistema  Nome do módulo (ex: ADMIN, RECURSOS_HUMANOS)
	 * @return UserDetailsImpl com perfis do módulo
	 * @throws UsernameNotFoundException Se usuário não encontrado ou sem acesso ao módulo
	 */
	UserDetailsImpl loadUserByUsernameAndPasswordAndSistema(
		String username,
		String password,
		String sistema
	) throws UsernameNotFoundException;

	/**
	 * Tipo 3: Username + Password + Módulo + Unidade
	 * Carrega usuário com máximo escopo (módulo + unidade)
	 *
	 * @param username  Nome de usuário
	 * @param password  Senha
	 * @param sistema   Nome do módulo
	 * @param unidade   Sigla da unidade (ex: PRESIDENCIA)
	 * @return UserDetailsImpl com perfis da combinação módulo + unidade
	 * @throws UsernameNotFoundException Se usuário não encontrado ou sem acesso
	 */
	UserDetailsImpl loadUserByUsernameAndPasswordAndSistema(
		String username,
		String password,
		String sistema,
		String unidade
	) throws UsernameNotFoundException;

	/**
	 * Tipo 2: Username + Módulo (sem password)
	 * Para cenários onde apenas módulo é fornecido
	 *
	 * @param username Nome de usuário
	 * @param sistema  Nome do módulo
	 * @return UserDetailsImpl com perfis do módulo
	 * @throws UsernameNotFoundException Se usuário não encontrado ou sem acesso
	 */
	UserDetailsImpl loadUserByUsernameAndSistema(
		String username,
		String sistema
	) throws UsernameNotFoundException;

	/**
	 * Tipo 3: Username + Módulo + Unidade (sem password)
	 * Para cenários onde password não é fornecida
	 *
	 * @param username Nome de usuário
	 * @param sistema  Nome do módulo
	 * @param unidade  Sigla da unidade
	 * @return UserDetailsImpl com perfis da combinação
	 * @throws UsernameNotFoundException Se usuário não encontrado ou sem acesso
	 */
	UserDetailsImpl loadUserByUsernameAndSistema(
		String username,
		String sistema,
		String unidade
	) throws UsernameNotFoundException;
}
