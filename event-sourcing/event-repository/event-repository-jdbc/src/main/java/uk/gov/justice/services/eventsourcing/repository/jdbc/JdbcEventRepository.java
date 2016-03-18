package uk.gov.justice.services.eventsourcing.repository.jdbc;

import uk.gov.justice.services.eventsourcing.repository.core.EventRepository;
import uk.gov.justice.services.eventsourcing.repository.core.exception.StoreEventRequestFailedException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.EventLog;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.EventLogConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.JdbcEventLogRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.EventLogRepositoryException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidSequenceIdException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidStreamIdException;
import uk.gov.justice.services.messaging.Envelope;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * JDBC implementation of {@link EventRepository}
 */
public class JdbcEventRepository implements EventRepository {

    @Inject
    EventLogConverter eventLogConverter;

    @Inject
    JdbcEventLogRepository jdbcEventLogRepository;

    @Override
    public Stream<Envelope> getByStreamId(final UUID streamId) {
        if (streamId == null) {
            throw new InvalidStreamIdException("streamId is null.");
        }

        return jdbcEventLogRepository.findByStreamIdOrderBySequenceIdAsc(streamId)
                .map(eventLogConverter::createEnvelope);
    }

    @Override
    public Stream<Envelope> getByStreamIdAndSequenceId(final UUID streamId, final Long sequenceId) {
        if (streamId == null) {
            throw new InvalidStreamIdException("streamId is null.");
        } else if (sequenceId == null) {
            throw new EventLogRepositoryException("sequenceId is null.");
        }

        return jdbcEventLogRepository.findByStreamIdFromSequenceIdOrderBySequenceIdAsc(streamId, sequenceId)
                .map(eventLogConverter::createEnvelope);

    }

    @Override
    @Transactional
    public void store(final Envelope envelope, final UUID streamId, final Long version) throws StoreEventRequestFailedException {
        try {
            final EventLog eventLog = eventLogConverter.createEventLog(envelope, streamId, version);
            jdbcEventLogRepository.insert(eventLog);
        } catch (InvalidSequenceIdException ex) {
            throw new StoreEventRequestFailedException(String.format("Could not store event for version %d of stream %s",
                    envelope.metadata().version().orElse(null), envelope.metadata().streamId().orElse(null)), ex);
        }
    }

    @Override
    public Long getCurrentSequenceIdForStream(final UUID streamId) {
        return jdbcEventLogRepository.getLatestSequenceIdForStream(streamId);
    }

}
