package io.github.bubalehich.sports.etl;

import io.github.bubalehich.sports.sport.Sport;
import io.github.bubalehich.sports.sport.SportRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

@Component
class SportSetup {

	private static final int BATCH_SIZE = 20;

	private final RakutenClient rakutenClient;
	private final SportRepository sportRepository;

	SportSetup(RakutenClient rakutenClient, SportRepository sportRepository) {
		this.rakutenClient = rakutenClient;
		this.sportRepository = sportRepository;
	}

	@EventListener(ApplicationReadyEvent.class)
	void loadSports() {
		rakutenClient.searchItems()
				.map(item -> Sport.named(item.itemName()))
				.distinct(sport -> sport.name().toLowerCase(Locale.ROOT))
				.log("sports.etl", Level.INFO, SignalType.REQUEST, SignalType.ON_NEXT, SignalType.ON_COMPLETE)
				.subscribe(new BatchSubscriber<>(BATCH_SIZE, this::saveNew));
	}

	private Mono<Long> saveNew(List<Sport> sports) {
		return Flux.fromIterable(sports)
				.filterWhen(this::isNew)
				.as(sportRepository::saveAll)
				.count();
	}

	private Mono<Boolean> isNew(Sport sport) {
		return sportRepository.existsByNameIgnoreCase(sport.name()).map(exists -> !exists);
	}

}
