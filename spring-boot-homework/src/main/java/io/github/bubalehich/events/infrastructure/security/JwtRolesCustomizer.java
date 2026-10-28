package io.github.bubalehich.events.infrastructure.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class JwtRolesCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

	static final String ROLES_CLAIM = "roles";
	private static final String ROLE_PREFIX = "ROLE_";

	@Override
	public void customize(JwtEncodingContext context) {
		if (!OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
			return;
		}
		List<String> roles = context.getPrincipal().getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.filter(authority -> authority.startsWith(ROLE_PREFIX))
				.map(authority -> authority.substring(ROLE_PREFIX.length()))
				.toList();
		context.getClaims().claim(ROLES_CLAIM, roles);
	}

}
