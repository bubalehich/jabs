package io.github.bubalehich.events.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.UUID;

interface SpringDataEventRepository extends JpaRepository<EventJpaEntity, UUID> {

	long countByStartsAtAfter(Instant moment);

}
