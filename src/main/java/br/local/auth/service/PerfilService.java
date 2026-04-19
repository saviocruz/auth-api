package br.local.auth.service;

import java.util.List;

import br.local.auth.dto.PerfilDTO;

/**
 * Service interface para operações de perfis.
 */
public interface PerfilService {

    /**
     * Listar todos os perfis ativos
     */
    List<PerfilDTO> listarPerfisAtivos();

    /**
     * Listar perfis por módulo
     */
    List<PerfilDTO> listarPerfisPorModulo(Long moduloId);

    /**
     * Buscar perfis por nome
     */
    List<PerfilDTO> buscarPerfisPorNome(String nome);

    /**
     * Obter perfil por ID
     */
    PerfilDTO obterPerfil(Long id);

    /**
     * Criar novo perfil
     */
    PerfilDTO criarPerfil(PerfilDTO perfilDTO);

    /**
     * Atualizar perfil
     */
    PerfilDTO atualizarPerfil(Long id, PerfilDTO perfilDTO);

    /**
     * Deletar perfil (soft delete)
     */
    void deletarPerfil(Long id);
}