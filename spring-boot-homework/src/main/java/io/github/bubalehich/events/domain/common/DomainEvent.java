package io.github.bubalehich.events.domain.common;

import java.time.Instant;

public interface DomainEvent {

	Instant occurredAt();

}
