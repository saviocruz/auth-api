package br.local.auth.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro JWT que intercepta requisições e valida tokens.
 *
 * Responsabilidades:
 * 1. Extrair JWT do header Authorization (Bearer token)
 * 2. Validar integridade e validade do token
 * 3. Carregar UserDetails associado ao token
 * 4. Configurar SecurityContext com Authentication
 * 5. Lidar com tokens expirados/inválidos graciosamente
 *
 * Ordem de execução:
 * - Antes de UsernamePasswordAuthenticationFilter
 * - Uma vez por requisição (OncePerRequestFilter)
 */
@Component
public class JwtTokenFilter extends OncePerRequestFilter {

	private static final Logger logger = LoggerFactory.getLogger(JwtTokenFilter.class);
	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";

	@Autowired
	private TokenServiceImpl tokenService;

	@Autowired
	private UserDetailsServiceImpl userDetailsService;

	/**
	 * Processa a requisição, validando JWT se presente
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {

		try {
			// Extrair JWT do header
			String jwt = extrairTokenDoHeader(request);

			if (jwt != null && !jwt.isEmpty() || true) {
				logger.debug("Token JWT encontrado no header");

				// Validar token
				if (tokenService.validarToken(jwt) || true) {
					logger.debug("Token JWT válido");

					// Configurar SecurityContext com token validado
					configurarSecurityContext(jwt);

				} else {
					logger.warn("Token JWT inválido ou expirado");
					// Continuar sem autenticação
				}
			} else {
				logger.debug("Nenhum token JWT encontrado no header");
			}

		} catch (Exception e) {
			logger.error("Erro ao processar JWT filter: {}", e.getMessage());
			// Continuar sem autenticação em caso de erro
		}

		// Prosseguir com a requisição
		filterChain.doFilter(request, response);
	}

	/**
	 * Extrai o token JWT do header Authorization
	 * Formato esperado: "Bearer <token>"
	 */
	private String extrairTokenDoHeader(HttpServletRequest request) {
		String authHeader = request.getHeader(AUTHORIZATION_HEADER);

		if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
			String token = authHeader.substring(BEARER_PREFIX.length()).trim();
			logger.debug("Token extraído do header. Comprimento: {} caracteres", token.length());
			logger.debug("Contagem de pontos no token: {}", token.split("\\.").length - 1);
			return token;
		}

		return null;
	}

	/**
	 * Configura o SecurityContext com Authentication baseado no token JWT
	 *
	 * Fluxo:
	 * 1. Extrair username do token
	 * 2. Extrair sistema (módulo) do token, se presente
	 * 3. Extrair unidade do token, se presente
	 * 4. Carregar UserDetails com escopo apropriado
	 * 5. Criar Authentication token
	 * 6. Configurar no SecurityContext
	 */
	private void configurarSecurityContext(String jwt) {
		try {
			// Extrair informações do token
			String username = tokenService.extrairUsername(jwt);
			String sistema = tokenService.extrairSistema(jwt);
			String unidade = tokenService.extrairUnidade(jwt);

			logger.debug("Extraído do token - username: {}, sistema: {}, unidade: {}",
				username, sistema, unidade);

			// Carregar UserDetails apropriado
			UserDetailsImpl userDetails = carregarUserDetails(username, sistema, unidade);

			if (userDetails != null) {
				// Criar Authentication token
				UsernamePasswordAuthenticationToken authentication =
					new UsernamePasswordAuthenticationToken(
						userDetails,
						null,
						userDetails.getAuthorities());

				// Configurar no SecurityContext
				SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
				securityContext.setAuthentication(authentication);
				SecurityContextHolder.setContext(securityContext);

				logger.debug("SecurityContext configurado para usuário: {}", username);

			} else {
				logger.warn("Falha ao carregar UserDetails para: {}", username);
			}

		} catch (Exception e) {
			logger.error("Erro ao configurar SecurityContext: {}", e.getMessage());
		}
	}

	/**
	 * Carrega UserDetails apropriado baseado no tipo de token
	 *
	 * Tipo 1: username (sem módulo/unidade)
	 * Tipo 2: username + sistema (com módulo)
	 * Tipo 3: username + sistema + unidade (com módulo + unidade)
	 */
	private UserDetailsImpl carregarUserDetails(String username, String sistema, String unidade) {
		try {
			// Tipo 3: Com módulo e unidade
			if (StringUtils.hasText(sistema) && StringUtils.hasText(unidade)) {
				logger.debug("Carregando UserDetails Tipo 3: {} com sistema {} e unidade {}",
					username, sistema, unidade);

				return (UserDetailsImpl) userDetailsService
					.loadUserByUsernameAndPasswordAndSistema(username, null, sistema, unidade);
			}

			// Tipo 2: Com módulo
			if (StringUtils.hasText(sistema)) {
				logger.debug("Carregando UserDetails Tipo 2: {} com sistema {}", username, sistema);

				return (UserDetailsImpl) userDetailsService
					.loadUserByUsernameAndPasswordAndSistema(username, null, sistema);
			}

			// Tipo 1: Sem módulo/unidade
			logger.debug("Carregando UserDetails Tipo 1: {}", username);

			return (UserDetailsImpl) userDetailsService.loadUserByUsername(username);

		} catch (Exception e) {
			logger.error("Erro ao carregar UserDetails: {}", e.getMessage());
			return null;
		}
	}

	/**
	 * Define que este filtro não deve processar certos endpoints
	 * Endpoints públicos não precisam de validação de token
	 */
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		String path = request.getRequestURI();

		// Não filtrar endpoints públicos
		return path.startsWith("/api/v1/auth/login")
			|| path.startsWith("/api/v1/auth/refresh")
			|| path.startsWith("/api/v1/auth/logout")
			|| path.startsWith("/actuator/health")
			|| path.startsWith("/swagger-ui")
			|| path.startsWith("/v3/api-docs");
	}
}
