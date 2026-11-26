package io.github.bubalehich.sports.api;

import io.github.bubalehich.sports.sport.Sport;
import io.github.bubalehich.sports.sport.SportRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static java.util.function.Predicate.not;

@Component
class SportHandler {

	private final SportRepository sportRepository;

	SportHandler(SportRepository sportRepository) {
		this.sportRepository = sportRepository;
	}

	Mono<ServerResponse> create(ServerRequest request) {
		String name = request.pathVariable("sportName").strip();
		if (name.isEmpty()) {
			return problem(HttpStatus.BAD_REQUEST, "Sport name must not be blank");
		}
		return sportRepository.save(Sport.named(name))
				.flatMap(sport -> ServerResponse.status(HttpStatus.CREATED).bodyValue(sport))
				.onErrorResume(DuplicateKeyException.class,
						ex -> problem(HttpStatus.CONFLICT, "Sport '%s' already exists".formatted(name)));
	}

	Mono<ServerResponse> search(ServerRequest request) {
		return request.queryParam("q")
				.filter(not(String::isBlank))
				.map(query -> ServerResponse.ok().body(sportRepository.findByNameContainingIgnoreCase(query), Sport.class))
				.orElseGet(() -> problem(HttpStatus.BAD_REQUEST, "Query parameter 'q' is required"));
	}

	private static Mono<ServerResponse> problem(HttpStatus status, String detail) {
		return ServerResponse.status(status)
				.contentType(MediaType.APPLICATION_PROBLEM_JSON)
				.bodyValue(ProblemDetail.forStatusAndDetail(status, detail));
	}

}
