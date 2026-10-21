package io.github.bubalehich.events.domain.event;

import io.github.bubalehich.events.domain.event.exception.InvalidEventDataException;

import java.util.Locale;
import java.util.regex.Pattern;

public record Email(String value) {

	private static final Pattern FORMAT = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

	public Email {
		if (value == null || !FORMAT.matcher(value.strip()).matches()) {
			throw new InvalidEventDataException("Invalid email: " + value);
		}
		value = value.strip().toLowerCase(Locale.ROOT);
	}

	@Override
	public String toString() {
		return value;
	}

}
