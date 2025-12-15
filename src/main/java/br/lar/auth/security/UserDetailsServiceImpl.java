package br.lar.auth.security;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lar.auth.dto.PerfilDTO;
import br.lar.auth.dto.UnidadeDTO;
import br.lar.auth.mapper.PerfilMapper;
import br.lar.auth.mapper.UnidadeMapper;
import br.lar.auth.model.Modulo;
import br.lar.auth.model.Unidade;
import br.lar.auth.model.Usuario;
import br.lar.auth.repository.ModuloRepository;
import br.lar.auth.repository.PerfilRepository;
import br.lar.auth.repository.UnidadeRepository;
import br.lar.auth.repository.UsuarioRepository;

/**
 * Implementação de CustomUserDetailsService
 *
 * Responsável por carregar detalhes do usuário do banco de dados
 * com validações de acesso a módulos e unidades
 */
@Service
@Transactional
public class UserDetailsServiceImpl implements CustomUserDetailsService {

	private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private PerfilRepository perfilRepository;

	@Autowired
	private ModuloRepository moduloRepository;

	@Autowired
	private UnidadeRepository unidadeRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private PerfilMapper perfilMapper;

	@Autowired
	private UnidadeMapper unidadeMapper;

	/**
	 * Tipo 1: Carrega usuário com todos os perfis
	 */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		logger.info("Carregando usuário: {}", username);

		Usuario usuario = usuarioRepository.findByUsername(username)
			.orElseThrow(() -> {
				logger.warn("Usuário não encontrado: {}", username);
				return new UsernameNotFoundException("Usuário não encontrado: " + username);
			});

		if (!usuario.podeLogin()) {
			logger.warn("Usuário não pode fazer login: {} (status: {})", username, usuario.getStatus());
			throw new UsernameNotFoundException("Usuário não pode fazer login");
		}

		// Carregar TODOS os perfis do usuário (sem filtro de módulo)
		List<PerfilDTO> perfis = usuario.getPerfis().stream()
			.map(perfilMapper::toDTO)
			.collect(Collectors.toList());

