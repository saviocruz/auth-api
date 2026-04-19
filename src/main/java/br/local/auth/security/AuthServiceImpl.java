package br.local.auth.security;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.local.auth.dto.LoginRequestDTO;
import br.local.auth.dto.LoginResponseDTO;
import br.local.auth.mapper.PerfilMapper;
import br.local.auth.mapper.UsuarioMapper;
import br.local.auth.model.Usuario;
import br.local.auth.model.UsuarioHistorico;
import br.local.auth.repository.UsuarioHistoricoRepository;
import br.local.auth.repository.UsuarioRepository;

/**
 * Implementação de AuthService para orquestração de autenticação e geração de tokens.
 *
 * Responsabilidades:
 * 1. Orquestrar o fluxo de autenticação (Tipo 1, 2 ou 3)
 * 2. Gerenciar tentativas falhas de login
 * 3. Validar credenciais e permissões
 * 4. Gerar tokens JWT (access e refresh)
 * 5. Registrar eventos de auditoria
 * 6. Renovar tokens (refresh token flow)
 */
@Service
@Transactional
public class AuthServiceImpl implements AuthService {

	private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private TokenService tokenService;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private UsuarioHistoricoRepository usuarioHistoricoRepository;

	@Autowired
	private UsuarioMapper usuarioMapper;

	@Autowired
	private PerfilMapper perfilMapper;

	/**
	 * Autentica um usuário baseado na requisição de login.
	 *
	 * Fluxo:
	 * 1. Detectar tipo de login (Tipo 1, 2 ou 3)
	 * 2. Criar token de autenticação apropriado
	 * 3. Autenticar com AuthenticationManager (que usa CustomAuthenticationProvider)
	 * 4. Gerar JWT e refresh token
	 * 5. Registrar sucesso em auditoria
	 * 6. Retornar resposta com tokens e dados do usuário
	 */
	@Override
	public LoginResponseDTO autenticar(LoginRequestDTO loginRequest) {
		logger.info("Iniciando autenticação para usuário: {}", loginRequest.getUsername());

		try {
			// Validar entrada
			validarLoginRequest(loginRequest);

			// Detectar tipo de login e criar token apropriado
			Authentication authenticationToken = criarTokenAutenticacao(loginRequest);

			// Autenticar com AuthenticationManager (CustomAuthenticationProvider)
			Authentication authenticationResult = authenticationManager.authenticate(authenticationToken);

			// Extrair UserDetails autenticado
			UserDetailsImpl userDetails = (UserDetailsImpl) authenticationResult.getPrincipal();

			// Extrair perfis como lista de strings
			List<String> perfis = userDetails.getAuthorities().stream()
				.map(auth -> auth.getAuthority())
				.collect(Collectors.toList());

			// Gerar tokens JWT
			String accessToken = gerarAccessToken(userDetails, loginRequest, perfis);
			String refreshToken = tokenService.gerarRefreshToken(userDetails.getId(), userDetails.getUsername());

			// Carregar dados completos do usuário
			Usuario usuario = usuarioRepository.findById(userDetails.getId())
				.orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

			// Atualizar data de último login
			usuario.setDataUltimoLogin(new Date());
			usuarioRepository.save(usuario);

			// Registrar sucesso em auditoria
			registrarLoginSucesso(usuario, loginRequest);

			// Montar resposta
			Date expiracao = tokenService.obterDataExpiracao(accessToken);
			Long tempoAteExpiracao = tokenService.obterTempoAteExpiracao(accessToken);

		LoginResponseDTO response = LoginResponseDTO.builder()
			.sucesso(true)
			.mensagem("Autenticação bem-sucedida")
			.token(accessToken)
			.refreshToken(refreshToken)
			.usuario(usuarioMapper.toDTO(usuario))
			.expiresIn(tempoAteExpiracao)
			.expiresAt(expiracao)
			.build();			logger.info("Usuário autenticado com sucesso: {}", loginRequest.getUsername());
			return response;

		} catch (AuthenticationException e) {
			logger.error("Falha de autenticação para usuário: {} - Motivo: {}",
				loginRequest.getUsername(), e.getMessage());

		LoginResponseDTO response = LoginResponseDTO.builder()
			.sucesso(false)
			.mensagem("Autenticação falhou")
			.erro(e.getMessage())
			.build();			return response;

		} catch (Exception e) {
			logger.error("Erro durante autenticação", e);

		LoginResponseDTO response = LoginResponseDTO.builder()
			.sucesso(false)
			.mensagem("Erro ao processar autenticação")
			.erro(e.getMessage())
			.build();			return response;
		}
	}

