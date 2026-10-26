package io.github.bubalehich.events.domain.event;

import io.github.bubalehich.events.domain.common.DomainEvent;

import java.time.Instant;

public record EventScheduled(EventId eventId, Instant occurredAt) implements DomainEvent {
}
