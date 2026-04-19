package br.local.auth.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import br.local.auth.security.CustomAuthenticationProvider;
import br.local.auth.security.JwtTokenFilter;

/**
 * Configuração de Spring Security para autenticação baseada em JWT.
 *
 * Responsabilidades:
 * 1. Configurar autenticação com CustomAuthenticationProvider
 * 2. Configurar autorização com @PreAuthorize
 * 3. Configurar session como STATELESS (JWT)
 * 4. Configurar CORS apropriadamente
 * 5. Configurar PasswordEncoder (BCrypt)
 * 6. Adicionar JwtTokenFilter ao pipeline
 * 7. Desabilitar CSRF (stateless)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	@Autowired
	private ObjectProvider<CustomAuthenticationProvider> customAuthenticationProviderProvider;

	@Autowired
	private ObjectProvider<JwtTokenFilter> jwtTokenFilterProvider;

	/**
	 * PasswordEncoder usando BCrypt com strength 10
	 * Força 10 oferece bom balanço entre segurança e performance
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(10);
	}

	/**
	 * AuthenticationManager que gerencia o fluxo de autenticação
	 */
	@Bean
	public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
		AuthenticationManagerBuilder authenticationManagerBuilder =
			http.getSharedObject(AuthenticationManagerBuilder.class);

		authenticationManagerBuilder
			.authenticationProvider(customAuthenticationProviderProvider.getObject());

		return authenticationManagerBuilder.build();
	}

	/**
	 * Configuração de CORS permitindo requisições de qualquer origem
	 * Em produção, restrinja as origens permitidas
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowCredentials(true);
		configuration.addAllowedOriginPattern("*");
		configuration.addAllowedMethod("GET");
		configuration.addAllowedMethod("POST");
		configuration.addAllowedMethod("PUT");
		configuration.addAllowedMethod("DELETE");
		configuration.addAllowedMethod("PATCH");
		configuration.addAllowedMethod("OPTIONS");
		configuration.addAllowedHeader("*");
		configuration.addExposedHeader("Authorization");
		configuration.addExposedHeader("Content-Type");
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	/**
	 * Configuração principal de segurança HTTP
	 *
	 * Chain de filtros:
	 * 1. CORS
	 * 2. CSRF (desabilitado para stateless)
	 * 3. Session (STATELESS para JWT)
	 * 4. JwtTokenFilter (antes de UsernamePasswordAuthenticationFilter)
	 * 5. Authorization
	 */
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			// Habilitar CORS
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))

			// Desabilitar CSRF (para APIs stateless com JWT)
			.csrf(csrf -> csrf.disable())

			// Definir gerenciamento de sessão como STATELESS
			.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			)

			// Configurar autorização de endpoints
			.authorizeHttpRequests((authz) -> authz
				// Endpoints públicos (sem autenticação)
				.requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
				.requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll()
				.requestMatchers("/api/v1/auth/logout").permitAll()
				.requestMatchers("/actuator/health").permitAll()
				.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()

				// Endpoints de usuário (requerem autenticação)
				.requestMatchers(HttpMethod.GET, "/api/v1/usuarios/**").authenticated()
				.requestMatchers(HttpMethod.POST, "/api/v1/usuarios").authenticated()
				.requestMatchers(HttpMethod.PUT, "/api/v1/usuarios/**").authenticated()
				.requestMatchers(HttpMethod.DELETE, "/api/v1/usuarios/**").authenticated()

				// Qualquer outra requisição requer autenticação
				.anyRequest().authenticated()
			)

			// Adicionar JwtTokenFilter antes do UsernamePasswordAuthenticationFilter
			.addFilterBefore(jwtTokenFilterProvider.getObject(), UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
