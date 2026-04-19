package br.local.auth.controller;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.local.auth.model.UsuarioHistorico;
import br.local.auth.repository.PerfilRepository;
import br.local.auth.repository.UsuarioHistoricoRepository;
import br.local.auth.repository.UsuarioRepository;
import br.local.auth.utils.AtivoInativo;

/**
 * Controller para estatísticas e dados do dashboard
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

	private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private PerfilRepository perfilRepository;

	@Autowired
	private UsuarioHistoricoRepository usuarioHistoricoRepository;

	/**
	 * Obter KPIs (indicadores-chave) do dashboard
	 */
	@GetMapping("/kpis")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> obterKpis() {
		logger.info("Obtendo KPIs do dashboard");

		try {
			Map<String, Object> kpis = new HashMap<>();

			// Contar usuários ativos
			long usuariosAtivos = usuarioRepository.countByStatus(AtivoInativo.ATIVO);
			kpis.put("usuariosAtivos", usuariosAtivos);

			// Contar perfis ativos
			long perfisAtivos = perfilRepository.count();
			kpis.put("perfisAtivos", perfisAtivos);

			// Contar acessos de hoje (LOGIN_SUCESSO)
			Date hoje = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
			Date amanha = Date.from(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
			
			Page<UsuarioHistorico> loginsHoje = usuarioHistoricoRepository.findByTipoEventoAndPeriodo(
				"LOGIN_SUCESSO", hoje, amanha, PageRequest.of(0, 1));
			kpis.put("acessosHoje", loginsHoje.getTotalElements());

			// Contar tentativas falhas nas últimas 24h
			Page<UsuarioHistorico> falhas24h = usuarioHistoricoRepository.findByTipoEventoAndPeriodo(
				"LOGIN_FALHA", hoje, amanha, PageRequest.of(0, 1));
			kpis.put("tentativasFalhas24h", falhas24h.getTotalElements());

			return ResponseEntity.ok(kpis);

		} catch (Exception e) {
			logger.error("Erro ao obter KPIs", e);
			return ResponseEntity.internalServerError()
				.body(Map.of("erro", "Erro ao obter KPIs"));
		}
	}

	/**
	 * Obter dados de logins por dia (últimos 7 dias)
	 */
	@GetMapping("/logins-diarios")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> obterLoginsDiarios() {
		logger.info("Obtendo logins diários");

		try {
			List<Map<String, Object>> loginsPorDia = new ArrayList<>();
			SimpleDateFormat sdf = new SimpleDateFormat("EEE");

			// Iterar pelos últimos 7 dias
			for (int i = 6; i >= 0; i--) {
				LocalDate data = LocalDate.now().minusDays(i);
				Date inicio = Date.from(data.atStartOfDay(ZoneId.systemDefault()).toInstant());
				Date fim = Date.from(data.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

				// Contar logins bem-sucedidos do dia
				Page<UsuarioHistorico> logins = usuarioHistoricoRepository.findByTipoEventoAndPeriodo(
					"LOGIN_SUCESSO", inicio, fim, PageRequest.of(0, 1));

				Map<String, Object> dadosDia = new HashMap<>();
				dadosDia.put("name", getDiaSemana(i));
				dadosDia.put("total", logins.getTotalElements());
				dadosDia.put("data", inicio);

				loginsPorDia.add(dadosDia);
			}

			return ResponseEntity.ok(loginsPorDia);

		} catch (Exception e) {
			logger.error("Erro ao obter logins diários", e);
			return ResponseEntity.internalServerError()
				.body(Map.of("erro", "Erro ao obter logins diários"));
		}
	}

	/**
	 * Obter atividades recentes (últimos eventos de auditoria)
	 */
	@GetMapping("/atividades-recentes")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> obterAtividadesRecentes() {
		logger.info("Obtendo atividades recentes");

		try {
			Pageable pageable = PageRequest.of(0, 5);
			Page<UsuarioHistorico> historicos = usuarioHistoricoRepository.findAll(
				PageRequest.of(0, 5, org.springframework.data.domain.Sort.by(
					org.springframework.data.domain.Sort.Direction.DESC, "dataSistema")));

			List<Map<String, Object>> atividades = historicos.getContent().stream()
				.map(this::converterAtividadeParaDTO)
				.collect(Collectors.toList());

			return ResponseEntity.ok(atividades);

		} catch (Exception e) {
			logger.error("Erro ao obter atividades recentes", e);
			return ResponseEntity.internalServerError()
				.body(Map.of("erro", "Erro ao obter atividades recentes"));
		}
	}

	/**
	 * Converter UsuarioHistorico para DTO de atividade
	 */
	private Map<String, Object> converterAtividadeParaDTO(UsuarioHistorico historico) {
		Map<String, Object> dto = new HashMap<>();
		dto.put("id", historico.getId());
		dto.put("usuario", historico.getUsuario() != null ? historico.getUsuario().getUsername() : "Sistema");
		dto.put("nome", historico.getUsuario() != null ? historico.getUsuario().getNome() : "Sistema");
		dto.put("email", historico.getUsuario() != null ? historico.getUsuario().getEmail() : "sistema@local");
		dto.put("tipo", historico.getTipoEvento());
		dto.put("descricao", historico.getDescricao());
		dto.put("data", historico.getDataSistema());
		
		// Determinar cor baseada no tipo de evento
		String cor = "text-muted-foreground";
		if ("LOGIN_SUCESSO".equals(historico.getTipoEvento())) {
			cor = "text-green-500";
		} else if ("LOGIN_FALHA".equals(historico.getTipoEvento())) {
			cor = "text-red-500";
		} else if ("ALTERAR_SENHA".equals(historico.getTipoEvento())) {
			cor = "text-blue-500";
		} else if ("CRIAR_USUARIO".equals(historico.getTipoEvento())) {
			cor = "text-purple-500";
		}
		dto.put("cor", cor);
		
		return dto;
	}

	/**
	 * Obter nome do dia da semana
	 */
	private String getDiaSemana(int diasAtras) {
		LocalDate data = LocalDate.now().minusDays(diasAtras);
		int diaSemana = data.getDayOfWeek().getValue();
		
		switch (diaSemana) {
			case 1: return "Seg";
			case 2: return "Ter";
			case 3: return "Qua";
			case 4: return "Qui";
			case 5: return "Sex";
			case 6: return "Sáb";
			case 7: return "Dom";
			default: return "";
		}
	}
}
