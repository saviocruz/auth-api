package br.local.auth.model;

import java.util.Date;
import java.util.Set;

import br.local.auth.utils.AtivoInativo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
@Table(name = "usuario", schema = "auth")
public class Usuario {

	@Id
	@SequenceGenerator(name = "SEQ_USUARIO", sequenceName = "SEQ_USUARIO", allocationSize = 1, schema = "auth")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_USUARIO")
	private Long id;

	@Column(name = "cpf", nullable = false, length = 20)
	@NotNull
	private String cpf;

	@Column(name = "username", nullable = false, length = 100, unique = true)
	@NotNull
	private String username;

	@Column(name = "nome", nullable = false, length = 100)
	@NotNull
	private String nome;

	@Column(name = "email", nullable = false, length = 100)
	@NotNull
	private String email;

	@Column(name = "matricula", length = 50)
	private String matricula;

	@Column(name = "chave")
	private String chave;

	@Column(name = "status", nullable = false)
	@NotNull
	@Enumerated(EnumType.STRING)
	private AtivoInativo status;

	@Column(name = "alterar")
	@NotNull
	private Boolean alterar;

	@Column(name = "data_cadastro", nullable = false)
	@NotNull
	@Temporal(TemporalType.DATE)
	private Date dataCadastro;

	@Column(name = "data_ultimo_login")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dataUltimoLogin;

	@Column(name = "tentativas_falhas")
	private Integer tentativasFalhas = 0;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "usuario_perfil", schema = "auth", 
		joinColumns = @JoinColumn(name = "id_usuario"), 
		inverseJoinColumns = @JoinColumn(name = "id_perfil"))
	private Set<Perfil> perfis;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "usuario_unidade", schema = "auth",
		joinColumns = @JoinColumn(name = "id_usuario"),
		inverseJoinColumns = @JoinColumn(name = "id_unidade"))
	private Set<Unidade> unidades;

	public boolean podeLogin() {
		return status != null && status.equals(AtivoInativo.ATIVO);
	}

	public void incrementarTentativasFalhas() {
		if (tentativasFalhas == null) {
			tentativasFalhas = 0;
		}
		tentativasFalhas++;
		
		// Bloquear após 5 tentativas falhas
		if (tentativasFalhas >= 5) {
			this.status = AtivoInativo.BLOQUEADO;
		}
	}

	public void resetarTentativasFalhas() {
		tentativasFalhas = 0;
	}

	public void bloquear() {
		this.status = AtivoInativo.BLOQUEADO;
	}

	public void desbloquear() {
		this.status = AtivoInativo.ATIVO;
		this.tentativasFalhas = 0;
	}

	public boolean estaBloqueado() {
		return status != null && status.equals(AtivoInativo.BLOQUEADO);
	}

	public Set<Perfil> getPerfis() {
		return perfis;
	}

	public void setPerfis(Set<Perfil> perfis) {
		this.perfis = perfis;
	}

	public Set<Unidade> getUnidades() {
		return unidades;
	}

	public void setUnidades(Set<Unidade> unidades) {
		this.unidades = unidades;
	}
}
