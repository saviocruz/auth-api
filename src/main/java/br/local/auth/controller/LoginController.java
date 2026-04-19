package br.local.auth.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.local.auth.dto.LoginRequestDTO;
import br.local.auth.dto.LoginResponseDTO;
import br.local.auth.security.AuthService;
import br.local.auth.security.UserDetailsImpl;
import jakarta.validation.Valid;

/**
 * Controller responsável por endpoints de autenticação.
 *
 * Endpoints:
 * - POST /api/v1/auth/login: Autenticar usuário (Tipo 1, 2 ou 3)
 * - POST /api/v1/auth/refresh: Renovar access token usando refresh token
 * - POST /api/v1/auth/logout: Fazer logout do usuário
 */
@RestController
@RequestMapping("/api/v1/auth")
public class LoginController {

	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

	@Autowired
	private AuthService authService;

	/**
	 * Endpoint de login que suporta 3 tipos de autenticação
	 *
	 * Tipo 1: Apenas username + password
	 * Tipo 2: username + password + sistema (módulo)
	 * Tipo 3: username + password + sistema + unidade
	 *
	 * @param loginRequest Requisição contendo credenciais
	 * @return LoginResponseDTO com tokens e dados do usuário
	 */
	@PostMapping("/login")
	public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
		logger.info("Requisição de login recebida para usuário: {}", loginRequest.getUsername());

		try {
			// Detectar tipo de login
			String tipo = detectarTipoLogin(loginRequest);
			logger.info("Tipo de login detectado: {}", tipo);

			// Autenticar usuário
			LoginResponseDTO response = authService.autenticar(loginRequest);

			// Retornar resposta apropriada
			if (response.isSucesso()) {
				logger.info("Login bem-sucedido para usuário: {}", loginRequest.getUsername());
				return ResponseEntity.ok(response);
			} else {
				logger.warn("Falha de login para usuário: {} - Motivo: {}",
					loginRequest.getUsername(), response.getMensagem());
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
			}

		} catch (Exception e) {
			logger.error("Erro ao processar login", e);

			LoginResponseDTO errorResponse = new LoginResponseDTO();
			errorResponse.setSucesso(false);
			errorResponse.setMensagem("Erro ao processar requisição de login");
			errorResponse.setErro(e.getMessage());

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}

	/**
	 * Endpoint para renovar access token usando refresh token
	 *
	 * @param authHeader Header Authorization contendo "Bearer <refresh-token>"
	 * @return LoginResponseDTO com novo access token
	 */
	@PostMapping("/refresh")
	public ResponseEntity<LoginResponseDTO> refresh(
			@RequestHeader(value = "Authorization", required = false) String authHeader) {
		logger.info("Requisição de refresh token recebida");

		try {
			// Extrair refresh token do header
			String refreshToken = extrairTokenDoHeader(authHeader);

			if (refreshToken == null || refreshToken.isEmpty()) {
				logger.warn("Refresh token não fornecido");

				LoginResponseDTO errorResponse = new LoginResponseDTO();
				errorResponse.setSucesso(false);
				errorResponse.setMensagem("Refresh token não fornecido");
				errorResponse.setErro("Authorization header ausente ou inválido");

				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
			}

			// Renovar token
			LoginResponseDTO response = authService.renovarToken(refreshToken);

			if (response.isSucesso()) {
				logger.info("Token renovado com sucesso");
				return ResponseEntity.ok(response);
			} else {
				logger.warn("Falha ao renovar token: {}", response.getMensagem());
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
			}

		} catch (Exception e) {
			logger.error("Erro ao renovar token", e);

			LoginResponseDTO errorResponse = new LoginResponseDTO();
			errorResponse.setSucesso(false);
			errorResponse.setMensagem("Erro ao renovar token");
			errorResponse.setErro(e.getMessage());

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}

	/**
	 * Endpoint para logout do usuário
	 * Remove token da blacklist e registra evento de logout
	 *
	 * @param authHeader Header Authorization contendo "Bearer <token>"
	 * @return Mensagem de sucesso
	 */
	@PostMapping("/logout")
	public ResponseEntity<?> logout(
			@RequestHeader(value = "Authorization", required = false) String authHeader) {
		logger.info("Requisição de logout recebida");

		try {
			// Obter autenticação atual do SecurityContext
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

			if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
				UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

				// Registrar logout em auditoria
				authService.registrarLogout(userDetails.getId(), userDetails.getUsername());

				// Extrair token e adicionar à blacklist
				String token = extrairTokenDoHeader(authHeader);
				if (token != null && !token.isEmpty()) {
					authService.invalidarToken(token);
				}

				// Limpar SecurityContext
				SecurityContextHolder.clearContext();

				logger.info("Logout realizado com sucesso para usuário: {}", userDetails.getUsername());

				return ResponseEntity.ok(new java.util.HashMap<String, Object>() {{
					put("sucesso", true);
					put("mensagem", "Logout realizado com sucesso");
				}});

			} else {
				logger.warn("Nenhuma autenticação encontrada no SecurityContext");
				return ResponseEntity.ok(new java.util.HashMap<String, Object>() {{
					put("sucesso", true);
					put("mensagem", "Logout realizado");
				}});
			}

		} catch (Exception e) {
			logger.error("Erro ao processar logout", e);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
				new java.util.HashMap<String, Object>() {{
					put("sucesso", false);
					put("mensagem", "Erro ao realizar logout");
					put("erro", e.getMessage());
				}}
			);
		}
	}

	/**
	 * Detecta o tipo de login baseado na requisição
	 * Tipo 1: Apenas username + password
	 * Tipo 2: username + password + sistema
	 * Tipo 3: username + password + sistema + unidade
	 */
	private String detectarTipoLogin(LoginRequestDTO loginRequest) {
		if (loginRequest.getUnidade() != null && !loginRequest.getUnidade().isEmpty()) {
			return "Tipo 3 (username + password + sistema + unidade)";
		}

		if (loginRequest.getSistema() != null && !loginRequest.getSistema().isEmpty()) {
			return "Tipo 2 (username + password + sistema)";
		}

		return "Tipo 1 (username + password)";
	}

	/**
	 * Extrai o token do header Authorization
	 * Formato: "Bearer <token>"
	 */
	private String extrairTokenDoHeader(String authHeader) {
		if (authHeader == null || authHeader.isEmpty()) {
			return null;
		}

		if (authHeader.startsWith("Bearer ")) {
			return authHeader.substring("Bearer ".length());
		}

		return null;
	}
}
