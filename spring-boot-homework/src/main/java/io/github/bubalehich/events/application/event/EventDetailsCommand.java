package io.github.bubalehich.events.application.event;

import java.time.Instant;

public record EventDetailsCommand(String title, Instant startsAt, int capacity) {
}
