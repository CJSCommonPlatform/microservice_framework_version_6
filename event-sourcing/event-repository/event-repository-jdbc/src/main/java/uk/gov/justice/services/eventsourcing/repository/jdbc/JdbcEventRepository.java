package uk.gov.justice.services.eventsourcing.repository.jdbc;

import uk.gov.justice.services.eventsourcing.repository.core.EventRepository;
import uk.gov.justice.services.eventsourcing.repository.core.exception.StoreEventRequestFailedException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.EventLog;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.EventLogConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.EventLogJdbcRepository;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidSequenceIdException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidStreamIdException;
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
    EventLogConverter eventLogConverter;

    @Inject
    EventLogJdbcRepository eventLogJdbcRepository;

    @Override
    public Stream<JsonEnvelope> getByStreamId(final UUID streamId) {
        if (streamId == null) {
            throw new InvalidStreamIdException("streamId is null.");
        }

        logger.trace("Retrieving event stream for {}", streamId);
        return eventLogJdbcRepository.findByStreamIdOrderBySequenceIdAsc(streamId)
                .map(eventLogConverter::createEnvelope);
    }

    @Override
    public Stream<JsonEnvelope> getByStreamIdAndSequenceId(final UUID streamId, final Long sequenceId) {
        if (streamId == null) {
            throw new InvalidStreamIdException("streamId is null.");
        } else if (sequenceId == null) {
            throw new JdbcRepositoryException("sequenceId is null.");
        }

        logger.trace("Retrieving event stream for {} at sequence {}", streamId, sequenceId);
        return eventLogJdbcRepository.findByStreamIdFromSequenceIdOrderBySequenceIdAsc(streamId, sequenceId)
                .map(eventLogConverter::createEnvelope);

    }

    @Override
    @Transactional
    public void store(final JsonEnvelope envelope, final UUID streamId, final Long version) throws StoreEventRequestFailedException {
        try {
            final EventLog eventLog = eventLogConverter.createEventLog(envelope, streamId, version);
            logger.trace("Storing event {} into stream {} at version {}", eventLog.getName(), streamId, version);
            eventLogJdbcRepository.insert(eventLog);
        } catch (InvalidSequenceIdException ex) {
            throw new StoreEventRequestFailedException(String.format("Could not store event for version %d of stream %s",
                    envelope.metadata().version().orElse(null), envelope.metadata().streamId().orElse(null)), ex);
        }
    }

    @Override
    public Long getCurrentSequenceIdForStream(final UUID streamId) {
        return eventLogJdbcRepository.getLatestSequenceIdForStream(streamId);
    }

}
