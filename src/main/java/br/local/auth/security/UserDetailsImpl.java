package br.local.auth.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import br.local.auth.dto.PerfilDTO;
import br.local.auth.dto.UnidadeDTO;
import br.local.auth.model.Usuario;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Implementação de UserDetails para Spring Security
 *
 * Contém:
 * - Dados básicos do usuário
 * - Perfis (roles)
 * - Módulo e unidade (para escopo de login)
 * - Autoridades derivadas dos perfis
 */
@Data
@NoArgsConstructor
@Builder
public class UserDetailsImpl implements UserDetails {

	private static final long serialVersionUID = 1L;

	private Long id;
	private String username;
	private String matricula;

	@JsonIgnore
	private String password;

	private String nome;
	private String email;

	/**
	 * Escopo de login (Tipo 2 e 3)
	 */
	private String sistema; // Módulo
	private UnidadeDTO unidade;

	/**
	 * Perfis do usuário
	 */
	private List<PerfilDTO> perfis;

	/**
	 * Autoridades derivadas dos perfis
	 */
	private Collection<? extends GrantedAuthority> authorities;

	/**
	 * Construtor a partir de Usuario (Tipo 1 de login - sem módulo/unidade)
	 */
	public UserDetailsImpl(Usuario usuario) {
		this.id = usuario.getId();
		this.username = usuario.getUsername();
		this.password = usuario.getChave();
		this.matricula = usuario.getMatricula();
		this.nome = usuario.getNome();
		this.email = usuario.getEmail();
		this.perfis = new ArrayList<>();
		this.authorities = new ArrayList<>();
	}

	/**
	 * Construtor completo
	 */
	public UserDetailsImpl(Long id, String username, String matricula, String password,
						String nome, String email, String sistema, UnidadeDTO unidade,
						List<PerfilDTO> perfis, Collection<? extends GrantedAuthority> authorities) {
		this.id = id;
		this.username = username;
		this.matricula = matricula;
		this.password = password;
		this.nome = nome;
		this.email = email;
		this.sistema = sistema;
		this.unidade = unidade;
		this.perfis = perfis;
		this.authorities = authorities;
	}

	/**
	 * Factory method para criar UserDetailsImpl a partir de Usuario
	 * Carrega perfis e autoridades
	 */
	public static UserDetailsImpl build(Usuario usuario, List<PerfilDTO> perfis) {
		List<GrantedAuthority> authorities = new ArrayList<>();

		// Adicionar cada perfil como autoridade
		if (perfis != null) {
			authorities.addAll(
				perfis.stream()
					.map(p -> new SimpleGrantedAuthority(removerPrefixoRole(p.getNome())))
					.collect(Collectors.toList())
			);
		}

		return new UserDetailsImpl(
			usuario.getId(),
			usuario.getUsername(),
			usuario.getMatricula(),
			usuario.getChave(),
			usuario.getNome(),
			usuario.getEmail(),
			null, // sem sistema
			null, // sem unidade
			perfis,
			authorities
		);
	}

	/**
	 * Factory method para Tipo 2 e 3 de login (com módulo/unidade)
	 */
	public static UserDetailsImpl buildWithScope(Usuario usuario, List<PerfilDTO> perfis,
											   String sistema, UnidadeDTO unidade) {
		List<GrantedAuthority> authorities = new ArrayList<>();

		if (perfis != null) {
			authorities.addAll(
				perfis.stream()
					.map(p -> new SimpleGrantedAuthority(removerPrefixoRole(p.getNome())))
					.collect(Collectors.toList())
			);
		}

		return new UserDetailsImpl(
			usuario.getId(),
			usuario.getUsername(),
			usuario.getMatricula(),
			usuario.getChave(),
			usuario.getNome(),
			usuario.getEmail(),
			sistema,
			unidade,
			perfis,
			authorities
		);
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		// Verificar se usuário não está bloqueado
		return true; // Será verificado no AuthenticationProvider
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true; // Será verificado no AuthenticationProvider
	}

	/**
	 * Verificar se tem módulo associado
	 */
	public boolean temSistema() {
		return this.sistema != null;
	}

	/**
	 * Verificar se tem unidade associada
	 */
	public boolean temUnidade() {
		return this.unidade != null;
	}

	/**
	 * Verificar se tem específica funcionalidade
	 */
	public boolean temFuncionalidade(String nomeFunc) {
		if (this.perfis == null) {
			return false;
		}

		return this.perfis.stream()
			.filter(p -> p.getFuncionalidades() != null)
			.flatMap(p -> p.getFuncionalidades().stream())
			.anyMatch(f -> f.getNome().equals(nomeFunc));
	}

	/**
	 * Remove o prefixo ROLE_ do nome do perfil se existir
	 * Exemplo: "ROLE_ADMIN_GERAL" vira "ADMIN_GERAL"
	 */
	private static String removerPrefixoRole(String nomePerfil) {
		if (nomePerfil != null && nomePerfil.startsWith("ROLE_")) {
			return nomePerfil.substring(5);
		}
		return nomePerfil;
	}
}
