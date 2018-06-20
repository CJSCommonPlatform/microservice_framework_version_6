package uk.gov.justice.services.eventsourcing.repository.jdbc;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStream;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidSequenceIdException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidStreamIdException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.OptimisticLockingRetryException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.StoreEventRequestFailedException;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;

/**
 * JDBC implementation of {@link EventRepository}
 */
public class JdbcEventRepository implements EventRepository {

    @Inject
    Logger logger;

    @Inject
    EventConverter eventConverter;

    @Inject
    EventJdbcRepository eventJdbcRepository;

    @Inject
    EventStreamJdbcRepository eventStreamJdbcRepository;

    @Override
    public Stream<JsonEnvelope> getByStreamId(final UUID streamId) {
        if (streamId == null) {
            throw new InvalidStreamIdException("streamId is null.");
        }

        logger.trace("Retrieving event stream for {}", streamId);
        return eventJdbcRepository.findByStreamIdOrderBySequenceIdAsc(streamId)
                .map(eventConverter::envelopeOf);
    }

    @Override
    public Stream<JsonEnvelope> getByStreamIdAndSequenceId(final UUID streamId, final Long sequenceId) {
        if (streamId == null) {
            throw new InvalidStreamIdException("streamId is null.");
        } else if (sequenceId == null) {
            throw new JdbcRepositoryException("sequenceId is null.");
        }

        logger.trace("Retrieving event stream for {} at sequence {}", streamId, sequenceId);
        return eventJdbcRepository.findByStreamIdFromSequenceIdOrderBySequenceIdAsc(streamId, sequenceId)
                .map(eventConverter::envelopeOf);
    }

    @Override
    public Stream<JsonEnvelope> getByStreamIdAndSequenceId(final UUID streamId, final Long sequenceId, final Integer pageSize) {
        if (streamId == null) {
            throw new InvalidStreamIdException("streamId is null.");
        } else if (sequenceId == null) {
            throw new JdbcRepositoryException("sequenceId is null.");
        }

        logger.trace("Retrieving event stream for {} at sequence {}", streamId, sequenceId);
        return eventJdbcRepository.findByStreamIdFromSequenceIdOrderBySequenceIdAsc(streamId, sequenceId, pageSize)
                .map(eventConverter::envelopeOf);
    }

    @Override
    public Stream<JsonEnvelope> getAll() {
        logger.trace("Retrieving all events");
        return eventJdbcRepository.findAll()
                .map(eventConverter::envelopeOf);
    }

    @Override
    @Transactional(dontRollbackOn = OptimisticLockingRetryException.class)
    public void store(final JsonEnvelope envelope) throws StoreEventRequestFailedException {
        try {
            final Event event = eventConverter.eventOf(envelope);
            logger.trace("Storing event {} into stream {} at version {}", event.getName(), event.getStreamId(), event.getSequenceId());
            eventJdbcRepository.insert(event);
        } catch (InvalidSequenceIdException ex) {
            throw new StoreEventRequestFailedException(String.format("Could not store event for version %d of stream %s",
                    envelope.metadata().version().orElse(null), envelope.metadata().streamId().orElse(null)), ex);
        }
    }

    @Override
    public long getCurrentSequenceIdForStream(final UUID streamId) {
        return eventJdbcRepository.getLatestSequenceIdForStream(streamId);
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
                    final Stream<Event> eventStream = eventJdbcRepository.findByStreamIdOrderBySequenceIdAsc(id);
                    streamIds.onClose(eventStream::close);
                    return eventStream.map(eventConverter::envelopeOf);
                });
    }

    @Override
    public void clear(final UUID id) {
        eventJdbcRepository.clear(id);
    }
}
