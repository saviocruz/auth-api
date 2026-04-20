package br.local.auth.service;

import java.util.List;

import br.local.auth.dto.FuncionalidadeDTO;

/**
 * Service interface para operações de funcionalidades.
 */
public interface FuncionalidadeService {

    /**
     * Listar todas as funcionalidades ativas
     */
    List<FuncionalidadeDTO> listarFuncionalidadesAtivas();

    /**
     * Obter funcionalidade por ID
     */
    FuncionalidadeDTO obterFuncionalidade(Long id);

    /**
     * Criar nova funcionalidade
     */
    FuncionalidadeDTO criarFuncionalidade(FuncionalidadeDTO funcionalidadeDTO);

    /**
     * Atualizar funcionalidade
     */
    FuncionalidadeDTO atualizarFuncionalidade(Long id, FuncionalidadeDTO funcionalidadeDTO);

    /**
     * Deletar funcionalidade (soft delete)
     */
    void deletarFuncionalidade(Long id);
}