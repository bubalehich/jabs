package io.github.bubalehich.events.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Sql(statements = {
		"INSERT INTO user_accounts (id, email, password_hash, role) "
				+ "VALUES (RANDOM_UUID(), 'admin@example.com', '{noop}secret', 'ADMIN')",
		"INSERT INTO user_accounts (id, email, password_hash, role) "
				+ "VALUES (RANDOM_UUID(), 'alice@example.com', '{noop}secret', 'USER')",
		"INSERT INTO user_accounts (id, email, password_hash, role) "
				+ "VALUES (RANDOM_UUID(), 'bob@example.com', '{noop}secret', 'USER')" })
class UserAccountRepositoryTest {

	@Autowired
	private UserAccountRepository userAccounts;

	@Test
	void findsAccountByEmail() {
		assertThat(userAccounts.findByEmail("alice@example.com"))
				.hasValueSatisfying(account -> assertThat(account.getRole()).isEqualTo(Role.USER));
	}

	@Test
	void findByEmailReturnsEmptyForUnknownEmail() {
		assertThat(userAccounts.findByEmail("carol@example.com")).isEmpty();
	}

	@Test
	void countsAccountsByRole() {
		assertThat(userAccounts.countByRole(Role.ADMIN)).isEqualTo(1);
		assertThat(userAccounts.countByRole(Role.USER)).isEqualTo(2);
	}

}
