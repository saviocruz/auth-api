package br.local.auth.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.local.auth.dto.FuncionalidadeDTO;
import br.local.auth.mapper.FuncionalidadeMapper;
import br.local.auth.model.Funcionalidade;
import br.local.auth.repository.FuncionalidadeRepository;
import br.local.auth.utils.AtivoInativo;

/**
 * Service implementation para operações de funcionalidades.
 */
@Service
@Transactional
public class FuncionalidadeServiceImpl implements FuncionalidadeService {

    private static final Logger logger = LoggerFactory.getLogger(FuncionalidadeServiceImpl.class);

    @Autowired
    private FuncionalidadeRepository funcionalidadeRepository;

    @Autowired
    private FuncionalidadeMapper funcionalidadeMapper;

    @Override
    @Transactional(readOnly = true)
    public List<FuncionalidadeDTO> listarFuncionalidadesAtivas() {
        logger.debug("Listando todas as funcionalidades ativas");

        try {
            List<Funcionalidade> funcionalidades = funcionalidadeRepository.findByStatus(AtivoInativo.ATIVO.name());
            logger.info("Encontradas {} funcionalidades ativas", funcionalidades.size());

            return funcionalidades.stream()
                    .map(funcionalidadeMapper::toDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Erro ao listar funcionalidades ativas", e);
            throw new RuntimeException("Erro ao listar funcionalidades ativas", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FuncionalidadeDTO obterFuncionalidade(Long id) {
        logger.debug("Obtendo funcionalidade: {}", id);

        if (id == null) {
            throw new IllegalArgumentException("ID da funcionalidade é obrigatório");
        }

        try {
            Funcionalidade funcionalidade = funcionalidadeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Funcionalidade não encontrada: " + id));

            logger.info("Funcionalidade encontrada: {}", funcionalidade.getNome());
            return funcionalidadeMapper.toDTO(funcionalidade);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrada")) {
                throw e;
            }
            logger.error("Erro ao obter funcionalidade: {}", id, e);
            throw new RuntimeException("Erro ao obter funcionalidade", e);
        }
    }

    @Override
    public FuncionalidadeDTO criarFuncionalidade(FuncionalidadeDTO funcionalidadeDTO) {
        logger.debug("Criando nova funcionalidade: {}", funcionalidadeDTO.getNome());

        if (funcionalidadeDTO == null) {
            throw new IllegalArgumentException("Dados da funcionalidade são obrigatórios");
        }

        if (funcionalidadeDTO.getNome() == null || funcionalidadeDTO.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da funcionalidade é obrigatório");
        }

        try {
            if (funcionalidadeRepository.existsByNome(funcionalidadeDTO.getNome())) {
                throw new RuntimeException("Já existe uma funcionalidade com este nome: " + funcionalidadeDTO.getNome());
            }

            Funcionalidade funcionalidade = funcionalidadeMapper.toEntity(funcionalidadeDTO);
            funcionalidade.setStatus(AtivoInativo.ATIVO.name());

            Funcionalidade funcionalidadeSalva = funcionalidadeRepository.save(funcionalidade);
            logger.info("Funcionalidade criada com sucesso: {}", funcionalidadeSalva.getNome());

            return funcionalidadeMapper.toDTO(funcionalidadeSalva);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("Já existe") || e.getMessage().contains("obrigatório")) {
                throw e;
            }
            logger.error("Erro ao criar funcionalidade", e);
            throw new RuntimeException("Erro ao criar funcionalidade", e);
        }
    }

    @Override
    public FuncionalidadeDTO atualizarFuncionalidade(Long id, FuncionalidadeDTO funcionalidadeDTO) {
        logger.debug("Atualizando funcionalidade: {}", id);

        if (id == null) {
            throw new IllegalArgumentException("ID da funcionalidade é obrigatório");
        }

        if (funcionalidadeDTO == null) {
            throw new IllegalArgumentException("Dados da funcionalidade são obrigatórios");
        }

        try {
            Funcionalidade funcionalidadeExistente = funcionalidadeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Funcionalidade não encontrada: " + id));

            String novoNome = funcionalidadeDTO.getNome();
            if (!funcionalidadeExistente.getNome().equals(novoNome) &&
                    funcionalidadeRepository.existsByNome(novoNome)) {
                throw new RuntimeException("Já existe uma funcionalidade com este nome: " + novoNome);
            }

            funcionalidadeExistente.setNome(funcionalidadeDTO.getNome());
            funcionalidadeExistente.setDescricao(funcionalidadeDTO.getDescricao());

            if (funcionalidadeDTO.getStatus() != null) {
                funcionalidadeExistente.setStatus(AtivoInativo.valueOf(funcionalidadeDTO.getStatus()).name());
            }

            Funcionalidade funcionalidadeAtualizada = funcionalidadeRepository.save(funcionalidadeExistente);
            logger.info("Funcionalidade atualizada com sucesso: {}", funcionalidadeAtualizada.getNome());

            return funcionalidadeMapper.toDTO(funcionalidadeAtualizada);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrada") ||
                    e.getMessage().contains("Já existe") ||
                    e.getMessage().contains("obrigatório")) {
                throw e;
            }
            logger.error("Erro ao atualizar funcionalidade: {}", id, e);
            throw new RuntimeException("Erro ao atualizar funcionalidade", e);
        }
    }

    @Override
    public void deletarFuncionalidade(Long id) {
        logger.debug("Deletando funcionalidade: {}", id);

        if (id == null) {
            throw new IllegalArgumentException("ID da funcionalidade é obrigatório");
        }

        try {
            Funcionalidade funcionalidade = funcionalidadeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Funcionalidade não encontrada: " + id));

            funcionalidade.setStatus(AtivoInativo.INATIVO.name());
            funcionalidadeRepository.save(funcionalidade);

            logger.info("Funcionalidade deletada com sucesso: {}", funcionalidade.getNome());

        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrada")) {
                throw e;
            }
            logger.error("Erro ao deletar funcionalidade: {}", id, e);
            throw new RuntimeException("Erro ao deletar funcionalidade", e);
        }
    }
}