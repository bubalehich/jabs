package io.github.bubalehich.events.application.event;

import io.github.bubalehich.events.domain.common.PageQuery;
import io.github.bubalehich.events.domain.common.PageResult;
import io.github.bubalehich.events.domain.event.Capacity;
import io.github.bubalehich.events.domain.event.Email;
import io.github.bubalehich.events.domain.event.Event;
import io.github.bubalehich.events.domain.event.EventId;
import io.github.bubalehich.events.domain.event.EventRepository;
import io.github.bubalehich.events.domain.event.EventTitle;
import io.github.bubalehich.events.domain.event.exception.EventNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
@Transactional
public class EventService {

	private final EventRepository events;
	private final Clock clock;

	public EventService(EventRepository events, Clock clock) {
		this.events = events;
		this.clock = clock;
	}

	public Event create(EventDetailsCommand command) {
		Event event = Event.schedule(new EventTitle(command.title()), command.startsAt(),
				new Capacity(command.capacity()), clock.instant());
		events.save(event);
		return event;
	}

	@Transactional(readOnly = true)
	public Event get(UUID id) {
		return load(new EventId(id));
	}

	@Transactional(readOnly = true)
	public PageResult<Event> list(int page, int size) {
		return events.findAll(new PageQuery(page, size));
	}

	public Event update(UUID id, EventDetailsCommand command) {
		Event event = load(new EventId(id));
		event.update(new EventTitle(command.title()), command.startsAt(), new Capacity(command.capacity()),
				clock.instant());
		events.save(event);
		return event;
	}

	public void delete(UUID id) {
		EventId eventId = new EventId(id);
		load(eventId);
		events.delete(eventId);
	}

	public Email register(UUID id, String participantEmail) {
		Event event = load(new EventId(id));
		Email participant = new Email(participantEmail);
		event.register(participant, clock.instant());
		events.save(event);
		return participant;
	}

	public void cancelRegistration(UUID id, String participantEmail) {
		Event event = load(new EventId(id));
		event.cancelRegistration(new Email(participantEmail), clock.instant());
		events.save(event);
	}

	private Event load(EventId id) {
		return events.findById(id).orElseThrow(() -> new EventNotFoundException(id));
	}

}
