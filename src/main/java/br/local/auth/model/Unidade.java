package br.local.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "unidade", schema = "auth")
public class Unidade {

	public Unidade(Long id) {
		this.id = id;
	}

	@Id
	@Column(name = "id")
	private Long id;

	@JoinColumn(name = "unidade_superior_id")
	@ManyToOne(targetEntity = Unidade.class)
	private Unidade unidadeSuperior;

	@Column(name = "sigla")
	private String sigla;

	@Column(name = "descricao")
	private String descricao;

	@Column(name = "situacao")
	private String situacao;

	@Column(name = "numero")
	private Long numero;

	@Column(name = "email")
	private String email;

	@Column(name = "ordenacao")
	private Integer ordenacao;

	@Column(name = "telefone")
	private String telefone;

	@Column(name = "endereco")
	private String endereco;

	@Column(name = "cep")
	private String cep;

	@Column(name = "bairro")
	private String bairro;
}
