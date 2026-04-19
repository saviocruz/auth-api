package br.local.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.local.auth.model.Funcionalidade;

@Repository
public interface FuncionalidadeRepository extends JpaRepository<Funcionalidade, Long> {

	/**
	 * Buscar funcionalidade por nome
	 */
	Optional<Funcionalidade> findByNome(String nome);

	/**
	 * Buscar funcionalidades ativas
	 */
	List<Funcionalidade> findByStatus(String status);

	/**
	 * Verificar se funcionalidade existe
	 */
	boolean existsByNome(String nome);
}
