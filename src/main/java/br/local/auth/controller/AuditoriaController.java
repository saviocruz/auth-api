package br.local.auth.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.local.auth.model.UsuarioHistorico;
import br.local.auth.repository.UsuarioHistoricoRepository;

/**
 * Controller para auditoria de eventos do sistema
 */
@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

	private static final Logger logger = LoggerFactory.getLogger(AuditoriaController.class);

	@Autowired
	private UsuarioHistoricoRepository usuarioHistoricoRepository;

	/**
	 * Listar eventos de auditoria com paginação e filtros
	 * 
	 * @param page Número da página (0-indexed)
	 * @param size Tamanho da página
	 * @param tipoEvento Filtro por tipo de evento (opcional)
	 * @param dataInicio Data início do período (formato: yyyy-MM-dd) (opcional)
	 * @param dataFim Data fim do período (formato: yyyy-MM-dd) (opcional)
	 * @param usuarioId ID do usuário (opcional)
	 */
	@GetMapping
	@PreAuthorize("hasAuthority('ADMIN_GERAL')")
	public ResponseEntity<?> listarEventos(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@RequestParam(required = false) String tipoEvento,
			@RequestParam(required = false) String dataInicio,
			@RequestParam(required = false) String dataFim,
			@RequestParam(required = false) Long usuarioId) {
		
		logger.info("Listando eventos de auditoria - page: {}, size: {}, tipo: {}, usuarioId: {}", 
				page, size, tipoEvento, usuarioId);

		try {
			Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dataSistema"));
			Page<UsuarioHistorico> historicos;

			// Aplicar filtros
			if (tipoEvento != null && dataInicio != null && dataFim != null) {
				Date inicio = parseDate(dataInicio);
				Date fim = parseDate(dataFim);
				historicos = usuarioHistoricoRepository.findByTipoEventoAndPeriodo(
						tipoEvento, inicio, fim, pageable);
			} else if (usuarioId != null && dataInicio != null && dataFim != null) {
				Date inicio = parseDate(dataInicio);
				Date fim = parseDate(dataFim);
				historicos = usuarioHistoricoRepository.findByUsuarioIdAndDataSistemaBetween(
						usuarioId, inicio, fim, pageable);
			} else if (dataInicio != null && dataFim != null) {
				Date inicio = parseDate(dataInicio);
				Date fim = parseDate(dataFim);
				historicos = usuarioHistoricoRepository.findByDataSistemaBetween(inicio, fim, pageable);
			} else if (usuarioId != null) {
				historicos = usuarioHistoricoRepository.findByUsuarioId(usuarioId, pageable);
			} else if (tipoEvento != null) {
				List<UsuarioHistorico> lista = usuarioHistoricoRepository.findByTipoEvento(tipoEvento);
				// Converter para page manualmente
				int start = page * size;
				int end = Math.min(start + size, lista.size());
				List<UsuarioHistorico> sublist = lista.subList(start, end);
				historicos = new org.springframework.data.domain.PageImpl<>(
						sublist, pageable, lista.size());
			} else {
				historicos = usuarioHistoricoRepository.findAll(pageable);
			}

			// Converter para DTO
			List<Map<String, Object>> eventos = historicos.getContent().stream()
				.map(this::converterParaDTO)
				.collect(Collectors.toList());

			Map<String, Object> response = new HashMap<>();
			response.put("eventos", eventos);
			response.put("totalElementos", historicos.getTotalElements());
			response.put("totalPaginas", historicos.getTotalPages());
			response.put("paginaAtual", historicos.getNumber());
			response.put("tamanho", historicos.getSize());

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			logger.error("Erro ao listar eventos de auditoria", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of("erro", "Erro ao listar eventos de auditoria"));
		}
	}

	/**
	 * Converter UsuarioHistorico para DTO
	 */
	private Map<String, Object> converterParaDTO(UsuarioHistorico historico) {
		Map<String, Object> dto = new HashMap<>();
		dto.put("id", historico.getId());
		dto.put("data", historico.getDataSistema());
		dto.put("usuario", historico.getUsuario() != null ? historico.getUsuario().getUsername() : "Sistema");
		dto.put("tipo", historico.getTipoEvento());
		dto.put("ip", "127.0.0.1"); // TODO: Adicionar campo IP na tabela se necessário
		dto.put("detalhes", historico.getDescricao());
		dto.put("modulo", historico.getModulo());
		dto.put("unidade", historico.getUnidade());
		return dto;
	}

	/**
	 * Parse de data no formato yyyy-MM-dd
	 */
	private Date parseDate(String dateString) throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		return format.parse(dateString);
	}
}
