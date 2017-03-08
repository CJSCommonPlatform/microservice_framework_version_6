package uk.gov.justice.services.eventsourcing.source.core;


import static java.util.stream.Collectors.toList;

import uk.gov.justice.services.common.configuration.GlobalValue;
import uk.gov.justice.services.eventsourcing.repository.core.EventRepository;
import uk.gov.justice.services.eventsourcing.repository.core.exception.OptimisticLockingRetryException;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.eventsourcing.source.core.exception.VersionMismatchException;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;

/**
 * Manages operations on {@link EventStream}
 */
@ApplicationScoped
public class EventStreamManager {

    @Inject
    private Logger logger;

    @Inject
    EventRepository eventRepository;

    @Inject
    EventAppender eventAppender;

    @Inject
    @GlobalValue(key = "internal.max.retry", defaultValue = "20")
    long maxRetry;


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
        long currentVersion = eventRepository.getCurrentSequenceIdForStream(streamId);

        validateEvents(streamId, envelopeList);

        for (final JsonEnvelope event : envelopeList) {
            boolean appendedSuccessfully = false;
            long retryCount = 0L;
            while (!appendedSuccessfully) {
                try {
                    eventAppender.append(event, streamId, ++currentVersion);
                    appendedSuccessfully = true;
                } catch (OptimisticLockingRetryException e) {
                    retryCount++;
                    if (retryCount > maxRetry) {
                        logger.warn("Failed to append to stream {} due to concurrency issues, returning to handler.", streamId);
                        throw e;
                    }
                    currentVersion = eventRepository.getCurrentSequenceIdForStream(streamId);
                    logger.trace("Retrying appending to stream {}, with version {}", streamId, currentVersion + 1);
                }
            }
        }
        return currentVersion;
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
        validateVersion(id, versionFrom, currentVersion);
        validateEvents(id, envelopeList);

        for (final JsonEnvelope event : envelopeList) {
            eventAppender.append(event, id, ++currentVersion);
        }
        return currentVersion;
    }

    private void validateEvents(final UUID id, final List<JsonEnvelope> envelopeList) throws EventStreamException {
        if (envelopeList.stream().anyMatch(e -> e.metadata().version().isPresent())) {
            throw new EventStreamException(String.format("Failed to append to stream %s. Version must be empty.", id));
        }
    }

    private void validateVersion(final UUID id, final Optional<Long> versionFrom, final Long currentVersion) throws VersionMismatchException {
        if (versionFrom.isPresent() && !versionFrom.get().equals(currentVersion)) {
            throw new VersionMismatchException(String.format("Failed to append to stream %s. Version mismatch. Expected %d, Found %d",
                    id, versionFrom.get(), currentVersion));
        }
    }
}
