package br.lar.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para Unidade
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UnidadeDTO {

	private Long id;
	private String sigla;
	private String descricao;
	private String situacao;
	private String numero;
	private String email;
	private String telefone;
	private String endereco;
	private String cep;
	private String bairro;
	private Integer ordenacao;
	private UnidadeDTO unidadeSuperior;	
}
