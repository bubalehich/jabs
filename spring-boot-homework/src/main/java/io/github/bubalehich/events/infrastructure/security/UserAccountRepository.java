package io.github.bubalehich.events.infrastructure.security;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface UserAccountRepository extends JpaRepository<UserAccountJpaEntity, UUID> {

	Optional<UserAccountJpaEntity> findByEmail(String email);

}
