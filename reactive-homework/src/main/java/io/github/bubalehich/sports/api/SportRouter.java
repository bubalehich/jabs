package io.github.bubalehich.sports.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
class SportRouter {

	@Bean
	RouterFunction<ServerResponse> sportRoutes(SportHandler handler) {
		return RouterFunctions.route()
				.path("/api/v1/sport", sport -> sport
						.POST("/{sportName}", handler::create)
						.GET(handler::search))
				.build();
	}

}
