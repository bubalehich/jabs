package io.github.bubalehich.events.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface SpringDataEventRepository extends JpaRepository<EventJpaEntity, UUID> {
}
