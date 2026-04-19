package br.local.auth.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@Entity
@Table(name = "usuario_historico", schema = "auth")
@AllArgsConstructor
public class UsuarioHistorico {

	@Id
	@SequenceGenerator(name = "SEQ_USUARIO_HISTORICO", sequenceName = "SEQ_USUARIO_HISTORICO", allocationSize = 1, schema = "auth")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_USUARIO_HISTORICO")
	private Long id;

	@ManyToOne(targetEntity = Usuario.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario", nullable = true)
	private Usuario usuario;

	@Column(name = "tipo_evento", length = 100)
	private String tipoEvento;

	@Column(name = "descricao", length = 500)
	private String descricao;

	@Column(name = "data_sistema")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dataSistema;

	@Column(name = "modulo", length = 100)
	private String modulo;

	@Column(name = "unidade", length = 100)
	private String unidade;

	public UsuarioHistorico() {
	}

	public UsuarioHistorico(Usuario usuario, String tipoEvento, String descricao, String modulo, String unidade) {
		this.usuario = usuario;
		this.tipoEvento = tipoEvento;
		this.descricao = descricao;
		this.modulo = modulo;
		this.unidade = unidade;
		this.dataSistema = new Date();
	}

	public String getTipoEvento() {
		return tipoEvento;
	}

	public void setTipoEvento(String tipoEvento) {
		this.tipoEvento = tipoEvento;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public Date getDataSistema() {
		return dataSistema;
	}

	public void setDataSistema(Date dataSistema) {
		this.dataSistema = dataSistema;
	}

	public String getModulo() {
		return modulo;
	}

	public void setModulo(String modulo) {
		this.modulo = modulo;
	}

	public String getUnidade() {
		return unidade;
	}

	public void setUnidade(String unidade) {
		this.unidade = unidade;
	}
}
