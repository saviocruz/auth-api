package br.local.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.local.auth.model.Perfil;
import br.local.auth.utils.AtivoInativo;

@Repository
public interface PerfilRepository extends JpaRepository<Perfil, Long> {

	/**
	 * Buscar perfil por nome
	 */
	Optional<Perfil> findByNome(String nome);

	/**
	 * Buscar perfis de um módulo
	 */
	List<Perfil> findByModuloId(Long moduloId);

	/**
	 * Buscar perfis de um módulo com paginação
	 */
	Page<Perfil> findByModuloId(Long moduloId, Pageable pageable);

	/**
	 * Buscar perfis ativos
	 */
	List<Perfil> findByStatus(AtivoInativo status);

	/**
	 * Buscar perfis ativos de um módulo
	 */
	List<Perfil> findByModuloIdAndStatus(Long moduloId, AtivoInativo status);

	/**
	 * Verificar se perfil existe
	 */
	boolean existsByNome(String nome);

	/**
	 * Contar perfis de um módulo
	 */
	long countByModuloId(Long moduloId);
}
