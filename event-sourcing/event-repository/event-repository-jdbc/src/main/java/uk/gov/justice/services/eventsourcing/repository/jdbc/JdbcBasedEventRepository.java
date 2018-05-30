package uk.gov.justice.services.eventsourcing.repository.jdbc;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStream;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidPositionException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidStreamIdException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.OptimisticLockingRetryException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.StoreEventRequestFailedException;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.transaction.Transactional;

import org.slf4j.Logger;

/**
 * Implementation of {@link EventRepository}
 */
public class JdbcBasedEventRepository implements EventRepository {


    private final Logger logger;
    private final EventConverter eventConverter;
    private final EventJdbcRepository eventJdbcRepository;
    private final EventStreamJdbcRepository eventStreamJdbcRepository;

    public JdbcBasedEventRepository(
            final EventConverter eventConverter,
            final EventJdbcRepository eventJdbcRepository,
            final EventStreamJdbcRepository eventStreamJdbcRepository,
            final Logger logger) {

        this.logger = logger;
        this.eventConverter = eventConverter;
        this.eventJdbcRepository = eventJdbcRepository;
        this.eventStreamJdbcRepository = eventStreamJdbcRepository;
    }

    @Override
    public Stream<JsonEnvelope> getEvents() {
        logger.trace("Retrieving all events");
        return eventJdbcRepository.findAll()
                .map(eventConverter::envelopeOf);
    }

    @Override
    public Stream<JsonEnvelope> getEventsByStreamId(final UUID streamId) {
        if (streamId == null) {
            throw new InvalidStreamIdException("streamId is null.");
        }

        logger.trace("Retrieving event stream for {}", streamId);
        return eventJdbcRepository.findByStreamIdOrderByPositionAsc(streamId)
                .map(eventConverter::envelopeOf);
    }

    @Override
    public Stream<JsonEnvelope> getEventsByStreamIdFromPosition(final UUID streamId, final Long position) {
        if (streamId == null) {
            throw new InvalidStreamIdException("streamId is null.");
        } else if (position == null) {
            throw new JdbcRepositoryException("position is null.");
        }

        logger.trace("Retrieving event stream for {} at sequence {}", streamId, position);
        return eventJdbcRepository.findByStreamIdFromPositionOrderByPositionAsc(streamId, position)
                .map(eventConverter::envelopeOf);
    }

    @Override
    @Transactional(dontRollbackOn = OptimisticLockingRetryException.class)
    public void storeEvent(final JsonEnvelope envelope) throws StoreEventRequestFailedException {
        try {
            final Event event = eventConverter.eventOf(envelope);
            logger.trace("Storing event {} into stream {} at position {}", event.getName(), event.getStreamId(), event.getSequenceId());
            eventJdbcRepository.insert(event);
        } catch (InvalidPositionException ex) {
            throw new StoreEventRequestFailedException(String.format("Could not store event for position %d of stream %s",
                    envelope.metadata().position().orElse(null), envelope.metadata().streamId().orElse(null)), ex);
        }
    }

    @Override
    public long getStreamSize(final UUID streamId) {
        return eventJdbcRepository.getStreamSize(streamId);
    }

    @Override
    public Stream<Stream<JsonEnvelope>> getStreamOfAllEventStreams() {
        final Stream<UUID> streamIds = eventJdbcRepository.getStreamIds();
        return getStreams(streamIds);
    }

    @Override
    public Stream<Stream<JsonEnvelope>> getStreamOfAllActiveEventStreams() {
        return getStreams(getAllActiveStreamIds());
    }

    @Override
    public Stream<UUID> getAllActiveStreamIds() {
        return eventStreamJdbcRepository.findActive()
                .map(EventStream::getStreamId);
    }

    private Stream<Stream<JsonEnvelope>> getStreams(final Stream<UUID> streamIds) {
        return streamIds
                .map(id -> {
                    final Stream<Event> eventStream = eventJdbcRepository.findByStreamIdOrderByPositionAsc(id);
                    streamIds.onClose(eventStream::close);
                    return eventStream.map(eventConverter::envelopeOf);
                });
    }

    @Override
    public void clearEventsForStream(final UUID id) {
        eventJdbcRepository.clear(id);
    }

    @Override
    public Stream<EventStreamMetadata> getStreams() {
        final Stream<EventStream> eventStreamStream = eventStreamJdbcRepository.findAll();
        return eventStreamStream.map(toEventStreamMetadata());
    }

    @Override
    public Stream<EventStreamMetadata> getEventStreamsFromPosition(final long position) {
        final Stream<EventStream> eventStreamStream = eventStreamJdbcRepository.findEventStreamWithPositionFrom(position);
        return eventStreamStream.map(toEventStreamMetadata());
    }

    @Override
    public void markEventStreamActive(final UUID streamId, final boolean active) {
        eventStreamJdbcRepository.markActive(streamId, active);
    }

    @Override
    public void createEventStream(final UUID streamId) {
        eventStreamJdbcRepository.insert(streamId);
    }

    @Override
    public long getStreamPosition(final UUID streamId) {
        return eventStreamJdbcRepository.getPosition(streamId);
    }

    private Function<EventStream, EventStreamMetadata> toEventStreamMetadata() {
        return e -> new DefaultEventStreamMetadata(e.getStreamId(), e.getPosition(),
                e.isActive(), e.getCreatedAt());
    }
}
