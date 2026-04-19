package br.local.auth.model;

import java.io.Serializable;

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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The persistent class for the PERFIL database table.
 */
@Entity
@Table(name = "perfil", schema = "auth")
@Data
@NoArgsConstructor
public class Perfil implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "SEQ_PERFIL", sequenceName = "SEQ_PERFIL", allocationSize = 1, schema = "auth")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_PERFIL")
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "nome", nullable = false, length = 100)
	private String nome;

	@Column(name = "status", nullable = false, length = 10)
	@Enumerated(EnumType.STRING)
	private AtivoInativo status;

	@Column(name = "descricao", nullable = false, length = 500)
	private String descricao;

	@ManyToOne(fetch = FetchType.EAGER, optional = true)
	@JoinColumn(name = "id_modulo", nullable = false)
	private Modulo modulo;

	public Perfil(Long id, String nome) {
		super();
		this.id = id;
		this.nome = nome;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Perfil other = (Perfil) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
