package uk.gov.justice.services.eventsourcing.source.core;


import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.OptimisticLockingRetryException;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.eventsourcing.source.core.exception.VersionMismatchException;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import javax.transaction.Transactional;

import org.slf4j.Logger;

/**
 * Manages operations on {@link EventStream}
 */
public class EventStreamManager {

    private final EventAppender eventAppender;
    private final long maxRetry;
    private final Logger logger;
    private final SystemEventService systemEventService;
    private final Enveloper enveloper;
    private final EventRepository eventRepository;
    private final String eventSourceName;

    public EventStreamManager(
            final EventAppender eventAppender,
            final long maxRetry,
            final SystemEventService systemEventService,
            final Enveloper enveloper,
            final EventRepository eventRepository,
            final String eventSourceName,
            final Logger logger) {

        this.eventAppender = eventAppender;
        this.maxRetry = maxRetry;
        this.logger = logger;
        this.systemEventService = systemEventService;
        this.enveloper = enveloper;
        this.eventRepository = eventRepository;
        this.eventSourceName = eventSourceName;
    }

    /**
     * Get the stream of events.
     *
     * @param id the UUID of the stream
     * @return the stream of events
     */
    public Stream<JsonEnvelope> read(final UUID id) {
        return eventRepository.getEventsByStreamId(id);
    }

    /**
     * Get the stream of events from the given version.
     *
     * @param id       the UUID of the stream
     * @param position the version of the stream
     * @return the stream of events
     */
    public Stream<JsonEnvelope> readFrom(final UUID id, final long position) {
        return eventRepository.getEventsByStreamIdFromPosition(id, position);
    }

    /**
     * Store a stream of events.
     *
     * @param id     the id of the stream
     * @param events the stream of events to store
     * @return the current stream version
     * @throws EventStreamException if an event could not be appended
     */
    @Transactional(dontRollbackOn = OptimisticLockingRetryException.class)
    public long append(final UUID id, final Stream<JsonEnvelope> events) throws EventStreamException {
        return append(id, events, Optional.empty());

    }

    /**
     * Store a stream of events without enforcing consecutive version ids. Reduces risk of throwing
     * optimistic lock error. To be use instead of the append method, when it's acceptable to store
     * events with non consecutive version ids
     *
     * @param streamId - id of the stream to append to
     * @param events   the stream of events to store
     * @return the current stream version
     * @throws EventStreamException if an event could not be appended
     */
    @Transactional(dontRollbackOn = OptimisticLockingRetryException.class)
    public long appendNonConsecutively(final UUID streamId, final Stream<JsonEnvelope> events) throws EventStreamException {
        final List<JsonEnvelope> envelopeList = events.collect(toList());
        long currentVersion = eventRepository.getStreamSize(streamId);

        validateEvents(streamId, envelopeList);

        for (final JsonEnvelope event : envelopeList) {
            boolean appendedSuccessfully = false;
            long retryCount = 0L;
            while (!appendedSuccessfully) {
                try {
                    eventAppender.append(event, streamId, ++currentVersion, eventSourceName);
                    appendedSuccessfully = true;
                } catch (final OptimisticLockingRetryException e) {
                    retryCount++;
                    if (retryCount > maxRetry) {
                        logger.warn("Failed to append to stream {} due to concurrency issues, returning to handler.", streamId);
                        throw e;
                    }
                    currentVersion = eventRepository.getStreamSize(streamId);
                    logger.trace("Retrying appending to stream {}, with version {}", streamId, currentVersion + 1);
                }
            }
        }
        return currentVersion;
    }

    /**
     * Store a stream of events after the given version.
     *
     * @param id      the id of the stream
     * @param events  the stream of events to store
     * @param version the version to append from
     * @return the current version
     * @throws EventStreamException if an event could not be appended
     */
    @Transactional(dontRollbackOn = OptimisticLockingRetryException.class)
    public long appendAfter(final UUID id, final Stream<JsonEnvelope> events, final Long version) throws EventStreamException {
        if (version == null) {
            throw new EventStreamException(format("Failed to append to stream %s. Version must not be null.", id));
        }
        return append(id, events, Optional.of(version));
    }

    /**
     * Clones the stream of events from one stream on to a new stream, to create a backup. This
     * operation does not alter the existing stream. The new stream is marked as inactive in the
     * stream repository and a system event is appended to the copy that points to its origin
     * streamId.
     *
     * @param id - the id of the stream to clone
     * @return the id of the cloned stream
     */
    @Transactional
    public UUID cloneAsAncestor(final UUID id) throws EventStreamException {

        final Stream<JsonEnvelope> existingStream = eventRepository.getEventsByStreamId(id);

        final JsonEnvelope systemEvent = systemEventService.clonedEventFor(id);

        final UUID clonedId = randomUUID();
        append(clonedId, concat(existingStream.map(this::stripMetadataFrom), of(systemEvent)));

        eventRepository.markEventStreamActive(clonedId, false);

        existingStream.close();

        return clonedId;
    }

    /**
     * Clears the stream, deleting all associated events from the event_log, it does not update the
     * event_stream.
     */
    public void clear(final UUID id) {
        eventRepository.clearEventsForStream(id);
    }

    /**
     * Get the latest position number for a stream
     *
     * @param id the id of the stream
     * @return the latest position number for the provided steam. 0 when stream is empty.
     */
    public long getSize(final UUID id) {
        return eventRepository.getStreamSize(id);
    }

    /**
     * Get the position of the stream within the streams
     *
     * @return the latest position number for the provided steam.
     */
    public long getStreamPosition(final UUID streamId) {
        return eventRepository.getStreamPosition(streamId);
    }

    private long append(final UUID id, final Stream<JsonEnvelope> events, final Optional<Long> positionFrom) throws EventStreamException {
        final List<JsonEnvelope> envelopeList = events.collect(toList());

        long currentPosition = eventRepository.getStreamSize(id);
        if (positionFrom.isPresent()) {
            validateVersion(id, positionFrom.get(), currentPosition);
        }
        validateEvents(id, envelopeList);

        for (final JsonEnvelope event : envelopeList) {
            eventAppender.append(event, id, ++currentPosition, eventSourceName);
        }
        return currentPosition;
    }

    private void validateEvents(final UUID id, final List<JsonEnvelope> envelopeList) throws EventStreamException {
        if (envelopeList.stream().anyMatch(e -> e.metadata().position().isPresent())) {
            throw new EventStreamException(format("Failed to append to stream %s. Version must be empty.", id));
        }
    }

    private void validateVersion(final UUID id, final Long versionFrom, final Long currentVersion) throws VersionMismatchException {
        if (versionFrom > currentVersion) {
            throw new VersionMismatchException(format("Failed to append to stream %s due to a version mismatch; expected %d, found %d",
                    id, versionFrom, currentVersion));
        } else if (versionFrom < currentVersion) {
            throw new OptimisticLockingRetryException(format("Optimistic locking failure while storing version %s of stream %s which is already at %s",
                    versionFrom + 1, id, currentVersion));
        }
    }

    /**
     * Clears the version (and other metadata) from the events so they can be appended as fresh
     * events.
     *
     * @param event - the event to have its metadata cleared
     * @return the event with cleared metadata
     */
    private JsonEnvelope stripMetadataFrom(final JsonEnvelope event) {
        return enveloper.withMetadataFrom(event, event.metadata().name()).apply(event.payload());
    }
}
