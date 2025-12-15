package br.lar.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.lar.auth.model.Usuario;
import br.lar.auth.utils.AtivoInativo;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

	/**
	 * Buscar usuário por username
	 */
	Optional<Usuario> findByUsername(String username);

	/**
	 * Buscar usuário por CPF
	 */
	Optional<Usuario> findByCpf(String cpf);

	/**
	 * Buscar usuário por email
	 */
	Optional<Usuario> findByEmail(String email);

	/**
	 * Buscar usuários por status
	 */
	List<Usuario> findByStatus(AtivoInativo status);

	/**
	 * Listar usuários ativos
	 */
	List<Usuario> findByStatusOrderByNome(AtivoInativo status);

	/**
	 * Verificar se username existe
	 */
	boolean existsByUsername(String username);

	/**
	 * Verificar se CPF existe
	 */
	boolean existsByCpf(String cpf);

	/**
	 * Buscar usuários com paginação
	 */
	Page<Usuario> findAll(Pageable pageable);

	/**
	 * Buscar usuários por status com paginação
	 */
	Page<Usuario> findByStatus(AtivoInativo status, Pageable pageable);

	/**
	 * Busca por nome (contains)
	 */
	Page<Usuario> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

	/**
	 * Busca complexa: username ou cpf ou email
	 */
	@Query("SELECT u FROM Usuario u WHERE " +
		   "LOWER(u.username) LIKE LOWER(CONCAT('%', :busca, '%')) OR " +
		   "LOWER(u.nome) LIKE LOWER(CONCAT('%', :busca, '%')) OR " +
		   "u.cpf = :busca")
	Page<Usuario> buscarPorUsernameOuNomeOuCpf(@Param("busca") String busca, Pageable pageable);

	/**
	 * Busca por módulo (via USUARIO_PERFIL)
	 */
	@Query("SELECT DISTINCT u FROM Usuario u " +
		   "JOIN u.perfis p " +
		   "WHERE p.modulo.id = :moduloId AND u.status = :status")
	Page<Usuario> findByModuloAndStatus(@Param("moduloId") Long moduloId,
										@Param("status") AtivoInativo status,
										Pageable pageable);

	/**
	 * Busca por unidade (via USUARIO_UNIDADE)
	 */
	@Query("SELECT DISTINCT u FROM Usuario u " +
		   "JOIN u.unidades un " +
		   "WHERE un.id = :unidadeId")
	Page<Usuario> findByUnidade(@Param("unidadeId") Long unidadeId, Pageable pageable);

	/**
	 * Contar usuários ativos
	 */
	long countByStatus(AtivoInativo status);
}
