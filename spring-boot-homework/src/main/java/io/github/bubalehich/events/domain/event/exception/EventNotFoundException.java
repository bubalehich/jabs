package io.github.bubalehich.events.domain.event.exception;

import io.github.bubalehich.events.domain.event.EventId;

public class EventNotFoundException extends RuntimeException {

	public EventNotFoundException(EventId id) {
		super("Event %s not found".formatted(id));
	}

}
