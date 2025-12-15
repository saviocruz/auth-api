package br.lar.auth.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lar.auth.dto.PerfilDTO;
import br.lar.auth.mapper.PerfilMapper;
import br.lar.auth.model.Perfil;
import br.lar.auth.repository.PerfilRepository;
import br.lar.auth.utils.AtivoInativo;

/**
 * Service implementation para operações de perfis.
 */
@Service
@Transactional
public class PerfilServiceImpl implements PerfilService {

    private static final Logger logger = LoggerFactory.getLogger(PerfilServiceImpl.class);

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private PerfilMapper perfilMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PerfilDTO> listarPerfisAtivos() {
        logger.debug("Listando todos os perfis ativos");

        try {
            List<Perfil> perfis = perfilRepository.findByStatus(AtivoInativo.ATIVO);
            logger.info("Encontrados {} perfis ativos", perfis.size());

            return perfis.stream()
                    .map(perfilMapper::toDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Erro ao listar perfis ativos", e);
            throw new RuntimeException("Erro ao listar perfis ativos", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerfilDTO> listarPerfisPorModulo(Long moduloId) {
        logger.debug("Listando perfis do módulo: {}", moduloId);

        if (moduloId == null) {
            throw new IllegalArgumentException("ID do módulo é obrigatório");
        }

        try {
            List<Perfil> perfis = perfilRepository.findByModuloIdAndStatus(moduloId, AtivoInativo.ATIVO);
            logger.info("Encontrados {} perfis ativos para o módulo {}", perfis.size(), moduloId);

            return perfis.stream()
                    .map(perfilMapper::toDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Erro ao listar perfis por módulo: {}", moduloId, e);
            throw new RuntimeException("Erro ao listar perfis por módulo", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerfilDTO> buscarPerfisPorNome(String nome) {
        logger.debug("Buscando perfis com nome: {}", nome);

        if (nome == null || nome.trim().isEmpty()) {
            logger.warn("Nome de busca vazio, retornando todos os perfis ativos");
            return listarPerfisAtivos();
        }

        try {
            // Busca por nome similar (case insensitive)
            String nomeLowerCase = nome.toLowerCase().trim();
            List<Perfil> perfis = perfilRepository.findByStatus(AtivoInativo.ATIVO).stream()
                    .filter(p -> p.getNome().toLowerCase().contains(nomeLowerCase))
                    .collect(Collectors.toList());

            logger.info("Encontrados {} perfis com nome contendo '{}'", perfis.size(), nome);

            return perfis.stream()
                    .map(perfilMapper::toDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Erro ao buscar perfis por nome: {}", nome, e);
            throw new RuntimeException("Erro ao buscar perfis por nome", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PerfilDTO obterPerfil(Long id) {
        logger.debug("Obtendo perfil: {}", id);

        if (id == null) {
            throw new IllegalArgumentException("ID do perfil é obrigatório");
        }

        try {
            Perfil perfil = perfilRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Perfil não encontrado: " + id));

            logger.info("Perfil encontrado: {}", perfil.getNome());
            return perfilMapper.toDTO(perfil);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrado")) {
                throw e;
            }
            logger.error("Erro ao obter perfil: {}", id, e);
            throw new RuntimeException("Erro ao obter perfil", e);
        }
    }

    @Override
    public PerfilDTO criarPerfil(PerfilDTO perfilDTO) {
        

        if (perfilDTO == null) {
            throw new IllegalArgumentException("Dados do perfil são obrigatórios");
        }

        if (perfilDTO.getNome() == null || perfilDTO.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do perfil é obrigatório");
        }
logger.debug("Criando novo perfil: {}", perfilDTO.getNome());
        try {
            // Verificar se já existe perfil com o mesmo nome
            if (perfilRepository.existsByNome(perfilDTO.getNome())) {
                throw new RuntimeException("Já existe um perfil com este nome: " + perfilDTO.getNome());
            }

            Perfil perfil = perfilMapper.toEntity(perfilDTO);
            perfil.setStatus(AtivoInativo.ATIVO);

            Perfil perfilSalvo = perfilRepository.save(perfil);
            logger.info("Perfil criado com sucesso: {}", perfilSalvo.getNome());

            return perfilMapper.toDTO(perfilSalvo);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("Já existe") || e.getMessage().contains("obrigatório")) {
                throw e;
            }
            logger.error("Erro ao criar perfil", e);
            throw new RuntimeException("Erro ao criar perfil", e);
        }
    }

    @Override
    public PerfilDTO atualizarPerfil(Long id, PerfilDTO perfilDTO) {
        logger.debug("Atualizando perfil: {}", id);

        if (id == null) {
            throw new IllegalArgumentException("ID do perfil é obrigatório");
        }

        if (perfilDTO == null) {
            throw new IllegalArgumentException("Dados do perfil são obrigatórios");
        }

        try {
            Perfil perfilExistente = perfilRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Perfil não encontrado: " + id));

            // Verificar se o novo nome já existe em outro perfil
            String novoNome = perfilDTO.getNome();
            if (!perfilExistente.getNome().equals(novoNome) &&
                    perfilRepository.existsByNome(novoNome)) {
                throw new RuntimeException("Já existe um perfil com este nome: " + novoNome);
            }

            // Atualizar dados
            perfilExistente.setNome(perfilDTO.getNome());
            perfilExistente.setDescricao(perfilDTO.getDescricao());

            if (perfilDTO.getStatus() != null) {
                perfilExistente.setStatus(AtivoInativo.valueOf(perfilDTO.getStatus()));
            }

            Perfil perfilAtualizado = perfilRepository.save(perfilExistente);
            logger.info("Perfil atualizado com sucesso: {}", perfilAtualizado.getNome());

            return perfilMapper.toDTO(perfilAtualizado);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrado") ||
                    e.getMessage().contains("Já existe") ||
                    e.getMessage().contains("obrigatório")) {
                throw e;
            }
            logger.error("Erro ao atualizar perfil: {}", id, e);
            throw new RuntimeException("Erro ao atualizar perfil", e);
        }
    }

    @Override
    public void deletarPerfil(Long id) {
        logger.debug("Deletando perfil: {}", id);

        if (id == null) {
            throw new IllegalArgumentException("ID do perfil é obrigatório");
        }

        try {
            Perfil perfil = perfilRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Perfil não encontrado: " + id));

            // Soft delete - apenas alterar status
            perfil.setStatus(AtivoInativo.INATIVO);
            perfilRepository.save(perfil);

            logger.info("Perfil deletado com sucesso: {}", perfil.getNome());

        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrado")) {
                throw e;
            }
            logger.error("Erro ao deletar perfil: {}", id, e);
            throw new RuntimeException("Erro ao deletar perfil", e);
        }
    }
}