	/**
	 * Renova um access token usando refresh token válido
	 */
	@Override
	public LoginResponseDTO renovarToken(String refreshToken) {
		logger.info("Renovando token");

		try {
			// Validar refresh token
			if (!tokenService.validarToken(refreshToken)) {
				logger.warn("Refresh token inválido ou expirado");
				return LoginResponseDTO.builder()
					.sucesso(false)
					.mensagem("Refresh token inválido ou expirado")
					.erro("Token expirado")
					.build();
			}

			// Extrair informações do refresh token
			Long usuarioId = tokenService.extrairUsuarioId(refreshToken);
			String username = tokenService.extrairUsername(refreshToken);

			// Carregar usuário
			Usuario usuario = usuarioRepository.findById(usuarioId)
				.orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

			if (!usuario.podeLogin()) {
				logger.warn("Usuário não pode fazer login: {}", username);
				return LoginResponseDTO.builder()
					.sucesso(false)
					.mensagem("Usuário não pode fazer login")
					.erro("Usuário bloqueado ou inativo")
					.build();
			}

			// Carregar UserDetails
			UserDetailsImpl userDetails = UserDetailsImpl.build(usuario, usuario.getPerfis().stream()
				.map(perfilMapper::toDTO)
				.collect(Collectors.toList()));

			List<String> perfis = userDetails.getAuthorities().stream()
				.map(auth -> auth.getAuthority())
				.collect(Collectors.toList());

			// Gerar novo access token (Tipo 1)
			String novoAccessToken = tokenService.gerarToken(
				userDetails.getId(),
				userDetails.getUsername(),
				userDetails.getEmail(),
				perfis);

			// Gerar novo refresh token
			String novoRefreshToken = tokenService.gerarRefreshToken(userDetails.getId(), userDetails.getUsername());

			// Montar resposta
			Date expiracao = tokenService.obterDataExpiracao(novoAccessToken);
			Long tempoAteExpiracao = tokenService.obterTempoAteExpiracao(novoAccessToken);

			LoginResponseDTO response = LoginResponseDTO.builder()
				.sucesso(true)
				.mensagem("Token renovado com sucesso")
				.token(novoAccessToken)
				.refreshToken(novoRefreshToken)
				.usuario(usuarioMapper.toDTO(usuario))
				.expiresIn(tempoAteExpiracao)
				.expiresAt(expiracao)
				.build();

			logger.info("Token renovado com sucesso para usuário: {}", username);
			return response;

		} catch (Exception e) {
			logger.error("Erro ao renovar token", e);
			return LoginResponseDTO.builder()
				.sucesso(false)
				.mensagem("Erro ao renovar token")
				.erro(e.getMessage())
				.build();
		}
	}

	/**
	 * Invalida um token adicionando-o à blacklist
	 * Nota: Implementação com Redis será feita em Sprint 3
	 */
	@Override
	public void invalidarToken(String token) {
		logger.info("Token adicionado à blacklist");
		// TODO: Implementar com Redis para melhor performance
		// redisTemplate.opsForValue().set("blacklist:" + token, true, Duration.ofHours(24));
	}

	/**
	 * Verifica se um token está na blacklist
	 */
	@Override
	public boolean estaNaBlacklist(String token) {
		// TODO: Implementar com Redis
		return false;
	}

	/**
	 * Registra logout em auditoria
	 */
	@Override
	public void registrarLogout(Long usuarioId, String username) {
		try {
			Usuario usuario = usuarioRepository.findById(usuarioId)
				.orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

			UsuarioHistorico historico = new UsuarioHistorico(
				usuario, "LOGOUT", "Logout realizado com sucesso", (String) null, (String) null);

			usuarioHistoricoRepository.save(historico);
			logger.info("Evento LOGOUT registrado para usuário: {}", username);

		} catch (Exception e) {
			logger.error("Erro ao registrar logout", e);
		}
	}

