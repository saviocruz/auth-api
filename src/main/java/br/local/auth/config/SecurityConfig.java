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

import br.local.auth.security.CustomAuthenticationProvider;
import br.local.auth.security.JwtTokenFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	@Autowired
	private ObjectProvider<CustomAuthenticationProvider> customAuthenticationProviderProvider;

	@Autowired
	private ObjectProvider<JwtTokenFilter> jwtTokenFilterProvider;

	@Autowired
	private CorsConfig corsConfig;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(10);
	}

	@Bean
	public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
		AuthenticationManagerBuilder authenticationManagerBuilder =
			http.getSharedObject(AuthenticationManagerBuilder.class);

		authenticationManagerBuilder
			.authenticationProvider(customAuthenticationProviderProvider.getObject());

		return authenticationManagerBuilder.build();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
			.csrf(csrf -> csrf.disable())
			.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			)
			.authorizeHttpRequests((authz) -> authz
				.requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
				.requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll()
				.requestMatchers("/api/v1/auth/logout").permitAll()
				.requestMatchers("/actuator/health").permitAll()
				.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/v1/usuarios/**").permitAll()
				.requestMatchers(HttpMethod.POST, "/api/v1/usuarios").authenticated()
				.requestMatchers(HttpMethod.PUT, "/api/v1/usuarios/**").authenticated()
				.requestMatchers(HttpMethod.DELETE, "/api/v1/usuarios/**").authenticated()
				.anyRequest().authenticated()
			)
			.addFilterBefore(jwtTokenFilterProvider.getObject(), UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
