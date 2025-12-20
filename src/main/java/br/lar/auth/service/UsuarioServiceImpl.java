package br.lar.auth.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lar.auth.dto.PerfilDTO;
import br.lar.auth.dto.UsuarioDTO;
import br.lar.auth.mapper.PerfilMapper;
import br.lar.auth.mapper.UnidadeMapper;
import br.lar.auth.mapper.UsuarioMapper;
import br.lar.auth.model.Perfil;
import br.lar.auth.model.Unidade;
import br.lar.auth.model.Usuario;
import br.lar.auth.model.UsuarioHistorico;
import br.lar.auth.utils.AtivoInativo;
import br.lar.auth.repository.PerfilRepository;
import br.lar.auth.repository.UnidadeRepository;
import br.lar.auth.repository.UsuarioHistoricoRepository;
import br.lar.auth.repository.UsuarioRepository;

/**
 * Implementação de UsuarioService para gerência de usuários.
 *
 * Responsabilidades:
 * 1. CRUD de usuários com validações
 * 2. Filtrar usuários por critérios
 * 3. Gerenciar perfis de usuário
 * 4. Gerenciar unidades de usuário
 * 5. Bloquear/desbloquear usuários
 * 6. Alterar senhas com encriptação
 * 7. Registrar eventos em auditoria
 */
