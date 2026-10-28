package io.github.bubalehich.events.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
class DatabaseUserDetailsService implements UserDetailsService {

	private final UserAccountRepository userAccounts;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) {
		return userAccounts.findByEmail(username.strip().toLowerCase(Locale.ROOT))
				.map(account -> User.withUsername(account.getEmail())
						.password(account.getPasswordHash())
						.roles(account.getRole().name())
						.build())
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
	}

}