	/**
	 * Valida a requisição de login
	 */
	private void validarLoginRequest(LoginRequestDTO loginRequest) throws BadCredentialsException {
		if (loginRequest.getUsername() == null || loginRequest.getUsername().isEmpty()) {
			throw new BadCredentialsException("Username é obrigatório");
		}

		if (loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty()) {
			throw new BadCredentialsException("Password é obrigatório");
		}

		// Para Tipo 2 e 3, sistema é obrigatório
		if ((loginRequest.getSistema() != null || loginRequest.getUnidade() != null)
			&& (loginRequest.getSistema() == null || loginRequest.getSistema().isEmpty())) {
			throw new BadCredentialsException("Sistema é obrigatório para Tipo 2/3");
		}
	}

	/**
	 * Cria o token de autenticação apropriado baseado na requisição
	 */
	private Authentication criarTokenAutenticacao(LoginRequestDTO loginRequest) {
		String username = loginRequest.getUsername();
		String password = loginRequest.getPassword();
		String sistema = loginRequest.getSistema();
		String unidade = loginRequest.getUnidade();

		// Tipo 3: Com módulo e unidade
		if (sistema != null && !sistema.isEmpty() && unidade != null && !unidade.isEmpty()) {
			logger.info("Criando token Tipo 3: username + password + sistema + unidade");

			java.util.Map<String, Object> params = new java.util.HashMap<>();
			params.put("sistema", sistema);
			params.put("unidade", unidade);

			ExtendedAuthenticationToken token = new ExtendedAuthenticationToken(
				username, password, sistema, unidade, params);

			return token;
		}

		// Tipo 2: Com módulo
		if (sistema != null && !sistema.isEmpty()) {
			logger.info("Criando token Tipo 2: username + password + sistema");

			CustomUsernamePasswordAuthenticationToken token = new CustomUsernamePasswordAuthenticationToken(
				username, password, sistema, null, null);

			return token;
		}

		// Tipo 1: Apenas username + password
		logger.info("Criando token Tipo 1: username + password");
		return new UsernamePasswordAuthenticationToken(username, password);
	}

	/**
	 * Gera o access token apropriado baseado no tipo de login
	 */
	private String gerarAccessToken(UserDetailsImpl userDetails, LoginRequestDTO loginRequest, List<String> perfis) {
		String sistema = loginRequest.getSistema();
		String unidade = loginRequest.getUnidade();

		// Tipo 3: Com módulo e unidade
		if (sistema != null && !sistema.isEmpty() && unidade != null && !unidade.isEmpty()) {
			logger.debug("Gerando access token Tipo 3");
			return tokenService.gerarTokenComSistemaEUnidade(
				userDetails.getId(),
				userDetails.getUsername(),
				userDetails.getEmail(),
				sistema,
				unidade,
				perfis);
		}

		// Tipo 2: Com módulo
		if (sistema != null && !sistema.isEmpty()) {
			logger.debug("Gerando access token Tipo 2");
			return tokenService.gerarTokenComSistema(
				userDetails.getId(),
				userDetails.getUsername(),
				userDetails.getEmail(),
				sistema,
				perfis);
		}

		// Tipo 1: Sem módulo/unidade
		logger.debug("Gerando access token Tipo 1");
		return tokenService.gerarToken(
			userDetails.getId(),
			userDetails.getUsername(),
			userDetails.getEmail(),
			perfis);
	}

	/**
	 * Registra login bem-sucedido em auditoria
	 */
	private void registrarLoginSucesso(Usuario usuario, LoginRequestDTO loginRequest) {
		try {
			String tipo = loginRequest.getUnidade() != null && !loginRequest.getUnidade().isEmpty()
				? "3"
				: (loginRequest.getSistema() != null && !loginRequest.getSistema().isEmpty() ? "2" : "1");

			String descricao = "Login bem-sucedido - Tipo " + tipo;

			UsuarioHistorico historico = new UsuarioHistorico(
				usuario,
				"LOGIN_SUCESSO",
				descricao,
				loginRequest.getSistema(),
				loginRequest.getUnidade());

			usuarioHistoricoRepository.save(historico);
			logger.debug("Evento LOGIN_SUCESSO registrado para usuário: {}", usuario.getUsername());

		} catch (Exception e) {
			logger.error("Erro ao registrar login sucesso em auditoria", e);
		}
	}
}