		logger.info("Usuário carregado com sucesso: {} (perfis: {})", username, perfis.size());
		return UserDetailsImpl.build(usuario, perfis);
	}

	/**
	 * Tipo 2: Carrega usuário com perfis de um módulo específico
	 */
	@Override
	public UserDetailsImpl loadUserByUsernameAndPasswordAndSistema(
		String username, String password, String sistema) throws UsernameNotFoundException {

		logger.info("Carregando usuário com módulo - username: {}, sistema: {}", username, sistema);

		// Buscar usuário
		Usuario usuario = usuarioRepository.findByUsername(username)
			.orElseThrow(() -> {
				logger.warn("Usuário não encontrado: {}", username);
				return new UsernameNotFoundException("Usuário não encontrado");
			});

		// Validar status
		if (!usuario.podeLogin()) {
			logger.warn("Usuário não pode fazer login: {} (status: {})", username, usuario.getStatus());
			throw new UsernameNotFoundException("Usuário não pode fazer login");
		}

		// Validar password
		if (password != null && !passwordEncoder.matches(password, usuario.getChave())) {
			logger.warn("Senha inválida para usuário: {}", username);
			throw new UsernameNotFoundException("Credenciais inválidas");
		}

		// Buscar e validar módulo
		Modulo modulo = moduloRepository.findByDescricaoAndStatus(sistema, "ATIVO")
			.orElseThrow(() -> {
				logger.warn("Módulo não encontrado ou inativo: {}", sistema);
				return new UsernameNotFoundException("Módulo não encontrado: " + sistema);
			});

		// Filtrar perfis do usuário apenas do módulo solicitado
		List<PerfilDTO> perfis = usuario.getPerfis().stream()
			.filter(p -> p.getModulo().getId().equals(modulo.getId()))
			.map(perfilMapper::toDTO)
			.collect(Collectors.toList());

		if (perfis.isEmpty()) {
			logger.warn("Usuário {} não tem acesso ao módulo {}", username, sistema);
			throw new UsernameNotFoundException("Acesso ao módulo negado");
		}

		logger.info("Usuário carregado com módulo: {} (módulo: {})", username, sistema);
		return UserDetailsImpl.buildWithScope(usuario, perfis, sistema, null);
	}

	/**
	 * Tipo 3: Carrega usuário com perfis de um módulo + unidade específicos
	 */
	@Override
	public UserDetailsImpl loadUserByUsernameAndPasswordAndSistema(
		String username, String password, String sistema, String unidade) throws UsernameNotFoundException {

		logger.info("Carregando usuário com módulo+unidade - username: {}, sistema: {}, unidade: {}",
			username, sistema, unidade);

		// Buscar usuário
		Usuario usuario = usuarioRepository.findByUsername(username)
			.orElseThrow(() -> {
				logger.warn("Usuário não encontrado: {}", username);
				return new UsernameNotFoundException("Usuário não encontrado");
			});

		// Validar status
		if (!usuario.podeLogin()) {
			logger.warn("Usuário não pode fazer login: {} (status: {})", username, usuario.getStatus());
			throw new UsernameNotFoundException("Usuário não pode fazer login");
		}

		// Validar password
		if (password != null && !passwordEncoder.matches(password, usuario.getChave())) {
			logger.warn("Senha inválida para usuário: {}", username);
			throw new UsernameNotFoundException("Credenciais inválidas");
		}

		// Buscar e validar módulo
		Modulo modulo = moduloRepository.findByDescricaoAndStatus(sistema, "ATIVO")
			.orElseThrow(() -> {
				logger.warn("Módulo não encontrado: {}", sistema);
				return new UsernameNotFoundException("Módulo não encontrado");
			});

		// Buscar e validar unidade
		Unidade unidadeObj = unidadeRepository.findBySigla(unidade)
			.orElseThrow(() -> {
				logger.warn("Unidade não encontrada: {}", unidade);
				return new UsernameNotFoundException("Unidade não encontrada");
			});

		//if (!unidadeObj.isAtiva()) {
		//	logger.warn("Unidade inativa: {}", unidade);
		//	throw new UsernameNotFoundException("Unidade inativa");
		//}

		// Validar que usuário tem acesso à unidade
		if (!usuario.getUnidades().contains(unidadeObj)) {
			logger.warn("Usuário {} não tem acesso à unidade {}", username, unidade);
			throw new UsernameNotFoundException("Acesso à unidade negado");
		}

		// Filtrar perfis do usuário apenas do módulo solicitado
		List<PerfilDTO> perfis = usuario.getPerfis().stream()
			.filter(p -> p.getModulo().getId().equals(modulo.getId()))
			.map(perfilMapper::toDTO)
			.collect(Collectors.toList());

		if (perfis.isEmpty()) {
			logger.warn("Usuário {} não tem acesso ao módulo {}", username, sistema);
			throw new UsernameNotFoundException("Acesso ao módulo negado");
		}

		UnidadeDTO unidadeDTO = unidadeMapper.toDTO(unidadeObj);
		logger.info("Usuário carregado com módulo+unidade: {} (módulo: {}, unidade: {})",
			username, sistema, unidade);

		return UserDetailsImpl.buildWithScope(usuario, perfis, sistema, unidadeDTO);
	}

	/**
	 * Tipo 2: Carrega usuário com módulo (sem validar password)
	 */
	@Override
	public UserDetailsImpl loadUserByUsernameAndSistema(
		String username, String sistema) throws UsernameNotFoundException {

		logger.info("Carregando usuário com módulo (sem password) - username: {}, sistema: {}", username, sistema);

		Usuario usuario = usuarioRepository.findByUsername(username)
			.orElseThrow(() -> {
				logger.warn("Usuário não encontrado: {}", username);
				return new UsernameNotFoundException("Usuário não encontrado");
			});

		if (!usuario.podeLogin()) {
			logger.warn("Usuário não pode fazer login: {} (status: {})", username, usuario.getStatus());
			throw new UsernameNotFoundException("Usuário não pode fazer login");
		}

		Modulo modulo = moduloRepository.findByDescricaoAndStatus(sistema, "ATIVO")
			.orElseThrow(() -> {
				logger.warn("Módulo não encontrado: {}", sistema);
				return new UsernameNotFoundException("Módulo não encontrado");
			});

		List<PerfilDTO> perfis = usuario.getPerfis().stream()
			.filter(p -> p.getModulo().getId().equals(modulo.getId()))
			.map(perfilMapper::toDTO)
			.collect(Collectors.toList());

		if (perfis.isEmpty()) {
			logger.warn("Usuário {} não tem acesso ao módulo {}", username, sistema);
			throw new UsernameNotFoundException("Acesso ao módulo negado");
		}

		return UserDetailsImpl.buildWithScope(usuario, perfis, sistema, null);
	}

	/**
	 * Tipo 3: Carrega usuário com módulo+unidade (sem validar password)
	 */
	@Override
	public UserDetailsImpl loadUserByUsernameAndSistema(
		String username, String sistema, String unidade) throws UsernameNotFoundException {

		logger.info("Carregando usuário com módulo+unidade (sem password) - username: {}, sistema: {}, unidade: {}",
			username, sistema, unidade);

		Usuario usuario = usuarioRepository.findByUsername(username)
			.orElseThrow(() -> {
				logger.warn("Usuário não encontrado: {}", username);
				return new UsernameNotFoundException("Usuário não encontrado");
			});

		if (!usuario.podeLogin()) {
			throw new UsernameNotFoundException("Usuário não pode fazer login");
		}

		Modulo modulo = moduloRepository.findByDescricaoAndStatus(sistema, "ATIVO")
			.orElseThrow(() -> new UsernameNotFoundException("Módulo não encontrado"));

		Unidade unidadeObj = unidadeRepository.findBySigla(unidade)
			.orElseThrow(() -> new UsernameNotFoundException("Unidade não encontrada"));

		//if (!unidadeObj.isAtiva()) {
		//	throw new UsernameNotFoundException("Unidade inativa");
		//}

		if (!usuario.getUnidades().contains(unidadeObj)) {
			logger.warn("Usuário {} não tem acesso à unidade {}", username, unidade);
			throw new UsernameNotFoundException("Acesso à unidade negado");
		}

		List<PerfilDTO> perfis = usuario.getPerfis().stream()
			.filter(p -> p.getModulo().getId().equals(modulo.getId()))
			.map(perfilMapper::toDTO)
			.collect(Collectors.toList());

		if (perfis.isEmpty()) {
			throw new UsernameNotFoundException("Acesso ao módulo negado");
		}

		UnidadeDTO unidadeDTO = unidadeMapper.toDTO(unidadeObj);
		return UserDetailsImpl.buildWithScope(usuario, perfis, sistema, unidadeDTO);
	}
}
