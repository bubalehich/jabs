package io.github.bubalehich.events.interfaces.rest.dto;

import io.github.bubalehich.events.domain.event.Event;

import java.time.Instant;
import java.util.UUID;

public record EventResponse(
		UUID id,
		String title,
		Instant startsAt,
		int capacity,
		int registeredCount,
		int availableSeats) {

	public static EventResponse from(Event event) {
		return new EventResponse(
				event.id().value(),
				event.title().value(),
				event.startsAt(),
				event.capacity().value(),
				event.participants().size(),
				event.availableSeats());
	}

}
