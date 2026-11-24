package io.github.bubalehich.sports.sport;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface SportRepository extends ReactiveCrudRepository<Sport, Integer> {

	Mono<Boolean> existsByNameIgnoreCase(String name);

}
