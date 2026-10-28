package io.github.bubalehich.events.infrastructure.persistence;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class EventJpaEntity {

	@Id
	private UUID id;

	@Setter
	@Column(nullable = false, length = 200)
	private String title;

	@Setter
	@Column(name = "starts_at", nullable = false)
	private Instant startsAt;

	@Setter
	@Column(nullable = false)
	private int capacity;

	@ElementCollection
	@CollectionTable(name = "event_registrations", joinColumns = @JoinColumn(name = "event_id"))
	@Column(name = "participant_email", nullable = false)
	private Set<String> participants = new LinkedHashSet<>();

	@Version
	private Long version;

	EventJpaEntity(UUID id) {
		this.id = id;
	}

}
