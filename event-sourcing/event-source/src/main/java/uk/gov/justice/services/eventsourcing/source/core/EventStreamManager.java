package uk.gov.justice.services.eventsourcing.source.core;


import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataFrom;

import uk.gov.justice.services.eventsourcing.publisher.core.EventPublisher;
import uk.gov.justice.services.eventsourcing.repository.core.EventRepository;
import uk.gov.justice.services.eventsourcing.repository.core.exception.OptimisticLockingRetryException;
import uk.gov.justice.services.eventsourcing.repository.core.exception.StoreEventRequestFailedException;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.eventsourcing.source.core.exception.InvalidStreamVersionRuntimeException;
import uk.gov.justice.services.eventsourcing.source.core.exception.VersionMismatchException;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
    public Stream<JsonEnvelope> readFrom(final UUID id, final long version) {
        return eventRepository.getByStreamIdAndSequenceId(id, version);
    }

    /**
     * Store a stream of events.
     *
     * @param events the stream of events to store
     * @throws EventStreamException if an event could not be appended
     * @return the current stream version
     */
    @Transactional(dontRollbackOn = OptimisticLockingRetryException.class)
    public long append(final UUID id, final Stream<JsonEnvelope> events) throws EventStreamException {
        return append(id, events, Optional.empty());

    }

    /**
     * Store a stream of events after the given version.
     *
     * @param events  the stream of events to store
     * @param version the version to append from
     * @throws EventStreamException if an event could not be appended
     */
    @Transactional(dontRollbackOn = OptimisticLockingRetryException.class)
    public long appendAfter(final UUID id, final Stream<JsonEnvelope> events, final Long version) throws EventStreamException {
        if (version == null) {
            throw new EventStreamException(String.format("Failed to append to stream %s. Version must not be null.", id));
        }
        return append(id, events, Optional.of(version));

    }

    /**
     * Get the current (current maximum) sequence id (version number) for a stream
     *
     * @return the latest sequence id for the provided steam. 0 when stream is empty.
     */
    public long getCurrentVersion(final UUID id) {
        return eventRepository.getCurrentSequenceIdForStream(id);
    }

    private long append(final UUID id, final Stream<JsonEnvelope> events, final Optional<Long> versionFrom) throws EventStreamException {
        final List<JsonEnvelope> envelopeList = events.collect(toList());

        long currentVersion = eventRepository.getCurrentSequenceIdForStream(id);

        validateEvents(id, envelopeList, versionFrom, currentVersion);

        for (final JsonEnvelope event : envelopeList) {
            try {
                final JsonEnvelope eventWithVersion = eventFrom(event, id, ++currentVersion);
                eventRepository.store(eventWithVersion);

                eventPublisher.publish(eventWithVersion);
            } catch (StoreEventRequestFailedException e) {
                throw new EventStreamException(String.format("Failed to append event to Event Store %s", event.metadata().id()), e);
            }
        }
        return currentVersion;
    }

    private JsonEnvelope eventFrom(final JsonEnvelope event, final UUID streamId, final Long version) {
        return envelopeFrom(metadataFrom(event.metadata()).withStreamId(streamId).withVersion(version), event.payloadAsJsonObject());
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
