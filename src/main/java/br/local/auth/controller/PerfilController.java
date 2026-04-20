package br.local.auth.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.local.auth.dto.PerfilDTO;
import br.local.auth.service.PerfilService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Controller responsável por endpoints de perfis.
 *
 * Endpoints:
 * - GET /api/v1/perfis: Listar todos os perfis ativos
 * - GET /api/v1/perfis/modulo/{moduloId}: Listar perfis por módulo
 * - GET /api/v1/perfis/search: Buscar perfis por nome
 */
@RestController
@RequestMapping("/api/v1/perfis")
public class PerfilController {

    private static final Logger logger = LoggerFactory.getLogger(PerfilController.class);

    @Autowired
    private PerfilService perfilService;

    /**
     * Listar todos os perfis ativos
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ADMINISTRADOR') or hasAuthority('GESTOR')")
    public ResponseEntity<List<PerfilDTO>> listarPerfis() {
        logger.info("Listando todos os perfis ativos");

        try {
            List<PerfilDTO> perfis = perfilService.listarPerfisAtivos();
            return ResponseEntity.ok(perfis);

        } catch (Exception e) {
            logger.error("Erro ao listar perfis", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Listar perfis por módulo
     */
    @GetMapping("/modulo/{moduloId}")
    @PreAuthorize("hasAuthority('ADMINISTRADOR') or hasAuthority('GESTOR_IMPLANTACAO')")
    public ResponseEntity<List<PerfilDTO>> listarPerfisPorModulo(@NotNull @PathVariable Long moduloId) {
        logger.info("Listando perfis do módulo: {}", moduloId);

        try {
            List<PerfilDTO> perfis = perfilService.listarPerfisPorModulo(moduloId);
            return ResponseEntity.ok(perfis);

        } catch (Exception e) {
            logger.error("Erro ao listar perfis por módulo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Buscar perfis por nome
     */
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('ADMINISTRADOR') or hasAuthority('GESTOR_IMPLANTACAO')")
    public ResponseEntity<List<PerfilDTO>> buscarPerfis(@RequestParam(required = false) String nome) {
        logger.info("Buscando perfis com nome: {}", nome);

        try {
            List<PerfilDTO> perfis;
            if (nome != null && !nome.trim().isEmpty()) {
                perfis = perfilService.buscarPerfisPorNome(nome);
            } else {
                perfis = perfilService.listarPerfisAtivos();
            }
            return ResponseEntity.ok(perfis);

        } catch (Exception e) {
            logger.error("Erro ao buscar perfis", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obter perfil por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMINISTRADOR') or hasAuthority('GESTOR_IMPLANTACAO')")
    public ResponseEntity<PerfilDTO> obterPerfil(@PathVariable Long id) {
        logger.info("Obtendo perfil: {}", id);

        try {
            PerfilDTO perfil = perfilService.obterPerfil(id);
            return ResponseEntity.ok(perfil);

        } catch (RuntimeException e) {
            logger.warn("Perfil não encontrado: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            logger.error("Erro ao obter perfil", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Criar novo perfil
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    public ResponseEntity<PerfilDTO> criarPerfil(@Valid @RequestBody PerfilDTO perfilDTO) {
        logger.info("Criando novo perfil: {}", perfilDTO.getNome());

        try {
            PerfilDTO perfilCriado = perfilService.criarPerfil(perfilDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(perfilCriado);

        } catch (RuntimeException e) {
            logger.warn("Erro ao criar perfil: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            logger.error("Erro ao criar perfil", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Atualizar perfil
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    public ResponseEntity<PerfilDTO> atualizarPerfil(
            @PathVariable Long id,
            @Valid @RequestBody PerfilDTO perfilDTO) {
        logger.info("Atualizando perfil: {}", id);

        try {
            PerfilDTO perfilAtualizado = perfilService.atualizarPerfil(id, perfilDTO);
            return ResponseEntity.ok(perfilAtualizado);

        } catch (RuntimeException e) {
            logger.warn("Erro ao atualizar perfil: {}", e.getMessage());
            if (e.getMessage().contains("não encontrado")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            logger.error("Erro ao atualizar perfil", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Deletar perfil (soft delete)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    public ResponseEntity<Void> deletarPerfil(@PathVariable Long id) {
        logger.info("Deletando perfil: {}", id);

        try {
            perfilService.deletarPerfil(id);
            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {
            logger.warn("Perfil não encontrado: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            logger.error("Erro ao deletar perfil", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}