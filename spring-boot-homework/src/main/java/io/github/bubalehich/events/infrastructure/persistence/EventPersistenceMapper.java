package io.github.bubalehich.events.infrastructure.persistence;

import io.github.bubalehich.events.domain.event.Capacity;
import io.github.bubalehich.events.domain.event.Email;
import io.github.bubalehich.events.domain.event.Event;
import io.github.bubalehich.events.domain.event.EventId;
import io.github.bubalehich.events.domain.event.EventTitle;

final class EventPersistenceMapper {

	private EventPersistenceMapper() {
	}

	static Event toDomain(EventJpaEntity entity) {
		return Event.restore(
				new EventId(entity.getId()),
				new EventTitle(entity.getTitle()),
				entity.getStartsAt(),
				new Capacity(entity.getCapacity()),
				entity.getParticipants().stream().map(Email::new).toList());
	}

	static void copyToEntity(Event event, EventJpaEntity entity) {
		entity.setTitle(event.title().value());
		entity.setStartsAt(event.startsAt());
		entity.setCapacity(event.capacity().value());
		entity.getParticipants().retainAll(event.participants().stream().map(Email::value).toList());
		event.participants().forEach(participant -> entity.getParticipants().add(participant.value()));
	}

}
