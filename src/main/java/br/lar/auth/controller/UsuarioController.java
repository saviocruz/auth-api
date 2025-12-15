package br.lar.auth.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.lar.auth.dto.UsuarioDTO;
import br.lar.auth.service.UsuarioService;
import jakarta.validation.Valid;

/**
 * Controller responsável por endpoints CRUD de usuários.
 *
 * Endpoints:
 * - GET /api/v1/usuarios: Listar usuários com paginação e filtros
 * - GET /api/v1/usuarios/{id}: Obter detalhes de um usuário
 * - POST /api/v1/usuarios: Criar novo usuário
 * - PUT /api/v1/usuarios/{id}: Atualizar dados do usuário
 * - DELETE /api/v1/usuarios/{id}: Deletar usuário (soft delete)
 * - PUT /api/v1/usuarios/{id}/perfis: Alterar perfis do usuário
 * - PUT /api/v1/usuarios/{id}/unidades: Alterar unidades do usuário
 * - PUT /api/v1/usuarios/{id}/bloquear: Bloquear usuário
 * - PUT /api/v1/usuarios/{id}/desbloquear: Desbloquear usuário
 * - PUT /api/v1/usuarios/{id}/alterar-senha: Alterar senha do usuário
 * - GET /api/v1/usuarios/{id}/historico-logins: Obter histórico de logins
 */
