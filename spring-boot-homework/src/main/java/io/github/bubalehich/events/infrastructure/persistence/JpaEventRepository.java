package io.github.bubalehich.events.infrastructure.persistence;

import io.github.bubalehich.events.domain.common.PageQuery;
import io.github.bubalehich.events.domain.common.PageResult;
import io.github.bubalehich.events.domain.event.Event;
import io.github.bubalehich.events.domain.event.EventId;
import io.github.bubalehich.events.domain.event.EventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class JpaEventRepository implements EventRepository {

	private static final Sort BY_START_TIME = Sort.by("startsAt", "id");

	private final SpringDataEventRepository springData;

	JpaEventRepository(SpringDataEventRepository springData) {
		this.springData = springData;
	}

	@Override
	public Optional<Event> findById(EventId id) {
		return springData.findById(id.value()).map(EventPersistenceMapper::toDomain);
	}

	@Override
	public PageResult<Event> findAll(PageQuery query) {
		Page<EventJpaEntity> page = springData.findAll(PageRequest.of(query.page(), query.size(), BY_START_TIME));
		return new PageResult<>(page.map(EventPersistenceMapper::toDomain).getContent(), query.page(),
				query.size(), page.getTotalElements());
	}

	@Override
	public void save(Event event) {
		EventJpaEntity entity = springData.findById(event.id().value())
				.orElseGet(() -> new EventJpaEntity(event.id().value()));
		EventPersistenceMapper.copyToEntity(event, entity);
		springData.save(entity);
	}

	@Override
	public void delete(EventId id) {
		springData.deleteById(id.value());
	}

}
