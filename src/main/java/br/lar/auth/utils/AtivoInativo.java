package br.lar.auth.utils;

/**
 * Enum que representa os possíveis estados de um Usuário
 */
public enum AtivoInativo {
	ATIVO("Ativo"),
	INATIVO("Inativo"),
	BLOQUEADO("Bloqueado");

	private String descricao;

	AtivoInativo(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}

	public static AtivoInativo fromString(String value) {
		for (AtivoInativo status : AtivoInativo.values()) {
			if (status.name().equalsIgnoreCase(value)) {
				return status;
			}
		}
		throw new IllegalArgumentException("Status inválido: " + value);
	}
}
