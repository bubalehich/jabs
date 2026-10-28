package io.github.bubalehich.events.interfaces.rest.dto;

import io.github.bubalehich.events.application.event.EventDetailsCommand;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record EventRequest(
		@NotBlank @Size(max = 200) String title,
		@NotNull @Future Instant startsAt,
		@NotNull @Positive Integer capacity) {

	public EventDetailsCommand toCommand() {
		return new EventDetailsCommand(title, startsAt, capacity);
	}

}
