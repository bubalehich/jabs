package io.github.bubalehich.events.domain.event;

import io.github.bubalehich.events.domain.event.exception.InvalidEventDataException;

public record Capacity(int value) {

	public Capacity {
		if (value <= 0) {
			throw new InvalidEventDataException("Event capacity must be positive");
		}
	}

}
