package br.local.auth.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

 

/**
 * The persistent class for the FUNCIONALIDADE database table.
 */
@Entity
@Table(name = "funcionalidade", schema = "auth")
@Data
@NoArgsConstructor
public class Funcionalidade implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "SEQ_FUNCIONALIDADE", sequenceName = "SEQ_FUNCIONALIDADE", allocationSize = 1, schema = "auth")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_FUNCIONALIDADE")
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "nome", nullable = false, length = 100)
	private String nome;

	@Column(name = "descricao", length = 500)
	private String descricao;

	@Column(name = "status", nullable = false, length = 100)
	private String status;

	public Funcionalidade(Long id, String nome) {
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
		Funcionalidade other = (Funcionalidade) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
