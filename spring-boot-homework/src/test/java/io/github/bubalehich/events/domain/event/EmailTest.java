package io.github.bubalehich.events.domain.event;

import io.github.bubalehich.events.domain.event.exception.InvalidEventDataException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {

	@Test
	void normalizesCaseAndSurroundingWhitespace() {
		assertThat(new Email("  Alice@Example.COM ")).isEqualTo(new Email("alice@example.com"));
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { "alice", "alice@", "alice@example", "al ice@example.com" })
	void rejectsMalformedAddress(String value) {
		assertThatThrownBy(() -> new Email(value)).isInstanceOf(InvalidEventDataException.class);
	}

}
