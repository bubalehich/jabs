package io.github.bubalehich.sports.etl;

import io.github.bubalehich.sports.sport.Sport;
import io.github.bubalehich.sports.sport.SportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Locale;

@Component
class SportSetup {

	private static final Logger LOG = LoggerFactory.getLogger(SportSetup.class);

	private final RakutenClient rakutenClient;
	private final SportRepository sportRepository;

	SportSetup(RakutenClient rakutenClient, SportRepository sportRepository) {
		this.rakutenClient = rakutenClient;
		this.sportRepository = sportRepository;
	}

	@EventListener(ApplicationReadyEvent.class)
	void loadSports() {
		importSports().subscribe(
				count -> LOG.info("Imported {} new sports from Rakuten", count),
				error -> LOG.error("Sports import failed", error));
	}

	Mono<Long> importSports() {
		return rakutenClient.searchItems()
				.map(item -> Sport.named(item.itemName()))
				.distinct(sport -> sport.name().toLowerCase(Locale.ROOT))
				.filterWhen(this::isNew)
				.as(sportRepository::saveAll)
				.count();
	}

	private Mono<Boolean> isNew(Sport sport) {
		return sportRepository.existsByNameIgnoreCase(sport.name()).map(exists -> !exists);
	}

}
