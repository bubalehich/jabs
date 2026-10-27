package io.github.bubalehich.events;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EventRegistrationIntegrationTest {

	private static final String ADMIN = "admin@example.com";
	private static final String ALICE = "alice@example.com";
	private static final String BOB = "bob@example.com";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JWKSource<SecurityContext> jwkSource;

	@Autowired
	private MeterRegistry meterRegistry;

	@Test
	void participantRegistersAndCancelsUntilEventIsFull() throws Exception {
		String event = createEvent(1);

		mockMvc.perform(post(event + "/registrations").header(HttpHeaders.AUTHORIZATION, bearer(ALICE, "USER")))
				.andExpect(status().isCreated());
		mockMvc.perform(post(event + "/registrations").header(HttpHeaders.AUTHORIZATION, bearer(ALICE, "USER")))
				.andExpect(status().isConflict());
		mockMvc.perform(post(event + "/registrations").header(HttpHeaders.AUTHORIZATION, bearer(BOB, "USER")))
				.andExpect(status().isConflict());
		mockMvc.perform(get(event).header(HttpHeaders.AUTHORIZATION, bearer(BOB, "USER")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.registeredCount").value(1))
				.andExpect(jsonPath("$.availableSeats").value(0));

		mockMvc.perform(delete(event + "/registrations/me").header(HttpHeaders.AUTHORIZATION, bearer(ALICE, "USER")))
				.andExpect(status().isNoContent());
		mockMvc.perform(get(event).header(HttpHeaders.AUTHORIZATION, bearer(BOB, "USER")))
				.andExpect(jsonPath("$.registeredCount").value(0));
	}

	@Test
	void registrationIsExposedAsPrometheusMetric() throws Exception {
		String event = createEvent(10);
		double before = meterRegistry.get("events.registrations").counter().count();

		mockMvc.perform(post(event + "/registrations").header(HttpHeaders.AUTHORIZATION, bearer(ALICE, "USER")))
				.andExpect(status().isCreated());

		assertThat(meterRegistry.get("events.registrations").counter().count()).isEqualTo(before + 1);
		mockMvc.perform(get("/actuator/prometheus"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("events_registrations_total")));
	}

	@Test
	void tokenWithoutRolesIsForbidden() throws Exception {
		mockMvc.perform(get("/api/events").header(HttpHeaders.AUTHORIZATION, bearer(ALICE)))
				.andExpect(status().isForbidden());
	}

	private String createEvent(int capacity) throws Exception {
		String location = mockMvc.perform(post("/api/events")
						.header(HttpHeaders.AUTHORIZATION, bearer(ADMIN, "ADMIN"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title": "Meetup", "startsAt": "%s", "capacity": %d}
								""".formatted(Instant.now().plus(Duration.ofDays(7)), capacity)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getHeader(HttpHeaders.LOCATION);
		assertThat(location).isNotNull();
		return location;
	}

	private String bearer(String subject, String... roles) {
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.subject(subject)
				.issuedAt(now)
				.expiresAt(now.plus(Duration.ofMinutes(5)))
				.claim("roles", List.of(roles))
				.build();
		JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
		String token = new NimbusJwtEncoder(jwkSource).encode(JwtEncoderParameters.from(header, claims))
				.getTokenValue();
		return "Bearer " + token;
	}

}