@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

	private static final Logger logger = LoggerFactory.getLogger(UsuarioServiceImpl.class);

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private PerfilRepository perfilRepository;

	@Autowired
	private UnidadeRepository unidadeRepository;

	@Autowired
	private UsuarioHistoricoRepository usuarioHistoricoRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UsuarioMapper usuarioMapper;

	@Autowired
	private PerfilMapper perfilMapper;

	@Autowired
	private UnidadeMapper unidadeMapper;

	@Override
	@Transactional(readOnly = true)
	public Page<UsuarioDTO> listarUsuarios(Pageable pageable, String status, String busca) {
		logger.info("Listando usuários - status: {}, busca: {}", status, busca);

		Page<Usuario> usuarios;

		if (busca != null && !busca.isEmpty()) {
			// Buscar por username, nome, cpf ou email
			usuarios = usuarioRepository.buscarPorUsernameOuNomeOuCpf(busca, pageable);
		} else if (status != null && !status.isEmpty()) {
			// Filtrar por status
			usuarios = usuarioRepository.findByStatus(AtivoInativo.fromString(status), pageable);
		} else {
			// Listar todos
			usuarios = usuarioRepository.findAllOrderByNome(pageable);
		}

		return usuarios.map(usuarioMapper::toDTO);
	}

	@Override
	@Transactional(readOnly = true)
	public UsuarioDTO obterUsuario(Long usuarioId) {
		logger.info("Obtendo usuário: {}", usuarioId);

		Usuario usuario = usuarioRepository.findById(usuarioId)
			.orElseThrow(() -> {
				logger.error("Usuário não encontrado: {}", usuarioId);
				return new RuntimeException("Usuário não encontrado");
			});

		return usuarioMapper.toDTO(usuario);
	}

	@Override
	public UsuarioDTO criarUsuario(UsuarioDTO usuarioDTO) {
		logger.info("Criando novo usuário: {}", usuarioDTO.getUsername());

		// Validar duplicação
		if (usuarioRepository.findByUsername(usuarioDTO.getUsername()).isPresent()) {
			throw new RuntimeException("Username já existe: " + usuarioDTO.getUsername());
		}

		if (usuarioDTO.getCpf() != null && !usuarioDTO.getCpf().isEmpty()
			&& usuarioRepository.findByCpf(usuarioDTO.getCpf()).isPresent()) {
			throw new RuntimeException("CPF já cadastrado: " + usuarioDTO.getCpf());
		}

		if (usuarioDTO.getEmail() != null && !usuarioDTO.getEmail().isEmpty()
			&& usuarioRepository.findByEmail(usuarioDTO.getEmail()).isPresent()) {
			throw new RuntimeException("Email já cadastrado: " + usuarioDTO.getEmail());
		}

		// Criar entidade
		Usuario usuario = new Usuario();
		usuario.setUsername(usuarioDTO.getUsername());
		usuario.setCpf(usuarioDTO.getCpf());
		usuario.setNome(usuarioDTO.getNome());
		usuario.setEmail(usuarioDTO.getEmail());
		usuario.setMatricula(usuarioDTO.getMatricula());
		usuario.setStatus(AtivoInativo.ATIVO);
		usuario.setChave(passwordEncoder.encode("SENHA_PADRAO_123")); // Senha padrão criptografada
		usuario.setDataCadastro(new Date());
		usuario.setTentativasFalhas(0);

		Usuario usuarioCriado = usuarioRepository.save(usuario);

		// Registrar em auditoria
		registrarEvento(usuarioCriado, "CRIACAO", "Usuário criado com sucesso", null, null);

		logger.info("Usuário criado com sucesso: {}", usuarioCriado.getId());

		return usuarioMapper.toDTO(usuarioCriado);
	}

	@Override
	public UsuarioDTO atualizarUsuario(Long usuarioId, UsuarioDTO usuarioDTO) {
		logger.info("Atualizando usuário: {}", usuarioId);

		Usuario usuario = usuarioRepository.findById(usuarioId)
			.orElseThrow(() -> {
				logger.error("Usuário não encontrado: {}", usuarioId);
				return new RuntimeException("Usuário não encontrado");
			});

		// Validar duplicação de email (se diferente)
		if (usuarioDTO.getEmail() != null && !usuarioDTO.getEmail().isEmpty()
			&& !usuario.getEmail().equals(usuarioDTO.getEmail())
			&& usuarioRepository.findByEmail(usuarioDTO.getEmail()).isPresent()) {
			throw new RuntimeException("Email já cadastrado: " + usuarioDTO.getEmail());
		}

		// Atualizar campos básicos
		usuario.setNome(usuarioDTO.getNome());
		usuario.setEmail(usuarioDTO.getEmail());
		usuario.setMatricula(usuarioDTO.getMatricula());

		// Atualizar perfis se fornecidos no DTO
		if (usuarioDTO.getPerfis() != null && !usuarioDTO.getPerfis().isEmpty()) {
			Set<Perfil> perfis = new HashSet<>();
			for (PerfilDTO perfilDTO : usuarioDTO.getPerfis()) {
				if (perfilDTO.getId() != null) {
					Perfil perfil = perfilRepository.findById(perfilDTO.getId())
						.orElseThrow(() -> {
							logger.error("Perfil não encontrado: {}", perfilDTO.getId());
							return new RuntimeException("Perfil não encontrado: " + perfilDTO.getId());
						});
					perfis.add(perfil);
				}
			}
			usuario.setPerfis(perfis);
			logger.info("Perfis atualizados para usuário: {} - Total: {}", usuarioId, perfis.size());
		} else if (usuarioDTO.getPerfis() != null && usuarioDTO.getPerfis().isEmpty()) {
			// Se a lista de perfis está vazia, remover todos os perfis
			usuario.setPerfis(new HashSet<>());
			logger.info("Todos os perfis removidos do usuário: {}", usuarioId);
		}

		Usuario usuarioAtualizado = usuarioRepository.save(usuario);

		// Registrar em auditoria
		registrarEvento(usuarioAtualizado, "EDICAO", "Usuário atualizado", null, null);

		logger.info("Usuário atualizado com sucesso: {}", usuarioId);

		return usuarioMapper.toDTO(usuarioAtualizado);
	}

	@Override
	public void deletarUsuario(Long usuarioId) {
		logger.info("Deletando usuário (soft delete): {}", usuarioId);

		Usuario usuario = usuarioRepository.findById(usuarioId)
			.orElseThrow(() -> {
				logger.error("Usuário não encontrado: {}", usuarioId);
				return new RuntimeException("Usuário não encontrado");
			});

		// Soft delete: apenas mudar status
		usuario.setStatus(AtivoInativo.INATIVO);
		usuarioRepository.save(usuario);

		// Registrar em auditoria
		registrarEvento(usuario, "DELECAO", "Usuário desativado (soft delete)", null, null);

		logger.info("Usuário deletado com sucesso: {}", usuarioId);
	}

	@Override
	public UsuarioDTO alterarPerfis(Long usuarioId, List<Long> perfilIds) {
		logger.info("Alterando perfis do usuário: {} - Perfis: {}", usuarioId, perfilIds);

		Usuario usuario = usuarioRepository.findById(usuarioId)
			.orElseThrow(() -> {
				logger.error("Usuário não encontrado: {}", usuarioId);
				return new RuntimeException("Usuário não encontrado");
			});

		// Carregar perfis
		Set<Perfil> perfis = new HashSet<>();
		for (Long perfilId : perfilIds) {
			Perfil perfil = perfilRepository.findById(perfilId)
				.orElseThrow(() -> {
					logger.error("Perfil não encontrado: {}", perfilId);
					return new RuntimeException("Perfil não encontrado: " + perfilId);
				});
			perfis.add(perfil);
		}

		// Atualizar perfis
		usuario.setPerfis(perfis);
		Usuario usuarioAtualizado = usuarioRepository.save(usuario);

		// Registrar em auditoria
		String descricao = "Perfis alterados: " + perfilIds.stream()
			.map(String::valueOf)
			.collect(Collectors.joining(", "));
		registrarEvento(usuarioAtualizado, "ALTERACAO_PERFIS", descricao, null, null);

		logger.info("Perfis alterados com sucesso para usuário: {}", usuarioId);

		return usuarioMapper.toDTO(usuarioAtualizado);
	}

	@Override
	public UsuarioDTO alterarUnidades(Long usuarioId, List<Long> unidadeIds) {
		logger.info("Alterando unidades do usuário: {} - Unidades: {}", usuarioId, unidadeIds);

		Usuario usuario = usuarioRepository.findById(usuarioId)
			.orElseThrow(() -> {
				logger.error("Usuário não encontrado: {}", usuarioId);
				return new RuntimeException("Usuário não encontrado");
			});

		// Carregar unidades
		Set<Unidade> unidades = new HashSet<>();
		for (Long unidadeId : unidadeIds) {
			Unidade unidade = unidadeRepository.findById(unidadeId)
				.orElseThrow(() -> {
					logger.error("Unidade não encontrada: {}", unidadeId);
					return new RuntimeException("Unidade não encontrada: " + unidadeId);
				});
			unidades.add(unidade);
		}

		// Atualizar unidades
		usuario.setUnidades(unidades);
		Usuario usuarioAtualizado = usuarioRepository.save(usuario);

		// Registrar em auditoria
		String descricao = "Unidades alteradas: " + unidadeIds.stream()
			.map(String::valueOf)
			.collect(Collectors.joining(", "));
		registrarEvento(usuarioAtualizado, "ALTERACAO_UNIDADES", descricao, null, null);

		logger.info("Unidades alteradas com sucesso para usuário: {}", usuarioId);

		return usuarioMapper.toDTO(usuarioAtualizado);
	}

	@Override
	public UsuarioDTO bloquearUsuario(Long usuarioId) {
		logger.info("Bloqueando usuário: {}", usuarioId);

		Usuario usuario = usuarioRepository.findById(usuarioId)
			.orElseThrow(() -> {
				logger.error("Usuário não encontrado: {}", usuarioId);
				return new RuntimeException("Usuário não encontrado");
			});

		usuario.bloquear();
		Usuario usuarioBloqueado = usuarioRepository.save(usuario);

		// Registrar em auditoria
		registrarEvento(usuarioBloqueado, "BLOQUEIO", "Usuário bloqueado", null, null);

		logger.info("Usuário bloqueado com sucesso: {}", usuarioId);

		return usuarioMapper.toDTO(usuarioBloqueado);
	}

	@Override
	public UsuarioDTO desbloquearUsuario(Long usuarioId) {
		logger.info("Desbloqueando usuário: {}", usuarioId);

		Usuario usuario = usuarioRepository.findById(usuarioId)
			.orElseThrow(() -> {
				logger.error("Usuário não encontrado: {}", usuarioId);
				return new RuntimeException("Usuário não encontrado");
			});

		usuario.desbloquear();
		usuario.resetarTentativasFalhas();
		Usuario usuarioDesbloqueado = usuarioRepository.save(usuario);

		// Registrar em auditoria
		registrarEvento(usuarioDesbloqueado, "DESBLOQUEIO", "Usuário desbloqueado", null, null);

		logger.info("Usuário desbloqueado com sucesso: {}", usuarioId);

		return usuarioMapper.toDTO(usuarioDesbloqueado);
	}

	@Override
	public void alterarSenha(Long usuarioId, String senhaAtual, String novaSenha) {
		logger.info("Alterando senha do usuário: {}", usuarioId);

		Usuario usuario = usuarioRepository.findById(usuarioId)
			.orElseThrow(() -> {
				logger.error("Usuário não encontrado: {}", usuarioId);
				return new RuntimeException("Usuário não encontrado");
			});

		// Validar senha atual
		if (!passwordEncoder.matches(senhaAtual, usuario.getChave())) {
			logger.warn("Senha atual inválida para usuário: {}", usuarioId);
			throw new RuntimeException("Senha atual inválida");
		}

		// Atualizar senha
		usuario.setChave(passwordEncoder.encode(novaSenha));
		usuarioRepository.save(usuario);

		// Registrar em auditoria
		registrarEvento(usuario, "ALTERACAO_SENHA", "Senha alterada com sucesso", null, null);

		logger.info("Senha alterada com sucesso para usuário: {}", usuarioId);
	}

	@Override
	public void resetarSenha(Long usuarioId, String senhaPadrao) {
		logger.info("Resetando senha do usuário: {}", usuarioId);

		Usuario usuario = usuarioRepository.findById(usuarioId)
			.orElseThrow(() -> {
				logger.error("Usuário não encontrado: {}", usuarioId);
				return new RuntimeException("Usuário não encontrado");
			});

		usuario.setChave(passwordEncoder.encode(senhaPadrao));
		usuarioRepository.save(usuario);

		// Registrar em auditoria
		registrarEvento(usuario, "RESET_SENHA", "Senha resetada para padrão", null, null);

		logger.info("Senha resetada com sucesso para usuário: {}", usuarioId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Map<String, Object>> obterHistoricoLogins(Long usuarioId, int limite) {
		logger.info("Obtendo histórico de logins para usuário: {}", usuarioId);

		List<UsuarioHistorico> historicos = usuarioHistoricoRepository.findByUsuarioId(usuarioId,
			org.springframework.data.domain.PageRequest.of(0, limite)).getContent();

		List<Map<String, Object>> resultado = new ArrayList<>();
		for (UsuarioHistorico h : historicos) {
			Map<String, Object> map = new HashMap<>();
			map.put("tipoEvento", h.getTipoEvento());
			map.put("descricao", h.getDescricao());
			map.put("data", h.getDataSistema());
			map.put("modulo", h.getModulo());
			map.put("unidade", h.getUnidade());
			resultado.add(map);
		}

		return resultado;
	}

	@Override
	public void incrementarTentativasFalhas(Long usuarioId) {
		logger.info("Incrementando tentativas falhas para usuário: {}", usuarioId);

		Usuario usuario = usuarioRepository.findById(usuarioId)
			.orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

		usuario.incrementarTentativasFalhas();

		if (usuario.estaBloqueado()) {
			usuario.bloquear();
			logger.warn("Usuário bloqueado após múltiplas tentativas: {}", usuarioId);
		}

		usuarioRepository.save(usuario);
	}

	@Override
	public void resetarTentativasFalhas(Long usuarioId) {
		logger.info("Resetando tentativas falhas para usuário: {}", usuarioId);

		Usuario usuario = usuarioRepository.findById(usuarioId)
			.orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

		usuario.resetarTentativasFalhas();
		usuarioRepository.save(usuario);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean usernameExiste(String username) {
		return usuarioRepository.findByUsername(username).isPresent();
	}

	@Override
	@Transactional(readOnly = true)
	public boolean cpfExiste(String cpf) {
		return usuarioRepository.findByCpf(cpf).isPresent();
	}

	@Override
	@Transactional(readOnly = true)
	public boolean emailExiste(String email) {
		return usuarioRepository.findByEmail(email).isPresent();
	}

	/**
	 * Registra um evento em auditoria
	 */
	private void registrarEvento(Usuario usuario, String tipoEvento, String descricao,
			String modulo, String unidade) {
		try {
			UsuarioHistorico historico = new UsuarioHistorico(
				usuario, tipoEvento, descricao, modulo, unidade);

			usuarioHistoricoRepository.save(historico);
			logger.debug("Evento registrado em auditoria: {} para usuário: {}",
				tipoEvento, usuario.getUsername());

		} catch (Exception e) {
			logger.error("Erro ao registrar evento em auditoria", e);
		}
	}
}
