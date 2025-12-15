package br.lar.auth.model;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "modulo", schema = "auth")
public class Modulo implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "SEQ_MODULO", sequenceName = "SEQ_MODULO", allocationSize = 1, schema = "auth")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MODULO")
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "email_responsavel", length = 100)
	private String emailResponsavel;

	@Column(name = "descricao", nullable = false, length = 500)
	private String descricao;

	@Column(name = "status")
	private String status;

	@Column(name = "data_cadastro")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dataCadastro;
}
