package io.github.bubalehich.events.infrastructure.persistence;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "events")
class EventJpaEntity {

	@Id
	private UUID id;

	@Column(nullable = false, length = 200)
	private String title;

	@Column(name = "starts_at", nullable = false)
	private Instant startsAt;

	@Column(nullable = false)
	private int capacity;

	@ElementCollection
	@CollectionTable(name = "event_registrations", joinColumns = @JoinColumn(name = "event_id"))
	@Column(name = "participant_email", nullable = false)
	private Set<String> participants = new LinkedHashSet<>();

	@Version
	private Long version;

	protected EventJpaEntity() {
	}

	EventJpaEntity(UUID id) {
		this.id = id;
	}

	UUID getId() {
		return id;
	}

	String getTitle() {
		return title;
	}

	void setTitle(String title) {
		this.title = title;
	}

	Instant getStartsAt() {
		return startsAt;
	}

	void setStartsAt(Instant startsAt) {
		this.startsAt = startsAt;
	}

	int getCapacity() {
		return capacity;
	}

	void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	Set<String> getParticipants() {
		return participants;
	}

}
