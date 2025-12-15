package br.lar.auth.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para Módulo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModuloDTO {

	private Long id;
	private String nome;
	private String descricao;
	private String emailResponsavel;
	private String status;
	private Date dataCadastro;
}
