package io.github.bubalehich.events.infrastructure.metrics;

import io.github.bubalehich.events.domain.event.EventRepository;
import io.github.bubalehich.events.domain.event.EventScheduled;
import io.github.bubalehich.events.domain.event.ParticipantRegistered;
import io.github.bubalehich.events.domain.event.RegistrationCancelled;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Clock;

@Component
class EventMetrics {

	private final Counter scheduled;
	private final Counter registrations;
	private final Counter registrationCancellations;

	EventMetrics(MeterRegistry registry, EventRepository events, Clock clock) {
		scheduled = Counter.builder("events.scheduled")
				.description("Events scheduled")
				.register(registry);
		registrations = Counter.builder("events.registrations")
				.description("Participant registrations")
				.register(registry);
		registrationCancellations = Counter.builder("events.registration.cancellations")
				.description("Cancelled participant registrations")
				.register(registry);
		Gauge.builder("events.upcoming", () -> events.countStartingAfter(clock.instant()))
				.description("Events that have not started yet")
				.register(registry);
	}

	@TransactionalEventListener
	void on(EventScheduled event) {
		scheduled.increment();
	}

	@TransactionalEventListener
	void on(ParticipantRegistered event) {
		registrations.increment();
	}

	@TransactionalEventListener
	void on(RegistrationCancelled event) {
		registrationCancellations.increment();
	}

}
