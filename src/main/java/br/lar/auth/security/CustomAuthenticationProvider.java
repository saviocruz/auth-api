package br.lar.auth.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import br.lar.auth.model.Usuario;
import br.lar.auth.model.UsuarioHistorico;
import br.lar.auth.repository.UsuarioHistoricoRepository;
import br.lar.auth.repository.UsuarioRepository;

/**
 * CustomAuthenticationProvider responsável por autenticar usuários
 * com suporte aos 3 tipos de login.
 *
 * Responsabilidades:
 * 1. Detectar tipo de token (Tipo 1, 2 ou 3)
 * 2. Validar credenciais via UserDetailsService apropriado
 * 3. Validar senha com PasswordEncoder
 * 4. Gerenciar tentativas falhas de login (bloqueio após 3 falhas)
 * 5. Registrar eventos de LOGIN_SUCESSO e LOGIN_FALHA em auditoria
 */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

	private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationProvider.class);
	private static final int MAX_TENTATIVAS_FALHAS = 3;

	@Autowired
	private CustomUserDetailsService customUserDetailsService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private UsuarioHistoricoRepository usuarioHistoricoRepository;

	/**
	 * Autentica um usuário baseado no tipo de token recebido.
	 *
	 * Suporta 3 tipos de autenticação:
	 * 1. UsernamePasswordAuthenticationToken: Tipo 1 (username + password)
	 * 2. CustomUsernamePasswordAuthenticationToken: Tipo 2 (username + password + módulo)
	 * 3. ExtendedAuthenticationToken: Tipo 3 (username + password + módulo + unidade)
	 *
	 * @param authentication Token de autenticação
	 * @return Authentication autenticado com autoridades
	 * @throws AuthenticationException Em caso de falha
	 */
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		logger.info("Iniciando autenticação - tipo de token: {}", authentication.getClass().getSimpleName());

		String username = authentication.getName();
		String password = (String) authentication.getCredentials();

		// Validar credenciais básicas
		if (username == null || username.isEmpty()) {
			logger.warn("Tentativa de autenticação com username vazio");
			throw new BadCredentialsException("Username não pode estar vazio");
		}

		if (password == null || password.isEmpty()) {
			logger.warn("Tentativa de autenticação com password vazio para usuário: {}", username);
			throw new BadCredentialsException("Password não pode estar vazio");
		}

		try {
			// Tipo 3: ExtendedAuthenticationToken (username + password + módulo + unidade)
			if (authentication instanceof ExtendedAuthenticationToken) {
				return autenticarTipo3((ExtendedAuthenticationToken) authentication);
			}

			// Tipo 2: CustomUsernamePasswordAuthenticationToken (username + password + módulo)
			if (authentication instanceof CustomUsernamePasswordAuthenticationToken) {
				return autenticarTipo2((CustomUsernamePasswordAuthenticationToken) authentication);
			}

			// Tipo 1: UsernamePasswordAuthenticationToken (username + password)
			if (authentication instanceof UsernamePasswordAuthenticationToken) {
				return autenticarTipo1((UsernamePasswordAuthenticationToken) authentication);
			}

			logger.error("Tipo de token não suportado: {}", authentication.getClass().getSimpleName());
			throw new BadCredentialsException("Tipo de autenticação não suportado");

		} catch (AuthenticationException e) {
			// Registrar falha em auditoria
			registrarLoginFalha(username, authentication);
			throw e;
		}
	}

	/**
	 * Autentica Tipo 1: username + password (acesso a todos os módulos)
	 */
	private Authentication autenticarTipo1(UsernamePasswordAuthenticationToken authentication)
			throws AuthenticationException {

		String username = authentication.getName();
		String password = (String) authentication.getCredentials();

		logger.info("Autenticando Tipo 1 - username: {}", username);

		// Carregar usuário com todos os perfis (Tipo 1)
		UserDetailsImpl userDetails = (UserDetailsImpl) customUserDetailsService.loadUserByUsername(username);

		// Validar status do usuário
		if (!userDetails.isEnabled()) {
			logger.warn("Usuário desabilitado ou bloqueado: {}", username);
			registrarLoginFalha(username, authentication);
			throw new DisabledException("Usuário desabilitado ou bloqueado");
		}

		// Validar senha
		//String s = passwordEncoder.encode("admin123");
		if (!passwordEncoder.matches(password, userDetails.getPassword())) {
			logger.warn("Senha inválida para usuário: {}", username);
			incrementarTentativasFalhas(userDetails);
			throw new BadCredentialsException("Credenciais inválidas");
		}

		// Resetar tentativas falhas
		resetarTentativasFalhas(userDetails);

		// Criar token autenticado
		Authentication authenticatedToken = new UsernamePasswordAuthenticationToken(
			userDetails, password, userDetails.getAuthorities());

		logger.info("Usuário autenticado com sucesso (Tipo 1): {}", username);
		registrarLoginSucesso(username, authentication);

		return authenticatedToken;
	}

	/**
	 * Autentica Tipo 2: username + password + módulo
	 */
	private Authentication autenticarTipo2(CustomUsernamePasswordAuthenticationToken authentication)
			throws AuthenticationException {

		String username = authentication.getName();
		String password = (String) authentication.getCredentials();
		String sistema = authentication.getSistema();

		logger.info("Autenticando Tipo 2 - username: {}, sistema: {}", username, sistema);

		// Carregar usuário com filtro de módulo (Tipo 2)
		UserDetailsImpl userDetails = (UserDetailsImpl) customUserDetailsService
			.loadUserByUsernameAndPasswordAndSistema(username, password, sistema);

		// Validar status do usuário
		if (!userDetails.isEnabled()) {
			logger.warn("Usuário desabilitado ou bloqueado para módulo {} - username: {}", sistema, username);
			registrarLoginFalha(username, authentication);
			throw new DisabledException("Usuário desabilitado ou bloqueado");
		}

		// Validar senha
		if (!passwordEncoder.matches(password, userDetails.getPassword())) {
			logger.warn("Senha inválida para usuário (Tipo 2): {} em módulo: {}", username, sistema);
			incrementarTentativasFalhas(userDetails);
			throw new BadCredentialsException("Credenciais inválidas");
		}

		// Resetar tentativas falhas
		resetarTentativasFalhas(userDetails);

		// Criar token autenticado com escopo de módulo
		CustomUsernamePasswordAuthenticationToken authenticatedToken =
			new CustomUsernamePasswordAuthenticationToken(
				userDetails,
				password,
				userDetails.getAuthorities(),
				sistema,
				null,
				null);

		logger.info("Usuário autenticado com sucesso (Tipo 2): {} em módulo: {}", username, sistema);
		registrarLoginSucesso(username, authentication);

		return authenticatedToken;
	}

	/**
	 * Autentica Tipo 3: username + password + módulo + unidade
	 */
	private Authentication autenticarTipo3(ExtendedAuthenticationToken authentication)
			throws AuthenticationException {

		String username = authentication.getName();
		String password = (String) authentication.getCredentials();
		String sistema = authentication.getSistema();
		String unidade = authentication.getUnidade();

		logger.info("Autenticando Tipo 3 - username: {}, sistema: {}, unidade: {}", username, sistema, unidade);

		// Carregar usuário com filtro de módulo + unidade (Tipo 3)
		UserDetailsImpl userDetails = (UserDetailsImpl) customUserDetailsService
			.loadUserByUsernameAndPasswordAndSistema(username, password, sistema, unidade);

		// Validar status do usuário
		if (!userDetails.isEnabled()) {
			logger.warn("Usuário desabilitado ou bloqueado para módulo {} e unidade {} - username: {}",
				sistema, unidade, username);
			registrarLoginFalha(username, authentication);
			throw new DisabledException("Usuário desabilitado ou bloqueado");
		}

		// Validar senha
		if (!passwordEncoder.matches(password, userDetails.getPassword())) {
			logger.warn("Senha inválida para usuário (Tipo 3): {} em módulo: {} e unidade: {}",
				username, sistema, unidade);
			incrementarTentativasFalhas(userDetails);
			throw new BadCredentialsException("Credenciais inválidas");
		}

		// Resetar tentativas falhas
		resetarTentativasFalhas(userDetails);

		// Criar token autenticado com escopo de módulo + unidade
		java.util.Map<String, Object> params = new java.util.HashMap<>();
		params.put("sistema", sistema);
		params.put("unidade", unidade);

		ExtendedAuthenticationToken authenticatedToken = new ExtendedAuthenticationToken(
			userDetails,
			password,
			userDetails.getAuthorities(),
			params);

		logger.info("Usuário autenticado com sucesso (Tipo 3): {} em módulo: {} e unidade: {}",
			username, sistema, unidade);
		registrarLoginSucesso(username, authentication);

		return authenticatedToken;
	}

	/**
	 * Incrementa tentativas falhas de login e bloqueia a conta após MAX_TENTATIVAS_FALHAS
	 */
	private void incrementarTentativasFalhas(UserDetailsImpl userDetails) {
		try {
			Usuario usuario = usuarioRepository.findById(userDetails.getId())
				.orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

			usuario.incrementarTentativasFalhas();

			if (usuario.estaBloqueado()) {
				logger.warn("Usuário bloqueado após {} tentativas falhas: {}",
					MAX_TENTATIVAS_FALHAS, usuario.getUsername());
				usuario.bloquear();
			}

			usuarioRepository.save(usuario);
			logger.info("Tentativas falhas incrementadas para usuário: {} (total: {})",
				usuario.getUsername(), usuario.getTentativasFalhas());

		} catch (Exception e) {
			logger.error("Erro ao incrementar tentativas falhas", e);
		}
	}

	/**
	 * Reseta tentativas falhas para zero
	 */
	private void resetarTentativasFalhas(UserDetailsImpl userDetails) {
		try {
			Usuario usuario = usuarioRepository.findById(userDetails.getId())
				.orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

			if (usuario.getTentativasFalhas() > 0) {
				usuario.resetarTentativasFalhas();
				usuarioRepository.save(usuario);
				logger.info("Tentativas falhas resetadas para usuário: {}", usuario.getUsername());
			}

		} catch (Exception e) {
			logger.error("Erro ao resetar tentativas falhas", e);
		}
	}

	/**
	 * Registra evento de LOGIN_SUCESSO em auditoria
	 */
	private void registrarLoginSucesso(String username, Authentication authentication) {
		try {
			Usuario usuario = usuarioRepository.findByUsername(username)
				.orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

			String tipo = extrairTipoAutenticacao(authentication);
			String descricao = "Login bem-sucedido - Tipo " + tipo;
			String modulo = extrairSistema(authentication);
			String unidade = extrairUnidade(authentication);

			UsuarioHistorico historico = new UsuarioHistorico(
				usuario, "LOGIN_SUCESSO", descricao, modulo, unidade);

			usuarioHistoricoRepository.save(historico);
			logger.info("Evento LOGIN_SUCESSO registrado para usuário: {}", username);

		} catch (Exception e) {
			logger.error("Erro ao registrar evento de login sucesso", e);
		}
	}

	/**
	 * Registra evento de LOGIN_FALHA em auditoria
	 */
	private void registrarLoginFalha(String username, Authentication authentication) {
		try {
			Usuario usuario = usuarioRepository.findByUsername(username)
				.orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

			String tipo = extrairTipoAutenticacao(authentication);
			String descricao = "Falha de login - Tipo " + tipo;
			String modulo = extrairSistema(authentication);
			String unidade = extrairUnidade(authentication);

			br.lar.auth.model.UsuarioHistorico historico = new br.lar.auth.model.UsuarioHistorico(
				usuario, "LOGIN_FALHA", descricao, modulo, unidade);

			usuarioHistoricoRepository.save(historico);
			logger.info("Evento LOGIN_FALHA registrado para usuário: {}", username);

		} catch (Exception e) {
			logger.error("Erro ao registrar evento de login falha", e);
		}
	}

	/**
	 * Extrai o tipo de autenticação do token
	 */
	private String extrairTipoAutenticacao(Authentication authentication) {
		if (authentication instanceof ExtendedAuthenticationToken) {
			return "3";
		}
		if (authentication instanceof CustomUsernamePasswordAuthenticationToken) {
			return "2";
		}
		return "1";
	}

	/**
	 * Extrai o módulo/sistema do token, se disponível
	 */
	private String extrairSistema(Authentication authentication) {
		if (authentication instanceof ExtendedAuthenticationToken) {
			return ((ExtendedAuthenticationToken) authentication).getSistema();
		}
		if (authentication instanceof CustomUsernamePasswordAuthenticationToken) {
			return ((CustomUsernamePasswordAuthenticationToken) authentication).getSistema();
		}
		return null;
	}

	/**
	 * Extrai a unidade do token, se disponível
	 */
	private String extrairUnidade(Authentication authentication) {
		if (authentication instanceof ExtendedAuthenticationToken) {
			return ((ExtendedAuthenticationToken) authentication).getUnidade();
		}
		return null;
	}

	/**
	 * Define quais tipos de autenticação este provider suporta
	 */
	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.isAssignableFrom(UsernamePasswordAuthenticationToken.class)
			|| authentication.isAssignableFrom(CustomUsernamePasswordAuthenticationToken.class)
			|| authentication.isAssignableFrom(ExtendedAuthenticationToken.class);
	}
}