@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

	private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);

	@Autowired
	private UsuarioService usuarioService;

	/**
	 * Listar todos os usuários com paginação e filtros
	 *
	 * Parâmetros de query:
	 * - page: Número da página (0-indexed)
	 * - size: Tamanho da página
	 * - sort: Campo para ordenação (ex: username,desc)
	 * - status: Filtro por status (ATIVO, INATIVO, BLOQUEADO)
	 * - busca: Filtro por username/nome/cpf/email
	 */
	@GetMapping
	@PreAuthorize("hasAuthority('ADMIN_GERAL')")
	public ResponseEntity<Page<UsuarioDTO>> listarUsuarios(
			Pageable pageable,
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String busca) {
		logger.info("Listando usuários - status: {}, busca: {}", status, busca);

		try {
			Page<UsuarioDTO> usuarios = usuarioService.listarUsuarios(pageable, status, busca);
			return ResponseEntity.ok(usuarios);

		} catch (Exception e) {
			logger.error("Erro ao listar usuários", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	/**
	 * Obter detalhes de um usuário específico
	 */
	@GetMapping("/{id}")
	@PreAuthorize("hasAuthority('ADMIN_GERAL') or @securityService.isCurrentUser(#id)")
	public ResponseEntity<UsuarioDTO> obterUsuario(@PathVariable Long id) {
		logger.info("Obtendo usuário: {}", id);

		try {
			UsuarioDTO usuario = usuarioService.obterUsuario(id);
			return ResponseEntity.ok(usuario);

		} catch (RuntimeException e) {
			logger.error("Usuário não encontrado: {}", id);
			return ResponseEntity.notFound().build();

		} catch (Exception e) {
			logger.error("Erro ao obter usuário", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	/**
	 * Criar novo usuário
	 */
	@PostMapping
	@PreAuthorize("hasAuthority('ADMIN_GERAL')")
	public ResponseEntity<UsuarioDTO> criarUsuario(@Valid @RequestBody UsuarioDTO usuarioDTO) {
		logger.info("Criando novo usuário: {}", usuarioDTO.getUsername());

		try {
			UsuarioDTO usuarioCriado = usuarioService.criarUsuario(usuarioDTO);
			return ResponseEntity.status(HttpStatus.CREATED).body(usuarioCriado);

		} catch (RuntimeException e) {
			logger.warn("Erro ao criar usuário: {}", e.getMessage());
			return ResponseEntity.badRequest().build();

		} catch (Exception e) {
			logger.error("Erro ao criar usuário", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	/**
	 * Atualizar dados de um usuário
	 */
	@PutMapping("/{id}")
	@PreAuthorize("hasAuthority('ADMIN_GERAL') or @securityService.isCurrentUser(#id)")
	public ResponseEntity<UsuarioDTO> atualizarUsuario(
			@PathVariable Long id,
			@Valid @RequestBody UsuarioDTO usuarioDTO) {
		logger.info("Atualizando usuário: {}", id);

		try {
			UsuarioDTO usuarioAtualizado = usuarioService.atualizarUsuario(id, usuarioDTO);
			return ResponseEntity.ok(usuarioAtualizado);

		} catch (RuntimeException e) {
			logger.warn("Erro ao atualizar usuário: {}", e.getMessage());
			return ResponseEntity.notFound().build();

		} catch (Exception e) {
			logger.error("Erro ao atualizar usuário", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	/**
	 * Deletar um usuário (soft delete)
	 */
	@DeleteMapping("/{id}")
	@PreAuthorize("hasAuthority('ADMIN_GERAL')")
	public ResponseEntity<Void> deletarUsuario(@PathVariable Long id) {
		logger.info("Deletando usuário: {}", id);

		try {
			usuarioService.deletarUsuario(id);
			return ResponseEntity.noContent().build();

		} catch (RuntimeException e) {
			logger.warn("Usuário não encontrado: {}", id);
			return ResponseEntity.notFound().build();

		} catch (Exception e) {
			logger.error("Erro ao deletar usuário", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	/**
	 * Alterar perfis (roles) de um usuário
	 */
	@PutMapping("/{id}/perfis")
	@PreAuthorize("hasAuthority('ADMIN_GERAL')")
	public ResponseEntity<UsuarioDTO> alterarPerfis(
			@PathVariable Long id,
			@RequestBody List<Long> perfilIds) {
		logger.info("Alterando perfis do usuário: {} - Perfis: {}", id, perfilIds);

		try {
			UsuarioDTO usuarioAtualizado = usuarioService.alterarPerfis(id, perfilIds);
			return ResponseEntity.ok(usuarioAtualizado);

		} catch (RuntimeException e) {
			logger.warn("Erro ao alterar perfis: {}", e.getMessage());
			return ResponseEntity.notFound().build();

		} catch (Exception e) {
			logger.error("Erro ao alterar perfis", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	/**
	 * Alterar unidades de um usuário
	 */
	@PutMapping("/{id}/unidades")
	@PreAuthorize("hasAuthority('ADMIN_GERAL')")
	public ResponseEntity<UsuarioDTO> alterarUnidades(
			@PathVariable Long id,
			@RequestBody List<Long> unidadeIds) {
		logger.info("Alterando unidades do usuário: {} - Unidades: {}", id, unidadeIds);

		try {
			UsuarioDTO usuarioAtualizado = usuarioService.alterarUnidades(id, unidadeIds);
			return ResponseEntity.ok(usuarioAtualizado);

		} catch (RuntimeException e) {
			logger.warn("Erro ao alterar unidades: {}", e.getMessage());
			return ResponseEntity.notFound().build();

		} catch (Exception e) {
			logger.error("Erro ao alterar unidades", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	/**
	 * Bloquear um usuário
	 */
	@PutMapping("/{id}/bloquear")
	@PreAuthorize("hasAuthority('ADMIN_GERAL')")
	public ResponseEntity<UsuarioDTO> bloquearUsuario(@PathVariable Long id) {
		logger.info("Bloqueando usuário: {}", id);

		try {
			UsuarioDTO usuarioBloqueado = usuarioService.bloquearUsuario(id);
			return ResponseEntity.ok(usuarioBloqueado);

		} catch (RuntimeException e) {
			logger.warn("Usuário não encontrado: {}", id);
			return ResponseEntity.notFound().build();

		} catch (Exception e) {
			logger.error("Erro ao bloquear usuário", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	/**
	 * Desbloquear um usuário
	 */
	@PutMapping("/{id}/desbloquear")
	@PreAuthorize("hasAuthority('ADMIN_GERAL')")
	public ResponseEntity<UsuarioDTO> desbloquearUsuario(@PathVariable Long id) {
		logger.info("Desbloqueando usuário: {}", id);

		try {
			UsuarioDTO usuarioDesbloqueado = usuarioService.desbloquearUsuario(id);
			return ResponseEntity.ok(usuarioDesbloqueado);

		} catch (RuntimeException e) {
			logger.warn("Usuário não encontrado: {}", id);
			return ResponseEntity.notFound().build();

		} catch (Exception e) {
			logger.error("Erro ao desbloquear usuário", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	/**
	 * Alterar senha de um usuário
	 */
	@PutMapping("/{id}/alterar-senha")
	@PreAuthorize("hasAuthority('ADMIN_GERAL') or @securityService.isCurrentUser(#id)")
	public ResponseEntity<?> alterarSenha(
			@PathVariable Long id,
			@RequestBody java.util.Map<String, String> request) {
		logger.info("Alterando senha do usuário: {}", id);

		try {
			String senhaAtual = request.get("senhaAtual");
			String novaSenha = request.get("novaSenha");

			if (senhaAtual == null || senhaAtual.isEmpty() || novaSenha == null || novaSenha.isEmpty()) {
				logger.warn("Senhas não fornecidas");
				return ResponseEntity.badRequest().body(
					new java.util.HashMap<String, String>() {{
						put("erro", "Senha atual e nova senha são obrigatórias");
					}}
				);
			}

			usuarioService.alterarSenha(id, senhaAtual, novaSenha);

			return ResponseEntity.ok(
				new java.util.HashMap<String, String>() {{
					put("sucesso", "true");
					put("mensagem", "Senha alterada com sucesso");
				}}
			);

		} catch (RuntimeException e) {
			logger.warn("Erro ao alterar senha: {}", e.getMessage());
			return ResponseEntity.badRequest().body(
				new java.util.HashMap<String, String>() {{
					put("erro", e.getMessage());
				}}
			);

		} catch (Exception e) {
			logger.error("Erro ao alterar senha", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	/**
	 * Obter histórico de logins de um usuário
	 */
	@GetMapping("/{id}/historico-logins")
	@PreAuthorize("hasAuthority('ADMIN_GERAL') or @securityService.isCurrentUser(#id)")
	public ResponseEntity<?> obterHistoricoLogins(
			@PathVariable Long id,
			@RequestParam(defaultValue = "10") int limite) {
		logger.info("Obtendo histórico de logins do usuário: {}", id);

		try {
			List<java.util.Map<String, Object>> historico = usuarioService.obterHistoricoLogins(id, limite);
			return ResponseEntity.ok(historico);

		} catch (Exception e) {
			logger.error("Erro ao obter histórico de logins", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
}
