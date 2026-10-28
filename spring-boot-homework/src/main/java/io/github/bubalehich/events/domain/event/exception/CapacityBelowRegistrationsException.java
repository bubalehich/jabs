package io.github.bubalehich.events.domain.event.exception;

import io.github.bubalehich.events.domain.event.Capacity;
import io.github.bubalehich.events.domain.event.EventId;

public final class CapacityBelowRegistrationsException extends EventRuleViolationException {

	public CapacityBelowRegistrationsException(EventId id, Capacity capacity, int registrations) {
		super("Capacity %d of event %s is below its %d registrations"
				.formatted(capacity.value(), id, registrations));
	}

}
