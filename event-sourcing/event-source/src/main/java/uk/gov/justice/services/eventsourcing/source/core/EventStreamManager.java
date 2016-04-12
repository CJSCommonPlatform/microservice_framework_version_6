package uk.gov.justice.services.eventsourcing.source.core;


import uk.gov.justice.services.eventsourcing.publisher.core.EventPublisher;
import uk.gov.justice.services.eventsourcing.repository.core.EventRepository;
import uk.gov.justice.services.eventsourcing.repository.core.exception.StoreEventRequestFailedException;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.eventsourcing.source.core.exception.InvalidStreamVersionRuntimeException;
import uk.gov.justice.services.messaging.Envelope;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public Stream<Envelope> read(final UUID id) {
        return eventRepository.getByStreamId(id);
    }

    /**
     * Get the stream of events from the given version.
     *
     * @return the stream of events
     */
    public Stream<Envelope> readFrom(final UUID id, final Long version) {
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
    public void append(final UUID id, final Stream<Envelope> events) throws EventStreamException {
        appendTo(id, getCurrentVersion(id), validEnvelopesFrom(id, events));
    }

    /**
     * Store a stream of events after the given version.
     *
     * @param events  the stream of events to store
     * @param version the version to append from
     * @throws EventStreamException if an event could not be appended
     */
    @Transactional
    public void appendAfter(final UUID id, final Stream<Envelope> events, final Long version) throws EventStreamException {
        appendTo(id, validCurrentVersionFrom(id, version), validEnvelopesFrom(id, events));
    }

    /**
     * Get the current (current maximum) sequence id (version number) for a stream
     *
     * @return the latest sequence id for the provided steam. 0 when stream is empty.
     */
    public Long getCurrentVersion(final UUID id) {
        return eventRepository.getCurrentSequenceIdForStream(id);
    }

    private Long validCurrentVersionFrom(final UUID id, final Long version) throws EventStreamException {
        final Long currentVersion = getCurrentVersion(id);

        if (version == null) {
            throw new EventStreamException(String.format("Failed to append to stream %s. Version must not be null.", id));
        }

        if (!version.equals(currentVersion)) {
            throw new EventStreamException(String.format("Failed to append to stream %s. Version mismatch. Expected %d, Found %d",
                    id, version, eventRepository.getCurrentSequenceIdForStream(id)));
        }

        return currentVersion;
    }

    private List<Envelope> validEnvelopesFrom(final UUID id, final Stream<Envelope> events) throws EventStreamException {
        final List<Envelope> envelopeList = events.collect(Collectors.toList());

        if (envelopeList.stream().anyMatch(e -> e.metadata().version().isPresent())) {
            throw new EventStreamException(String.format("Failed to append to stream %s. Version must be empty.", id));
        }

        return envelopeList;
    }

    private void appendTo(final UUID id, final Long currentVersion, final List<Envelope> envelopeList) throws EventStreamException {
        Long version = currentVersion;
        for (final Envelope event : envelopeList) {
            try {
                eventRepository.store(event, id, ++version);
                eventPublisher.publish(event);
            } catch (StoreEventRequestFailedException e) {
                throw new EventStreamException(String.format("Failed to append event to Event Store %s", event.metadata().id()), e);
            }
        }
    }

}
