package io.github.bubalehich.events.domain.event.exception;

import io.github.bubalehich.events.domain.event.Email;
import io.github.bubalehich.events.domain.event.EventId;

public class RegistrationNotFoundException extends RuntimeException {

	public RegistrationNotFoundException(EventId id, Email participant) {
		super("%s is not registered for event %s".formatted(participant, id));
	}

}
