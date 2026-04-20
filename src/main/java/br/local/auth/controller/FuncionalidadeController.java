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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.local.auth.dto.FuncionalidadeDTO;
import br.local.auth.service.FuncionalidadeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Controller responsável por endpoints de funcionalidades.
 *
 * Endpoints:
 * - GET /api/v1/funcionalidades: Listar todas as funcionalidades ativas
 * - GET /api/v1/funcionalidades/{id}: Obter funcionalidade por ID
 * - POST /api/v1/funcionalidades: Criar nova funcionalidade
 * - PUT /api/v1/funcionalidades/{id}: Atualizar funcionalidade
 * - DELETE /api/v1/funcionalidades/{id}: Deletar funcionalidade
 */
@RestController
@RequestMapping("/api/v1/funcionalidades")
public class FuncionalidadeController {

    private static final Logger logger = LoggerFactory.getLogger(FuncionalidadeController.class);

    @Autowired
    private FuncionalidadeService funcionalidadeService;

    /**
     * Listar todas as funcionalidades ativas
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN_GERAL') or hasAuthority('GERENCIAR_PERFIS')")
    public ResponseEntity<List<FuncionalidadeDTO>> listarFuncionalidades() {
        logger.info("Listando todas as funcionalidades ativas");

        try {
            List<FuncionalidadeDTO> funcionalidades = funcionalidadeService.listarFuncionalidadesAtivas();
            return ResponseEntity.ok(funcionalidades);

        } catch (Exception e) {
            logger.error("Erro ao listar funcionalidades", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obter funcionalidade por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN_GERAL') or hasAuthority('GERENCIAR_PERFIS')")
    public ResponseEntity<FuncionalidadeDTO> obterFuncionalidade(@PathVariable Long id) {
        logger.info("Obtendo funcionalidade: {}", id);

        try {
            FuncionalidadeDTO funcionalidade = funcionalidadeService.obterFuncionalidade(id);
            return ResponseEntity.ok(funcionalidade);

        } catch (RuntimeException e) {
            logger.warn("Funcionalidade não encontrada: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            logger.error("Erro ao obter funcionalidade", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Criar nova funcionalidade
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN_GERAL')")
    public ResponseEntity<FuncionalidadeDTO> criarFuncionalidade(@Valid @RequestBody FuncionalidadeDTO funcionalidadeDTO) {
        logger.info("Criando nova funcionalidade: {}", funcionalidadeDTO.getNome());

        try {
            FuncionalidadeDTO funcionalidadeCriada = funcionalidadeService.criarFuncionalidade(funcionalidadeDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(funcionalidadeCriada);

        } catch (RuntimeException e) {
            logger.warn("Erro ao criar funcionalidade: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            logger.error("Erro ao criar funcionalidade", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Atualizar funcionalidade
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN_GERAL')")
    public ResponseEntity<FuncionalidadeDTO> atualizarFuncionalidade(
            @PathVariable Long id,
            @Valid @RequestBody FuncionalidadeDTO funcionalidadeDTO) {
        logger.info("Atualizando funcionalidade: {}", id);

        try {
            FuncionalidadeDTO funcionalidadeAtualizada = funcionalidadeService.atualizarFuncionalidade(id, funcionalidadeDTO);
            return ResponseEntity.ok(funcionalidadeAtualizada);

        } catch (RuntimeException e) {
            logger.warn("Erro ao atualizar funcionalidade: {}", e.getMessage());
            if (e.getMessage().contains("não encontrada")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            logger.error("Erro ao atualizar funcionalidade", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Deletar funcionalidade (soft delete)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN_GERAL')")
    public ResponseEntity<Void> deletarFuncionalidade(@PathVariable Long id) {
        logger.info("Deletando funcionalidade: {}", id);

        try {
            funcionalidadeService.deletarFuncionalidade(id);
            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {
            logger.warn("Funcionalidade não encontrada: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            logger.error("Erro ao deletar funcionalidade", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}