package br.lar.auth.dto;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import br.lar.auth.config.jackson.PerfilDTODeserializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para Perfil
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(using = PerfilDTODeserializer.class)
public class PerfilDTO {

	private Long id;
	private String nome;
	private String descricao;
	private String status;
	private ModuloDTO modulo;
	private Set<FuncionalidadeDTO> funcionalidades;
}
