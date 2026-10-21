package io.github.bubalehich.events.domain.event.exception;

public abstract sealed class EventRuleViolationException extends RuntimeException
		permits AlreadyRegisteredException, CapacityBelowRegistrationsException, EventAlreadyStartedException,
		EventFullException {

	protected EventRuleViolationException(String message) {
		super(message);
	}

}
