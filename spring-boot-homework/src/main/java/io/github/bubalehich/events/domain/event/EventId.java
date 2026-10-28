package io.github.bubalehich.events.domain.event;

import java.util.Objects;
import java.util.UUID;

public record EventId(UUID value) {

	public EventId {
		Objects.requireNonNull(value);
	}

	public static EventId newId() {
		return new EventId(UUID.randomUUID());
	}

	@Override
	public String toString() {
		return value.toString();
	}

}
