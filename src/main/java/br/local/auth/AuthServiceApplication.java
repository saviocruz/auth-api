package br.local.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Classe principal da aplicação Spring Boot para o serviço de autenticação.
 *
 * Funcionalidades:
 * - Sistema de autenticação com 3 tipos de login
 * - Autorização baseada em perfis e funcionalidades
 * - Gerenciamento de usuários com multi-tenancy
 * - Auditoria e compliance
 * - JWT para stateless authentication
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "br.lar.auth.repository")
public class AuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}
}
