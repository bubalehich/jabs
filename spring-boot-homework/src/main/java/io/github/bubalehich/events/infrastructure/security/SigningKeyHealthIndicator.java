package io.github.bubalehich.events.infrastructure.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
class SigningKeyHealthIndicator extends AbstractHealthIndicator {

	private static final JWSAlgorithm ALGORITHM = JWSAlgorithm.RS256;
	private static final JWKSelector SIGNING_KEY = new JWKSelector(JWKMatcher.forJWSHeader(new JWSHeader(ALGORITHM)));

	private final JWKSource<SecurityContext> jwkSource;

	@Override
	protected void doHealthCheck(Health.Builder builder) throws Exception {
		List<JWK> keys = jwkSource.get(SIGNING_KEY, null);
		if (keys.isEmpty()) {
			builder.down().withDetail("reason", "No " + ALGORITHM + " signing key available");
			return;
		}
		builder.up()
				.withDetail("keyId", keys.getFirst().getKeyID())
				.withDetail("algorithm", ALGORITHM.getName());
	}

}
