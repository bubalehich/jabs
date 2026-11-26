package io.github.bubalehich.sports.etl;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
class RakutenClient {

	private static final String SEARCH_PATH = "/ichibams/api/IchibaItem/Search/20220601";

	private final WebClient webClient;
	private final RakutenProperties properties;

	RakutenClient(WebClient.Builder webClientBuilder, RakutenProperties properties) {
		this.webClient = webClientBuilder.baseUrl(properties.baseUrl()).build();
		this.properties = properties;
	}

	Flux<RakutenSearchResponse.Item> searchItems() {
		return Flux.range(1, properties.pages())
				.concatMap(this::fetchPage)
				.flatMapIterable(RakutenSearchResponse::items);
	}

	private Mono<RakutenSearchResponse> fetchPage(int page) {
		return webClient.get()
				.uri(uri -> uri.path(SEARCH_PATH)
						.queryParam("applicationId", properties.applicationId())
						.queryParam("keyword", properties.keyword())
						.queryParam("hits", properties.hitsPerPage())
						.queryParam("page", page)
						.queryParam("formatVersion", 2)
						.build())
				.header("accessKey", properties.accessKey())
				.retrieve()
				.bodyToMono(RakutenSearchResponse.class);
	}

}
