package io.github.bubalehich.events.domain.event.exception;

import io.github.bubalehich.events.domain.event.Email;
import io.github.bubalehich.events.domain.event.EventId;

public final class AlreadyRegisteredException extends EventRuleViolationException {

	public AlreadyRegisteredException(EventId id, Email participant) {
		super("%s is already registered for event %s".formatted(participant, id));
	}

}
