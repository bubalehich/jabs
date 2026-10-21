package io.github.bubalehich.events.domain.event.exception;

import io.github.bubalehich.events.domain.event.EventId;

public final class EventFullException extends EventRuleViolationException {

	public EventFullException(EventId id) {
		super("Event %s has no available seats".formatted(id));
	}

}
