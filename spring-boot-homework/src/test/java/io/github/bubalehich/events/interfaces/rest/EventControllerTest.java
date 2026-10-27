package io.github.bubalehich.events.interfaces.rest;

import io.github.bubalehich.events.application.event.EventDetailsCommand;
import io.github.bubalehich.events.application.event.EventService;
import io.github.bubalehich.events.domain.event.Capacity;
import io.github.bubalehich.events.domain.event.Event;
import io.github.bubalehich.events.domain.event.EventId;
import io.github.bubalehich.events.domain.event.EventTitle;
import io.github.bubalehich.events.domain.event.exception.EventFullException;
import io.github.bubalehich.events.domain.event.exception.EventNotFoundException;
import io.github.bubalehich.events.infrastructure.security.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.oauth2.server.authorization.autoconfigure.servlet.OAuth2AuthorizationServerAutoConfiguration;
import org.springframework.boot.security.oauth2.server.authorization.autoconfigure.servlet.OAuth2AuthorizationServerJwtAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
@Import(SecurityConfiguration.class)
@ImportAutoConfiguration({ OAuth2AuthorizationServerAutoConfiguration.class,
		OAuth2AuthorizationServerJwtAutoConfiguration.class })
class EventControllerTest {

	private static final Instant STARTS_AT = Instant.parse("2099-01-01T10:00:00Z");
	private static final String VALID_REQUEST = """
			{"title": "Meetup", "startsAt": "2099-01-01T10:00:00Z", "capacity": 10}
			""";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private EventService eventService;

	@Test
	void createReturnsCreatedEventWithLocation() throws Exception {
		Event event = Event.restore(EventId.newId(), new EventTitle("Meetup"), STARTS_AT, new Capacity(10), List.of());
		given(eventService.create(new EventDetailsCommand("Meetup", STARTS_AT, 10))).willReturn(event);

		mockMvc.perform(post("/api/events").with(admin())
						.contentType(MediaType.APPLICATION_JSON)
						.content(VALID_REQUEST))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", endsWith("/api/events/" + event.id())))
				.andExpect(jsonPath("$.title").value("Meetup"))
				.andExpect(jsonPath("$.availableSeats").value(10));
	}

	@Test
	void createRejectsInvalidRequestBeforeReachingService() throws Exception {
		mockMvc.perform(post("/api/events").with(admin())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title": " ", "startsAt": "2099-01-01T10:00:00Z", "capacity": 0}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors", hasSize(2)));
		verifyNoInteractions(eventService);
	}

	@Test
	void createIsForbiddenForUser() throws Exception {
		mockMvc.perform(post("/api/events").with(user("alice@example.com"))
						.contentType(MediaType.APPLICATION_JSON)
						.content(VALID_REQUEST))
				.andExpect(status().isForbidden());
		verifyNoInteractions(eventService);
	}

	@Test
	void listRequiresAuthentication() throws Exception {
		mockMvc.perform(get("/api/events"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void listRejectsPageSizeAboveMaximum() throws Exception {
		mockMvc.perform(get("/api/events").param("size", "101").with(user("alice@example.com")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors", hasSize(1)));
	}

	@Test
	void getReturnsNotFoundForUnknownEvent() throws Exception {
		EventId id = EventId.newId();
		given(eventService.get(id.value())).willThrow(new EventNotFoundException(id));

		mockMvc.perform(get("/api/events/{id}", id.value()).with(user("alice@example.com")))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("Event " + id + " not found"));
	}

	@Test
	void registerUsesEmailFromToken() throws Exception {
		UUID id = UUID.randomUUID();

		mockMvc.perform(post("/api/events/{id}/registrations", id).with(user("alice@example.com")))
				.andExpect(status().isCreated());
		verify(eventService).register(id, "alice@example.com");
	}

	@Test
	void registerReturnsConflictWhenEventIsFull() throws Exception {
		EventId id = EventId.newId();
		willThrow(new EventFullException(id)).given(eventService).register(any(), any());

		mockMvc.perform(post("/api/events/{id}/registrations", id.value()).with(user("alice@example.com")))
				.andExpect(status().isConflict());
	}

	private static RequestPostProcessor admin() {
		return jwt().jwt(token -> token.subject("admin@example.com"))
				.authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
	}

	private static RequestPostProcessor user(String email) {
		return jwt().jwt(token -> token.subject(email))
				.authorities(new SimpleGrantedAuthority("ROLE_USER"));
	}

}
