package io.github.bubalehich.events.domain.event.exception;

import io.github.bubalehich.events.domain.event.EventId;

public final class EventAlreadyStartedException extends EventRuleViolationException {

	public EventAlreadyStartedException(EventId id) {
		super("Event %s has already started".formatted(id));
	}

}
