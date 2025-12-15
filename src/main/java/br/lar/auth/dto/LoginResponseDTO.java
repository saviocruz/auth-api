package br.lar.auth.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para resposta de Login
 * Contém token JWT e informações do usuário
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponseDTO {

	private boolean sucesso;
	private String mensagem;
	private String token;
	private String refreshToken;
	private UsuarioDTO usuario;
	private Long expiresIn; // Segundos até expiração
	private Date expiresAt; // Data de expiração
	private String erro;
}
