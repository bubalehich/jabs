package io.github.bubalehich.events.domain.event;

import io.github.bubalehich.events.domain.common.PageQuery;
import io.github.bubalehich.events.domain.common.PageResult;

import java.time.Instant;
import java.util.Optional;

public interface EventRepository {

	Optional<Event> findById(EventId id);

	PageResult<Event> findAll(PageQuery query);

	void save(Event event);

	void delete(EventId id);

	long countStartingAfter(Instant moment);

}
