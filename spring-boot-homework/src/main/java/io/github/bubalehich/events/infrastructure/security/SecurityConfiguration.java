package io.github.bubalehich.events.infrastructure.security;

import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

@Configuration
public class SecurityConfiguration {

	@Bean
	@Order(1)
	public SecurityFilterChain authorizationServerFilterChain(HttpSecurity http) {
		http
				.oauth2AuthorizationServer(server -> http.securityMatcher(server.getEndpointsMatcher()))
				.authorizeHttpRequests(requests -> requests.anyRequest().authenticated())
				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")));
		return http.build();
	}

	@Bean
	@Order(2)
	public SecurityFilterChain apiFilterChain(HttpSecurity http) {
		http
				.securityMatcher("/api/**")
				.authorizeHttpRequests(requests -> requests
						.requestMatchers(HttpMethod.GET, "/api/events/**").hasAnyRole("USER", "ADMIN")
						.requestMatchers("/api/events/*/registrations/**").hasAnyRole("USER", "ADMIN")
						.requestMatchers("/api/events/**").hasRole("ADMIN")
						.anyRequest().denyAll());
		acceptBearerTokens(http);
		return http.build();
	}

	@Bean
	@Order(3)
	public SecurityFilterChain actuatorFilterChain(HttpSecurity http) {
		http
				.securityMatcher(EndpointRequest.toAnyEndpoint())
				.authorizeHttpRequests(requests -> requests
						.requestMatchers(EndpointRequest.to("health", "prometheus")).permitAll()
						.anyRequest().hasRole("ADMIN"));
		acceptBearerTokens(http);
		return http.build();
	}

	@Bean
	@Order(4)
	public SecurityFilterChain loginFilterChain(HttpSecurity http) {
		http
				.authorizeHttpRequests(requests -> requests.anyRequest().authenticated())
				.formLogin(Customizer.withDefaults());
		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	private static void acceptBearerTokens(HttpSecurity http) {
		http
				.oauth2ResourceServer(resourceServer -> resourceServer
						.jwt(jwt -> jwt.jwtAuthenticationConverter(rolesAuthenticationConverter())))
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.csrf(csrf -> csrf.disable());
	}

	private static JwtAuthenticationConverter rolesAuthenticationConverter() {
		JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
		authoritiesConverter.setAuthoritiesClaimName(JwtRolesCustomizer.ROLES_CLAIM);
		authoritiesConverter.setAuthorityPrefix("ROLE_");
		JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
		authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
		return authenticationConverter;
	}

}
