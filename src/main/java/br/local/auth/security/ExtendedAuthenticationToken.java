package br.local.auth.security;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/**
 * Token de autenticação completamente customizado para Tipo 3 de login
 * (Username + Password + Módulo + Unidade)
 *
 * Suporta mapa de parâmetros adicionais para máxima flexibilidade
 */
public class ExtendedAuthenticationToken extends AbstractAuthenticationToken {

	private static final long serialVersionUID = 1L;

	private Object principal;
	private Object credentials;
	private String sistema;
	private String unidade;
	private Map<String, Object> allParameters; // Parâmetros adicionais

	/**
	 * Construtor antes da autenticação
	 */
	public ExtendedAuthenticationToken(
		Object principal, Object credentials,
		String sistema, String unidade,
		Map<String, Object> allParameters) {

		super(null);
		this.principal = principal;
		this.credentials = credentials;
		this.sistema = sistema;
		this.unidade = unidade;
		this.allParameters = allParameters != null ? allParameters : new HashMap<>();
		this.setAuthenticated(false);
	}

	/**
	 * Construtor após autenticação
	 */
	public ExtendedAuthenticationToken(
		Object principal, Object credentials,
		Collection<? extends GrantedAuthority> authorities,
		Map<String, Object> allParameters) {

		super(authorities);
		this.principal = principal;
		this.credentials = credentials;

		// Extrair sistema e unidade do mapa de parâmetros
		if (allParameters != null) {
			this.sistema = (String) allParameters.get("sistema");
			this.unidade = (String) allParameters.get("unidade");
			this.allParameters = allParameters;
		} else {
			this.allParameters = new HashMap<>();
		}

		this.setAuthenticated(true);
	}

	@Override
	public Object getCredentials() {
		return credentials;
	}

	@Override
	public Object getPrincipal() {
		return principal;
	}

	public String getSistema() {
		return sistema;
	}

	public void setSistema(String sistema) {
		this.sistema = sistema;
		if (this.allParameters != null) {
			this.allParameters.put("sistema", sistema);
		}
	}

	public String getUnidade() {
		return unidade;
	}

	public void setUnidade(String unidade) {
		this.unidade = unidade;
		if (this.allParameters != null) {
			this.allParameters.put("unidade", unidade);
		}
	}

	public Map<String, Object> getAllParameters() {
		return allParameters;
	}

	public void setAllParameters(Map<String, Object> allParameters) {
		this.allParameters = allParameters;
	}

	public Object getParameter(String key) {
		return this.allParameters.get(key);
	}

	public void putParameter(String key, Object value) {
		this.allParameters.put(key, value);
	}
}
