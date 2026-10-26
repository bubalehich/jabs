package io.github.bubalehich.events.domain.event;

import io.github.bubalehich.events.domain.common.DomainEvent;
import io.github.bubalehich.events.domain.event.exception.AlreadyRegisteredException;
import io.github.bubalehich.events.domain.event.exception.CapacityBelowRegistrationsException;
import io.github.bubalehich.events.domain.event.exception.EventAlreadyStartedException;
import io.github.bubalehich.events.domain.event.exception.EventFullException;
import io.github.bubalehich.events.domain.event.exception.InvalidEventDataException;
import io.github.bubalehich.events.domain.event.exception.RegistrationNotFoundException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Event {

	private final EventId id;
	private EventTitle title;
	private Instant startsAt;
	private Capacity capacity;
	private final Set<Email> participants;
	private final List<DomainEvent> domainEvents = new ArrayList<>();

	private Event(EventId id, EventTitle title, Instant startsAt, Capacity capacity, Collection<Email> participants) {
		this.id = Objects.requireNonNull(id);
		this.title = Objects.requireNonNull(title);
		this.startsAt = Objects.requireNonNull(startsAt);
		this.capacity = Objects.requireNonNull(capacity);
		this.participants = new LinkedHashSet<>(participants);
	}

	public static Event schedule(EventTitle title, Instant startsAt, Capacity capacity, Instant now) {
		requireStartInFuture(startsAt, now);
		Event event = new Event(EventId.newId(), title, startsAt, capacity, Set.of());
		event.domainEvents.add(new EventScheduled(event.id, now));
		return event;
	}

	public static Event restore(EventId id, EventTitle title, Instant startsAt, Capacity capacity,
			Collection<Email> participants) {
		return new Event(id, title, startsAt, capacity, participants);
	}

	public void update(EventTitle newTitle, Instant newStartsAt, Capacity newCapacity, Instant now) {
		requireNotStarted(now);
		requireStartInFuture(newStartsAt, now);
		if (newCapacity.value() < participants.size()) {
			throw new CapacityBelowRegistrationsException(id, newCapacity, participants.size());
		}
		this.title = newTitle;
		this.startsAt = newStartsAt;
		this.capacity = newCapacity;
	}

	public void register(Email participant, Instant now) {
		requireNotStarted(now);
		if (participants.contains(participant)) {
			throw new AlreadyRegisteredException(id, participant);
		}
		if (availableSeats() == 0) {
			throw new EventFullException(id);
		}
		participants.add(participant);
		domainEvents.add(new ParticipantRegistered(id, participant, now));
	}

	public void cancelRegistration(Email participant, Instant now) {
		requireNotStarted(now);
		if (!participants.remove(participant)) {
			throw new RegistrationNotFoundException(id, participant);
		}
		domainEvents.add(new RegistrationCancelled(id, participant, now));
	}

	public boolean hasStarted(Instant now) {
		return !now.isBefore(startsAt);
	}

	public int availableSeats() {
		return capacity.value() - participants.size();
	}

	public EventId id() {
		return id;
	}

	public EventTitle title() {
		return title;
	}

	public Instant startsAt() {
		return startsAt;
	}

	public Capacity capacity() {
		return capacity;
	}

	public Set<Email> participants() {
		return Collections.unmodifiableSet(participants);
	}

	public List<DomainEvent> pullDomainEvents() {
		List<DomainEvent> pulled = List.copyOf(domainEvents);
		domainEvents.clear();
		return pulled;
	}

	private void requireNotStarted(Instant now) {
		if (hasStarted(now)) {
			throw new EventAlreadyStartedException(id);
		}
	}

	private static void requireStartInFuture(Instant startsAt, Instant now) {
		if (!startsAt.isAfter(now)) {
			throw new InvalidEventDataException("Event start time must be in the future");
		}
	}

	@Override
	public boolean equals(Object other) {
		return this == other || other instanceof Event event && id.equals(event.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

}
