package io.github.bubalehich.events.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class AdminAccountsHealthIndicator extends AbstractHealthIndicator {

	private final UserAccountRepository userAccounts;

	@Override
	protected void doHealthCheck(Health.Builder builder) {
		long administrators = userAccounts.countByRole(Role.ADMIN);
		builder.withDetail("administrators", administrators);
		if (administrators == 0) {
			builder.down().withDetail("reason", "No administrator account");
			return;
		}
		builder.up();
	}

}
