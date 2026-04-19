package br.local.auth.dto;

import java.util.Date;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para retorno simples de Usuário
 * Sem dados sensíveis (senha)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UsuarioDTO {

	private Long id;
	private String username;
	private String cpf;
	private String nome;
	private String email;
	private String matricula;
	private String status;
	private Boolean alterar;
	private Date dataCadastro;
	private Date dataUltimoLogin;
	private Set<PerfilDTO> perfis;
	private Set<UnidadeDTO> unidades;
}
