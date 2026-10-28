package io.github.bubalehich.events.domain.event;

import io.github.bubalehich.events.domain.event.exception.AlreadyRegisteredException;
import io.github.bubalehich.events.domain.event.exception.CapacityBelowRegistrationsException;
import io.github.bubalehich.events.domain.event.exception.EventAlreadyStartedException;
import io.github.bubalehich.events.domain.event.exception.EventFullException;
import io.github.bubalehich.events.domain.event.exception.InvalidEventDataException;
import io.github.bubalehich.events.domain.event.exception.RegistrationNotFoundException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventTest {

	private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");
	private static final Instant STARTS_AT = NOW.plus(Duration.ofDays(7));
	private static final Email ALICE = new Email("alice@example.com");
	private static final Email BOB = new Email("bob@example.com");

	@Test
	void scheduleRaisesEventScheduled() {
		Event event = scheduleWithCapacity(10);

		assertThat(event.pullDomainEvents()).containsExactly(new EventScheduled(event.id(), NOW));
		assertThat(event.availableSeats()).isEqualTo(10);
	}

	@Test
	void scheduleRejectsStartInThePast() {
		assertThatThrownBy(() -> Event.schedule(new EventTitle("Meetup"), NOW, new Capacity(10), NOW))
				.isInstanceOf(InvalidEventDataException.class);
	}

	@Test
	void registerTakesSeatAndRaisesParticipantRegistered() {
		Event event = scheduleWithCapacity(10);
		event.pullDomainEvents();

		event.register(ALICE, NOW);

		assertThat(event.participants()).containsExactly(ALICE);
		assertThat(event.availableSeats()).isEqualTo(9);
		assertThat(event.pullDomainEvents()).containsExactly(new ParticipantRegistered(event.id(), ALICE, NOW));
	}

	@Test
	void registerRejectsSameParticipantTwice() {
		Event event = scheduleWithCapacity(10);
		event.register(ALICE, NOW);

		assertThatThrownBy(() -> event.register(ALICE, NOW)).isInstanceOf(AlreadyRegisteredException.class);
	}

	@Test
	void registerRejectsParticipantWhenNoSeatsLeft() {
		Event event = scheduleWithCapacity(1);
		event.register(ALICE, NOW);

		assertThatThrownBy(() -> event.register(BOB, NOW)).isInstanceOf(EventFullException.class);
	}

	@Test
	void registerRejectsParticipantOnceEventHasStarted() {
		Event event = scheduleWithCapacity(10);

		assertThatThrownBy(() -> event.register(ALICE, STARTS_AT)).isInstanceOf(EventAlreadyStartedException.class);
	}

	@Test
	void cancelRegistrationFreesSeatAndRaisesRegistrationCancelled() {
		Event event = scheduleWithCapacity(10);
		event.register(ALICE, NOW);
		event.pullDomainEvents();

		event.cancelRegistration(ALICE, NOW);

		assertThat(event.participants()).isEmpty();
		assertThat(event.pullDomainEvents()).containsExactly(new RegistrationCancelled(event.id(), ALICE, NOW));
	}

	@Test
	void cancelRegistrationRejectsUnknownParticipant() {
		Event event = scheduleWithCapacity(10);

		assertThatThrownBy(() -> event.cancelRegistration(ALICE, NOW))
				.isInstanceOf(RegistrationNotFoundException.class);
	}

	@Test
	void updateRejectsCapacityBelowRegisteredParticipants() {
		Event event = scheduleWithCapacity(10);
		event.register(ALICE, NOW);
		event.register(BOB, NOW);

		assertThatThrownBy(() -> event.update(new EventTitle("Meetup"), STARTS_AT, new Capacity(1), NOW))
				.isInstanceOf(CapacityBelowRegistrationsException.class);
	}

	@Test
	void updateReplacesEventDetails() {
		Event event = scheduleWithCapacity(10);
		Instant newStart = STARTS_AT.plus(Duration.ofDays(1));

		event.update(new EventTitle("Conference"), newStart, new Capacity(50), NOW);

		assertThat(event.title()).isEqualTo(new EventTitle("Conference"));
		assertThat(event.startsAt()).isEqualTo(newStart);
		assertThat(event.capacity()).isEqualTo(new Capacity(50));
	}

	@Test
	void pullDomainEventsReturnsEachEventOnlyOnce() {
		Event event = scheduleWithCapacity(10);
		event.pullDomainEvents();

		assertThat(event.pullDomainEvents()).isEmpty();
	}

	private static Event scheduleWithCapacity(int capacity) {
		return Event.schedule(new EventTitle("Meetup"), STARTS_AT, new Capacity(capacity), NOW);
	}

}
