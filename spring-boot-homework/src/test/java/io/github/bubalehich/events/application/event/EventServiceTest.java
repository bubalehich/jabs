package io.github.bubalehich.events.application.event;

import io.github.bubalehich.events.domain.event.Capacity;
import io.github.bubalehich.events.domain.event.Email;
import io.github.bubalehich.events.domain.event.Event;
import io.github.bubalehich.events.domain.event.EventId;
import io.github.bubalehich.events.domain.event.EventRepository;
import io.github.bubalehich.events.domain.event.EventScheduled;
import io.github.bubalehich.events.domain.event.EventTitle;
import io.github.bubalehich.events.domain.event.ParticipantRegistered;
import io.github.bubalehich.events.domain.event.RegistrationCancelled;
import io.github.bubalehich.events.domain.event.exception.EventFullException;
import io.github.bubalehich.events.domain.event.exception.EventNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

	private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");
	private static final Instant STARTS_AT = NOW.plus(Duration.ofDays(7));
	private static final String ALICE = "alice@example.com";

	@Mock
	private EventRepository events;

	@Mock
	private ApplicationEventPublisher publisher;

	private EventService eventService;

	@BeforeEach
	void setUp() {
		eventService = new EventService(events, Clock.fixed(NOW, ZoneOffset.UTC), publisher);
	}

	@Test
	void createSavesEventAndPublishesEventScheduled() {
		Event event = eventService.create(new EventDetailsCommand("Meetup", STARTS_AT, 10));

		verify(events).save(event);
		verify(publisher).publishEvent(new EventScheduled(event.id(), NOW));
	}

	@Test
	void getFailsForUnknownEvent() {
		EventId id = EventId.newId();
		given(events.findById(id)).willReturn(Optional.empty());

		assertThatThrownBy(() -> eventService.get(id.value())).isInstanceOf(EventNotFoundException.class);
	}

	@Test
	void updateSavesChangedEvent() {
		Event event = existingEvent(10);

		Event updated = eventService.update(event.id().value(), new EventDetailsCommand("Conference", STARTS_AT, 20));

		assertThat(updated.title()).isEqualTo(new EventTitle("Conference"));
		assertThat(updated.capacity()).isEqualTo(new Capacity(20));
		verify(events).save(event);
	}

	@Test
	void deleteRemovesExistingEvent() {
		Event event = existingEvent(10);

		eventService.delete(event.id().value());

		verify(events).delete(event.id());
	}

	@Test
	void deleteFailsForUnknownEventWithoutTouchingRepository() {
		EventId id = EventId.newId();
		given(events.findById(id)).willReturn(Optional.empty());

		assertThatThrownBy(() -> eventService.delete(id.value())).isInstanceOf(EventNotFoundException.class);
		verify(events, never()).delete(any());
	}

	@Test
	void registerSavesParticipantAndPublishesParticipantRegistered() {
		Event event = existingEvent(10);

		eventService.register(event.id().value(), ALICE);

		assertThat(event.participants()).containsExactly(new Email(ALICE));
		verify(events).save(event);
		verify(publisher).publishEvent(new ParticipantRegistered(event.id(), new Email(ALICE), NOW));
	}

	@Test
	void registerDoesNotSaveOrPublishWhenDomainRuleIsViolated() {
		Event event = existingEvent(1, "bob@example.com");

		assertThatThrownBy(() -> eventService.register(event.id().value(), ALICE))
				.isInstanceOf(EventFullException.class);
		verify(events, never()).save(any());
		verifyNoInteractions(publisher);
	}

	@Test
	void cancelRegistrationSavesEventAndPublishesRegistrationCancelled() {
		Event event = existingEvent(10, ALICE);

		eventService.cancelRegistration(event.id().value(), ALICE);

		assertThat(event.participants()).isEmpty();
		verify(events).save(event);
		verify(publisher).publishEvent(new RegistrationCancelled(event.id(), new Email(ALICE), NOW));
	}

	private Event existingEvent(int capacity, String... participants) {
		Event event = Event.restore(EventId.newId(), new EventTitle("Meetup"), STARTS_AT, new Capacity(capacity),
				Arrays.stream(participants).map(Email::new).toList());
		given(events.findById(event.id())).willReturn(Optional.of(event));
		return event;
	}

}
