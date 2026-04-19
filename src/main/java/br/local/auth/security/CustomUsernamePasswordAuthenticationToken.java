package br.local.auth.security;

import java.util.Collection;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/**
 * Token de autenticação customizado para Tipo 2 de login
 * (Username + Password + Módulo)
 *
 * Estende UsernamePasswordAuthenticationToken para adicionar campos de escopo
 */
public class CustomUsernamePasswordAuthenticationToken extends UsernamePasswordAuthenticationToken {

	private static final long serialVersionUID = 1L;

	private String sistema; // Módulo
	private String unidade;
	private String perfil;

	/**
	 * Construtor antes da autenticação (unauthenticated)
	 */
	public CustomUsernamePasswordAuthenticationToken(
		Object principal, Object credentials, String sistema, String unidade, String perfil) {

		super(principal, credentials);
		this.sistema = sistema;
		this.unidade = unidade;
		this.perfil = perfil;
		this.setAuthenticated(false);
	}

	/**
	 * Construtor após a autenticação (authenticated)
	 */
	public CustomUsernamePasswordAuthenticationToken(
		Object principal, Object credentials,
		Collection<? extends GrantedAuthority> authorities,
		String sistema, String unidade, String perfil) {

		super(principal, credentials, authorities);
		this.sistema = sistema;
		this.unidade = unidade;
		this.perfil = perfil;
		this.setAuthenticated(true);
	}

	public String getSistema() {
		return sistema;
	}

	public void setSistema(String sistema) {
		this.sistema = sistema;
	}

	public String getUnidade() {
		return unidade;
	}

	public void setUnidade(String unidade) {
		this.unidade = unidade;
	}

	public String getPerfil() {
		return perfil;
	}

	public void setPerfil(String perfil) {
		this.perfil = perfil;
	}
}
