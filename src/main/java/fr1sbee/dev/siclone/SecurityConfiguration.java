package fr1sbee.dev.siclone;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * API sin estado protegida con JWT emitidos por Keycloak. La documentación OpenAPI
 * y los endpoints de salud e información quedan abiertos.
 * <p>
 * Se moverá al módulo {@code seguridad} cuando exista.
 */
@Configuration(proxyBeanMethods = false)
class SecurityConfiguration {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
			.authorizeHttpRequests((auth) -> auth
				.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
				.requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
				.anyRequest().authenticated())
			.oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults()))
			.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.csrf(AbstractHttpConfigurer::disable)
			.build();
	}

}
