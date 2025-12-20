package br.lar.auth.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

/**
 * Manipulador global de exceções para a aplicação.
 * Trata erros comuns como conversão de tipos inválidos em parâmetros de rota.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	/**
	 * Trata exceções de conversão de tipo em parâmetros de rota.
	 * Exemplo: quando um ID é passado como "undefined" em vez de um número.
	 */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<Map<String, Object>> handleMethodArgumentTypeMismatch(
			MethodArgumentTypeMismatchException ex) {
		
		logger.warn("Erro de conversão de tipo: parâmetro '{}' com valor '{}' não pode ser convertido para {}",
				ex.getName(), ex.getValue(), ex.getRequiredType().getSimpleName());

		Map<String, Object> response = new HashMap<>();
		response.put("erro", "Parâmetro inválido");
		response.put("detalhes", String.format(
				"O parâmetro '%s' com valor '%s' não é válido. Esperado: %s",
				ex.getName(),
				ex.getValue(),
				ex.getRequiredType().getSimpleName()
		));
		response.put("parametro", ex.getName());
		response.put("valorRecebido", ex.getValue());

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	/**
	 * Trata exceções genéricas não capturadas.
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
		logger.error("Erro não tratado", ex);

		Map<String, Object> response = new HashMap<>();
		response.put("erro", "Erro interno do servidor");
		response.put("mensagem", ex.getMessage());

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}
}
