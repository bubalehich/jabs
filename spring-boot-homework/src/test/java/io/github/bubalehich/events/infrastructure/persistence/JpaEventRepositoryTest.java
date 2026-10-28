package io.github.bubalehich.events.infrastructure.persistence;

import io.github.bubalehich.events.domain.common.PageQuery;
import io.github.bubalehich.events.domain.common.PageResult;
import io.github.bubalehich.events.domain.event.Capacity;
import io.github.bubalehich.events.domain.event.Email;
import io.github.bubalehich.events.domain.event.Event;
import io.github.bubalehich.events.domain.event.EventId;
import io.github.bubalehich.events.domain.event.EventRepository;
import io.github.bubalehich.events.domain.event.EventTitle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaEventRepository.class)
class JpaEventRepositoryTest {

	private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");
	private static final Email ALICE = new Email("alice@example.com");
	private static final Email BOB = new Email("bob@example.com");

	@Autowired
	private EventRepository events;

	@Autowired
	private TestEntityManager entityManager;

	@Test
	void restoresSavedAggregateWithParticipants() {
		Event event = event("Meetup", NOW.plus(Duration.ofDays(1)), ALICE, BOB);

		events.save(event);
		flushAndClear();

		Event restored = events.findById(event.id()).orElseThrow();
		assertThat(restored.title()).isEqualTo(event.title());
		assertThat(restored.startsAt()).isEqualTo(event.startsAt());
		assertThat(restored.capacity()).isEqualTo(event.capacity());
		assertThat(restored.participants()).containsExactlyInAnyOrder(ALICE, BOB);
	}

	@Test
	void saveRemovesCancelledRegistrations() {
		Event event = event("Meetup", NOW.plus(Duration.ofDays(1)), ALICE, BOB);
		events.save(event);
		flushAndClear();

		Event loaded = events.findById(event.id()).orElseThrow();
		loaded.cancelRegistration(ALICE, NOW);
		events.save(loaded);
		flushAndClear();

		assertThat(events.findById(event.id()).orElseThrow().participants()).containsExactly(BOB);
	}

	@Test
	void findAllReturnsRequestedPageOrderedByStartTime() {
		Event third = event("Third", NOW.plus(Duration.ofDays(3)));
		Event first = event("First", NOW.plus(Duration.ofDays(1)));
		Event second = event("Second", NOW.plus(Duration.ofDays(2)));
		List.of(third, first, second).forEach(events::save);
		flushAndClear();

		PageResult<Event> page = events.findAll(new PageQuery(0, 2));

		assertThat(page.content()).extracting(Event::id).containsExactly(first.id(), second.id());
		assertThat(page.totalElements()).isEqualTo(3);
		assertThat(page.totalPages()).isEqualTo(2);
	}

	@Test
	void deleteRemovesEventTogetherWithRegistrations() {
		Event event = event("Meetup", NOW.plus(Duration.ofDays(1)), ALICE);
		events.save(event);
		flushAndClear();

		events.delete(event.id());
		flushAndClear();

		assertThat(events.findById(event.id())).isEmpty();
		assertThat(countRows("event_registrations")).isZero();
	}

	@Test
	void countStartingAfterIgnoresEarlierEvents() {
		events.save(event("Past", NOW.minus(Duration.ofDays(1))));
		events.save(event("Upcoming", NOW.plus(Duration.ofDays(1))));
		flushAndClear();

		assertThat(events.countStartingAfter(NOW)).isEqualTo(1);
	}

	private static Event event(String title, Instant startsAt, Email... participants) {
		return Event.restore(EventId.newId(), new EventTitle(title), startsAt, new Capacity(10),
				List.of(participants));
	}

	private void flushAndClear() {
		entityManager.flush();
		entityManager.clear();
	}

	private long countRows(String table) {
		return ((Number) entityManager.getEntityManager()
				.createNativeQuery("SELECT COUNT(*) FROM " + table)
				.getSingleResult()).longValue();
	}

}
