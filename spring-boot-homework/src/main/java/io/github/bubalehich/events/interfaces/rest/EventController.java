package io.github.bubalehich.events.interfaces.rest;

import io.github.bubalehich.events.application.event.EventService;
import io.github.bubalehich.events.domain.common.PageQuery;
import io.github.bubalehich.events.domain.event.Email;
import io.github.bubalehich.events.domain.event.Event;
import io.github.bubalehich.events.interfaces.rest.dto.EventRequest;
import io.github.bubalehich.events.interfaces.rest.dto.EventResponse;
import io.github.bubalehich.events.interfaces.rest.dto.PageResponse;
import io.github.bubalehich.events.interfaces.rest.dto.RegistrationRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
public class EventController {

	private final EventService eventService;

	public EventController(EventService eventService) {
		this.eventService = eventService;
	}

	@PostMapping
	public ResponseEntity<EventResponse> create(@Valid @RequestBody EventRequest request) {
		Event event = eventService.create(request.toCommand());
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(event.id().value())
				.toUri();
		return ResponseEntity.created(location).body(EventResponse.from(event));
	}

	@GetMapping
	public PageResponse<EventResponse> list(
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(PageQuery.MAX_SIZE) int size) {
		return PageResponse.from(eventService.list(page, size), EventResponse::from);
	}

	@GetMapping("/{id}")
	public EventResponse get(@PathVariable UUID id) {
		return EventResponse.from(eventService.get(id));
	}

	@PutMapping("/{id}")
	public EventResponse update(@PathVariable UUID id, @Valid @RequestBody EventRequest request) {
		return EventResponse.from(eventService.update(id, request.toCommand()));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable UUID id) {
		eventService.delete(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/registrations")
	public ResponseEntity<Void> register(@PathVariable UUID id, @Valid @RequestBody RegistrationRequest request) {
		Email participant = eventService.register(id, request.email());
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{email}")
				.buildAndExpand(participant.value())
				.toUri();
		return ResponseEntity.created(location).build();
	}

	@DeleteMapping("/{id}/registrations/{email}")
	public ResponseEntity<Void> cancelRegistration(@PathVariable UUID id, @PathVariable String email) {
		eventService.cancelRegistration(id, email);
		return ResponseEntity.noContent().build();
	}

}
