CREATE TABLE events (
    id        UUID                     NOT NULL,
    title     VARCHAR(200)             NOT NULL,
    starts_at TIMESTAMP WITH TIME ZONE NOT NULL,
    capacity  INTEGER                  NOT NULL,
    version   BIGINT                   NOT NULL,
    CONSTRAINT pk_events PRIMARY KEY (id),
    CONSTRAINT ck_events_capacity_positive CHECK (capacity > 0)
);

CREATE INDEX ix_events_starts_at ON events (starts_at, id);

CREATE TABLE event_registrations (
    event_id          UUID         NOT NULL,
    participant_email VARCHAR(255) NOT NULL,
    CONSTRAINT pk_event_registrations PRIMARY KEY (event_id, participant_email),
    CONSTRAINT fk_event_registrations_event FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE
);
