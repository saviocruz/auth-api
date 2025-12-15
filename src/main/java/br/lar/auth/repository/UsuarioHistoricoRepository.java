package br.lar.auth.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.lar.auth.model.UsuarioHistorico;

@Repository
public interface UsuarioHistoricoRepository extends JpaRepository<UsuarioHistorico, Long> {

	/**
	 * Buscar histórico de um usuário com paginação
	 */
	Page<UsuarioHistorico> findByUsuarioId(Long usuarioId, Pageable pageable);

	/**
	 * Buscar histórico por tipo de evento
	 */
	List<UsuarioHistorico> findByTipoEvento(String tipoEvento);

	/**
	 * Buscar histórico por período
	 */
	Page<UsuarioHistorico> findByDataSistemaBetween(Date dataInicio, Date dataFim, Pageable pageable);

	/**
	 * Buscar histórico de um usuário por período
	 */
	Page<UsuarioHistorico> findByUsuarioIdAndDataSistemaBetween(
		Long usuarioId, Date dataInicio, Date dataFim, Pageable pageable);

	/**
	 * Buscar histórico por tipo de evento e período
	 */
	@Query("SELECT h FROM UsuarioHistorico h " +
		   "WHERE h.tipoEvento = :tipoEvento " +
		   "AND h.dataSistema BETWEEN :dataInicio AND :dataFim")
	Page<UsuarioHistorico> findByTipoEventoAndPeriodo(
		@Param("tipoEvento") String tipoEvento,
		@Param("dataInicio") Date dataInicio,
		@Param("dataFim") Date dataFim,
		Pageable pageable);

	/**
	 * Buscar histórico de logins de um usuário
	 */
	@Query("SELECT h FROM UsuarioHistorico h " +
		   "WHERE h.usuario.id = :usuarioId " +
		   "AND (h.tipoEvento = 'LOGIN_SUCESSO' OR h.tipoEvento = 'LOGIN_FALHA') " +
		   "ORDER BY h.dataSistema DESC")
	Page<UsuarioHistorico> findLoginsByUsuarioId(@Param("usuarioId") Long usuarioId, Pageable pageable);

	/**
	 * Contar eventos por tipo
	 */
	long countByTipoEvento(String tipoEvento);

	/**
	 * Contar eventos de um usuário
	 */
	long countByUsuarioId(Long usuarioId);
}
