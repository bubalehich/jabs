package io.github.bubalehich.events.domain.event;

import io.github.bubalehich.events.domain.event.exception.InvalidEventDataException;

public record EventTitle(String value) {

	public static final int MAX_LENGTH = 200;

	public EventTitle {
		if (value == null || value.isBlank()) {
			throw new InvalidEventDataException("Event title must not be blank");
		}
		value = value.strip();
		if (value.length() > MAX_LENGTH) {
			throw new InvalidEventDataException("Event title must not exceed " + MAX_LENGTH + " characters");
		}
	}

}
