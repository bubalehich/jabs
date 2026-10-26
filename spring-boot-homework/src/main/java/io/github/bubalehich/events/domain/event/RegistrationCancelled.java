package io.github.bubalehich.events.domain.event;

import io.github.bubalehich.events.domain.common.DomainEvent;

import java.time.Instant;

public record RegistrationCancelled(EventId eventId, Email participant, Instant occurredAt) implements DomainEvent {
}
