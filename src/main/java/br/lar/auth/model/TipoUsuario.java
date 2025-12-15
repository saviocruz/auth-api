package br.lar.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "TIPO_USUARIO", schema = "auth")
@Data
public class TipoUsuario {

	@Id
	@Column(name = "ID")
	private Long id;

	@Column(name = "NOME")
	private String nome;
}
