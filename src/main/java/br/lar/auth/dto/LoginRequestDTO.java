package br.lar.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de Login
 * Suporta 3 tipos de login:
 * 1. Apenas username e password
 * 2. Username, password e sistema
 * 3. Username, password, sistema e unidade
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginRequestDTO {

	@NotBlank(message = "Username é obrigatório")
	private String username;

	@NotBlank(message = "Password é obrigatório")
	private String password;

	/**
	 * Opcional: Tipo 2 e 3 de login
	 * Módulo/Sistema
	 */
	private String sistema;

	/**
	 * Opcional: Apenas Tipo 3 de login
	 * Unidade específica
	 */
	private String unidade;
}
