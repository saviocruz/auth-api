package br.local.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.local.auth.model.Unidade;

@Repository
public interface UnidadeRepository extends JpaRepository<Unidade, Long> {

	/**
	 * Buscar unidade por sigla
	 */
	Optional<Unidade> findBySigla(String sigla);

	/**
	 * Buscar unidades ativas
	 */
	List<Unidade> findBySituacao(String situacao);

	/**
	 * Verificar se sigla existe
	 */
	boolean existsBySigla(String sigla);

	/**
	 * Buscar unidades ativas ordenadas
	 */
	List<Unidade> findBySituacaoOrderByOrdenacao(String situacao);

	/**
	 * Buscar unidades filhas de uma unidade superior
	 */
	List<Unidade> findByUnidadeSuperiorId(Long unidadeSuperiorId);

	/**
	 * Buscar hierarquia de unidades (árvore)
	 */
	@Query("SELECT u FROM Unidade u " +
		   "WHERE u.id = :idUnidade " +
		   "OR u.unidadeSuperior.id = :idUnidade " +
		   "ORDER BY u.ordenacao")
	List<Unidade> findHierarchy(@Param("idUnidade") Long idUnidade);
}
