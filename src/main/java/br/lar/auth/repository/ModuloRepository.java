package br.lar.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.lar.auth.model.Modulo;

@Repository
public interface ModuloRepository extends JpaRepository<Modulo, Long> {

	/**
	 * Buscar módulo por nome
	 */
	Optional<Modulo> findByDescricao(String descricao);

	/**
	 * Buscar módulos ativos
	 */
	List<Modulo> findByStatus(String status);

	/**
	 * Verificar se módulo existe
	 */
	boolean existsByDescricao(String descricao);

	/**
	 * Buscar módulo ativo por nome
	 */
	Optional<Modulo> findByDescricaoAndStatus(String descricao, String status);
}
