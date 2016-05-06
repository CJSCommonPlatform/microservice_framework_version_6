package uk.gov.justice.services.eventsourcing.source.core;


import uk.gov.justice.services.eventsourcing.publisher.core.EventPublisher;
import uk.gov.justice.services.eventsourcing.repository.core.EventRepository;
import uk.gov.justice.services.eventsourcing.repository.core.exception.StoreEventRequestFailedException;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.eventsourcing.source.core.exception.InvalidStreamVersionRuntimeException;
import uk.gov.justice.services.eventsourcing.source.core.exception.VersionMismatchException;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * Manages operations on {@link EventStream}
 */
public class EventStreamManager {

    @Inject
    EventRepository eventRepository;

    @Inject
    EventPublisher eventPublisher;

    /**
     * Get the stream of events.
     *
     * @return the stream of events
     */
    public Stream<JsonEnvelope> read(final UUID id) {
        return eventRepository.getByStreamId(id);
    }

    /**
     * Get the stream of events from the given version.
     *
     * @return the stream of events
     */
    public Stream<JsonEnvelope> readFrom(final UUID id, final Long version) {
        if (version > eventRepository.getCurrentSequenceIdForStream(id)) {
            throw new InvalidStreamVersionRuntimeException(String.format("Failed to read from stream %s. Version %d does not exist.", id, version));
        }
        return eventRepository.getByStreamIdAndSequenceId(id, version);
    }

    /**
     * Store a stream of events.
     *
     * @param events the stream of events to store
     * @throws EventStreamException if an event could not be appended
     */
    @Transactional
    public void append(final UUID id, final Stream<JsonEnvelope> events) throws EventStreamException {
        append(id, events, Optional.empty());
    }

    /**
     * Store a stream of events after the given version.
     *
     * @param events  the stream of events to store
     * @param version the version to append from
     * @throws EventStreamException if an event could not be appended
     */
    @Transactional
    public void appendAfter(final UUID id, final Stream<JsonEnvelope> events, final Long version) throws EventStreamException {
        if (version == null) {
            throw new EventStreamException(String.format("Failed to append to stream %s. Version must not be null.", id));
        }
        append(id, events, Optional.of(version));
    }

    /**
     * Get the current (current maximum) sequence id (version number) for a stream
     *
     * @return the latest sequence id for the provided steam. 0 when stream is empty.
     */
    public Long getCurrentVersion(final UUID id) {
        return eventRepository.getCurrentSequenceIdForStream(id);
    }

    private void append(final UUID id, final Stream<JsonEnvelope> events, final Optional<Long> versionFrom) throws EventStreamException {
        List<JsonEnvelope> envelopeList = events.collect(Collectors.toList());

        Long currentVersion = eventRepository.getCurrentSequenceIdForStream(id);

        validateEvents(id, envelopeList, versionFrom, currentVersion);

        for (final JsonEnvelope event : envelopeList) {
            try {
                eventRepository.store(event, id, ++currentVersion);
                eventPublisher.publish(event);
            } catch (StoreEventRequestFailedException e) {
                throw new EventStreamException(String.format("Failed to append event to Event Store %s", event.metadata().id()), e);
            }
        }
    }

    private void validateEvents(final UUID id, final List<JsonEnvelope> envelopeList, final Optional<Long> versionFrom, final Long currentVersion) throws EventStreamException {
        if (versionFrom.isPresent() && !versionFrom.get().equals(currentVersion)) {
            throw new VersionMismatchException(String.format("Failed to append to stream %s. Version mismatch. Expected %d, Found %d",
                    id, versionFrom.get(), currentVersion));
        }

        if (envelopeList.stream().anyMatch(e -> e.metadata().version().isPresent())) {
            throw new EventStreamException(String.format("Failed to append to stream %s. Version must be empty.", id));
        }
    }
}
