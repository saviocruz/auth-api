package br.local.auth.security;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

/**
 * Implementação de TokenService para geração e validação de tokens JWT.
 *
 * Responsabilidades:
 * 1. Gerar tokens JWT assinados com HMAC256
 * 2. Validar integridade e validade dos tokens
 * 3. Extrair claims de tokens válidos
 * 4. Gerenciar tempos de expiração
 */
@Service
public class TokenServiceImpl implements TokenService {

	private static final Logger logger = LoggerFactory.getLogger(TokenServiceImpl.class);

	@Value("${jwt.secret:sua-chave-secreta-super-longa-com-pelo-menos-256-bits}")
	private String jwtSecret;

	@Value("${jwt.expiration:86400000}") // 24 horas em ms
	private long jwtExpiration;

	@Value("${jwt.refresh.expiration:604800000}") // 7 dias em ms
	private long jwtRefreshExpiration;

	/**
	 * Gera a chave secreta para assinar tokens
	 */
	private SecretKey gerarChave() {
		byte[] decodedKey = jwtSecret.getBytes(StandardCharsets.UTF_8);
		// Garantir que a chave tenha pelo menos 256 bits (32 bytes)
		if (decodedKey.length < 32) {
			logger.warn("Chave JWT tem menos de 256 bits. Considere usar uma chave mais forte.");
		}
		return Keys.hmacShaKeyFor(decodedKey);
	}

	@Override
	public String gerarToken(Long usuarioId, String username, String email, List<String> perfis) {
		logger.info("Gerando token para usuário: {} (Tipo 1)", username);

		Date agora = new Date();
		Date expiracaoEm = new Date(agora.getTime() + jwtExpiration);

		String token = Jwts.builder()
			.setSubject(username)
			.claim("usuarioId", usuarioId)
			.claim("email", email)
			.claim("perfis", perfis)
			.claim("tipo", "1")
			.setIssuedAt(agora)
			.setExpiration(expiracaoEm)
			.signWith(gerarChave(), SignatureAlgorithm.HS256)
			.compact();

		logger.info("Token gerado com sucesso para usuário: {} - Expira em: {}", username, expiracaoEm);
		return token;
	}

	@Override
	public String gerarTokenComSistema(Long usuarioId, String username, String email,
			String sistema, List<String> perfis) {
		logger.info("Gerando token para usuário: {} com sistema: {} (Tipo 2)", username, sistema);

		Date agora = new Date();
		Date expiracaoEm = new Date(agora.getTime() + jwtExpiration);

		String token = Jwts.builder()
			.setSubject(username)
			.claim("usuarioId", usuarioId)
			.claim("email", email)
			.claim("sistema", sistema)
			.claim("perfis", perfis)
			.claim("tipo", "2")
			.setIssuedAt(agora)
			.setExpiration(expiracaoEm)
			.signWith(gerarChave(), SignatureAlgorithm.HS256)
			.compact();

		logger.info("Token (Tipo 2) gerado com sucesso para usuário: {} - Expira em: {}", username, expiracaoEm);
		return token;
	}

	@Override
	public String gerarTokenComSistemaEUnidade(Long usuarioId, String username, String email,
			String sistema, String unidade, List<String> perfis) {
		logger.info("Gerando token para usuário: {} com sistema: {} e unidade: {} (Tipo 3)",
			username, sistema, unidade);

		Date agora = new Date();
		Date expiracaoEm = new Date(agora.getTime() + jwtExpiration);

		String token = Jwts.builder()
			.setSubject(username)
			.claim("usuarioId", usuarioId)
			.claim("email", email)
			.claim("sistema", sistema)
			.claim("unidade", unidade)
			.claim("perfis", perfis)
			.claim("tipo", "3")
			.setIssuedAt(agora)
			.setExpiration(expiracaoEm)
			.signWith(gerarChave(), SignatureAlgorithm.HS256)
			.compact();

		logger.info("Token (Tipo 3) gerado com sucesso para usuário: {} - Expira em: {}",
			username, expiracaoEm);
		return token;
	}

	@Override
	public String gerarRefreshToken(Long usuarioId, String username) {
		logger.info("Gerando refresh token para usuário: {}", username);

		Date agora = new Date();
		Date expiracaoEm = new Date(agora.getTime() + jwtRefreshExpiration);

		String token = Jwts.builder()
			.setSubject(username)
			.claim("usuarioId", usuarioId)
			.claim("tipo", "REFRESH")
			.setIssuedAt(agora)
			.setExpiration(expiracaoEm)
			.signWith(gerarChave(), SignatureAlgorithm.HS256)
			.compact();

		logger.info("Refresh token gerado com sucesso para usuário: {} - Expira em: {}", username, expiracaoEm);
		return token;
	}

	@Override
	public boolean validarToken(String token) {
		try {
			Jwts.parserBuilder()
				.setSigningKey(gerarChave())
				.build()
				.parseClaimsJws(token);

			logger.debug("Token válido");
			return true;

		} catch (SignatureException e) {
			logger.error("Assinatura JWT inválida: {}", e.getMessage());
		} catch (MalformedJwtException e) {
			logger.error("Token JWT mal formado: {}", e.getMessage());
		} catch (ExpiredJwtException e) {
			logger.error("Token JWT expirado: {}", e.getMessage());
		} catch (UnsupportedJwtException e) {
			logger.error("Token JWT não suportado: {}", e.getMessage());
		} catch (IllegalArgumentException e) {
			logger.error("Claims JWT vazias: {}", e.getMessage());
		}

		return false;
	}

	@Override
	public String extrairUsername(String token) {
		return extrairClaims(token).getSubject();
	}

	@Override
	public Long extrairUsuarioId(String token) {
		Object usuarioId = extrairClaims(token).get("usuarioId");
		if (usuarioId instanceof Number) {
			return ((Number) usuarioId).longValue();
		}
		logger.warn("usuarioId não encontrado ou inválido no token");
		return null;
	}

	@Override
	public String extrairEmail(String token) {
		Object email = extrairClaims(token).get("email");
		return email != null ? email.toString() : null;
	}

	@Override
	public String extrairSistema(String token) {
		Object sistema = extrairClaims(token).get("sistema");
		return sistema != null ? sistema.toString() : null;
	}

	@Override
	public String extrairUnidade(String token) {
		Object unidade = extrairClaims(token).get("unidade");
		return unidade != null ? unidade.toString() : null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<String> extrairPerfis(String token) {
		Object perfis = extrairClaims(token).get("perfis");
		if (perfis instanceof List) {
			return (List<String>) perfis;
		}
		logger.warn("Perfis não encontrados ou em formato inválido no token");
		return Arrays.asList();
	}

	@Override
	public Claims extrairClaims(String token) {
		try {
			return Jwts.parserBuilder()
				.setSigningKey(gerarChave())
				.build()
				.parseClaimsJws(token)
				.getBody();

		} catch (ExpiredJwtException e) {
			// Mesmo com token expirado, retornamos os claims para que possam ser processados
			logger.warn("Token expirado, retornando claims: {}", e.getClaims().getSubject());
			return e.getClaims();

		} catch (Exception e) {
			logger.error("Erro ao extrair claims do token: {}", e.getMessage());
			throw new RuntimeException("Token inválido", e);
		}
	}

	@Override
	public Date obterDataExpiracao(String token) {
		return extrairClaims(token).getExpiration();
	}

	@Override
	public boolean estaExpirado(String token) {
		try {
			return obterDataExpiracao(token).before(new Date());
		} catch (Exception e) {
			logger.error("Erro ao verificar expiração do token: {}", e.getMessage());
			return true;
		}
	}

	@Override
	public Long obterTempoAteExpiracao(String token) {
		try {
			Date expiracao = obterDataExpiracao(token);
			Date agora = new Date();

			if (expiracao.before(agora)) {
				return 0L;
			}

			return (expiracao.getTime() - agora.getTime()) / 1000; // Convertendo ms para segundos

		} catch (Exception e) {
			logger.error("Erro ao obter tempo até expiração: {}", e.getMessage());
			return 0L;
		}
	}
}
