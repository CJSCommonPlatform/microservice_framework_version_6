package uk.gov.justice.services.eventsourcing.repository.jdbc.event;


import static java.lang.String.format;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.fromSqlTimestamp;

import uk.gov.justice.services.eventsourcing.repository.jdbc.EventInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidSequenceIdException;
import uk.gov.justice.services.jdbc.persistence.AbstractJdbcRepository;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.naming.NamingException;

import org.slf4j.Logger;

/**
 * JDBC based repository for event log records.
 */
@ApplicationScoped
public class EventJdbcRepository extends AbstractJdbcRepository<Event> {

    /**
     * Column Names
     */
    static final String PRIMARY_KEY_ID = "id";
    static final String COL_STREAM_ID = "stream_id";
    static final String COL_SEQUENCE_ID = "sequence_id";
    static final String COL_NAME = "name";
    static final String COL_METADATA = "metadata";
    static final String COL_PAYLOAD = "payload";
    static final String COL_TIMESTAMP = "date_created";
    static final long INITIAL_VERSION = 0L;
    /**
     * Statements
     */
    static final String SQL_FIND_ALL = "SELECT * FROM event_log ORDER BY sequence_id ASC";
    static final String SQL_FIND_BY_STREAM_ID = "SELECT * FROM event_log WHERE stream_id=? ORDER BY sequence_id ASC";
    static final String SQL_FIND_BY_STREAM_ID_AND_SEQUENCE_ID = "SELECT * FROM event_log WHERE stream_id=? AND sequence_id>=? ORDER BY sequence_id ASC";
    static final String SQL_FIND_LATEST_SEQUENCE_ID = "SELECT MAX(sequence_id) FROM event_log WHERE stream_id=?";
    static final String SQL_DISTINCT_STREAM_ID = "SELECT DISTINCT stream_id FROM event_log";
    private static final String FAILED_TO_READ_STREAM = "Failed to read stream {}";

    /*
     * Pagination Statements
     */
    private static final String SQL_GET_HEAD = "SELECT * FROM event_log t WHERE t.stream_id=?  ORDER BY t.sequence_id DESC LIMIT ?";
    private static final String SQL_GET_FORWARD = "SELECT * FROM event_log t WHERE t.stream_id=? and t.sequence_id  >= ?  ORDER BY t.sequence_id ASC LIMIT ?";
    private static final String SQL_GET_BACKWARD = "SELECT * FROM event_log t WHERE t.stream_id=? and t.sequence_id  <= ? ORDER BY t.sequence_id DESC LIMIT ?";
    private static final String SQL_GET_FIRST = "SELECT * FROM event_log t WHERE t.stream_id=?  ORDER BY t.sequence_id ASC LIMIT ?";
    private static final String SQL_RECORD_EXIST = "SELECT COUNT(*) FROM event_log t WHERE t.stream_id=? and t.sequence_id  = ?"; //-- Previous

    private static final String READING_STREAM_ALL_EXCEPTION = "Exception while reading stream";
    private static final String READING_STREAM_EXCEPTION = "Exception while reading stream %s";
    private static final String JNDI_DS_EVENT_STORE_PATTERN = "java:/app/%s/DS.eventstore";
    @Inject
    protected Logger logger;
    @Inject
    EventInsertionStrategy eventInsertionStrategy;

    /**
     * Insert the given event into the event log.
     *
     * @param event the event to insert
     * @throws InvalidSequenceIdException if the version already exists or is null.
     */
    public void insert(final Event event) throws InvalidSequenceIdException {
        try (final PreparedStatementWrapper ps = preparedStatementWrapperOf(
                eventInsertionStrategy.insertStatement())) {
            eventInsertionStrategy.insert(ps, event);
        } catch (final SQLException e) {
            logger.error("Error persisting event to the database", e);
            throw new JdbcRepositoryException(format("Exception while storing sequence %s of stream %s",
                    event.getSequenceId(), event.getStreamId()), e);
        }
    }

    /**
     * Returns a Stream of {@link Event} for the given stream streamId.
     *
     * @param streamId streamId of the stream.
     * @return a stream of {@link Event}. Never returns null.
     */
    public Stream<Event> findByStreamIdOrderBySequenceIdAsc(final UUID streamId) {

        try {
            final PreparedStatementWrapper ps = preparedStatementWrapperOf(SQL_FIND_BY_STREAM_ID);
            ps.setObject(1, streamId);
            return streamOf(ps);
        } catch (final SQLException e) {
            logger.warn(FAILED_TO_READ_STREAM, streamId, e);
            throw new JdbcRepositoryException(format(READING_STREAM_EXCEPTION, streamId), e);
        }

    }

    /**
     * Returns a Stream of {@link Event} for the given stream streamId starting from the given
     * version.
     *
     * @param streamId    streamId of the stream.
     * @param versionFrom the version to read from.
     * @return a stream of {@link Event}. Never returns null.
     */
    public Stream<Event> findByStreamIdFromSequenceIdOrderBySequenceIdAsc(final UUID streamId,
                                                                          final Long versionFrom) {

        try {
            final PreparedStatementWrapper ps = preparedStatementWrapperOf(
                    SQL_FIND_BY_STREAM_ID_AND_SEQUENCE_ID);

            ps.setObject(1, streamId);
            ps.setLong(2, versionFrom);

            return streamOf(ps);
        } catch (final SQLException e) {
            logger.warn(FAILED_TO_READ_STREAM, streamId, e);
            throw new JdbcRepositoryException(format(READING_STREAM_EXCEPTION, streamId), e);
        }
    }

    /**
     * Returns a Stream of {@link Event}
     *
     * @return a stream of {@link Event}. Never returns null.
     */
    public Stream<Event> findAll() {
        try {
            return streamOf(preparedStatementWrapperOf(SQL_FIND_ALL));
        } catch (final SQLException e) {
            throw new JdbcRepositoryException(READING_STREAM_ALL_EXCEPTION, e);
        }
    }

    /**
     * Returns the latest sequence Id for the given stream streamId.
     *
     * @param streamId streamId of the stream.
     * @return latest sequence streamId for the stream.  Returns 0 if stream doesn't exist.
     */
    public long getLatestSequenceIdForStream(final UUID streamId) {
        try (final PreparedStatementWrapper ps = preparedStatementWrapperOf(
                SQL_FIND_LATEST_SEQUENCE_ID)) {

            ps.setObject(1, streamId);

            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong(1);
            }

        } catch (final SQLException e) {
            logger.warn(FAILED_TO_READ_STREAM, streamId, e);
            throw new JdbcRepositoryException(format(READING_STREAM_EXCEPTION, streamId), e);
        }

        return INITIAL_VERSION;
    }


    /**
     * Returns stream of event stream ids
     *
     * @return event stream ids
     */
    public Stream<UUID> getStreamIds() {
        try {
            final PreparedStatementWrapper psWrapper = preparedStatementWrapperOf(SQL_DISTINCT_STREAM_ID);
            final ResultSet resultSet = psWrapper.executeQuery();
            return streamFrom(psWrapper, resultSet);
        } catch (final SQLException e) {
            throw new JdbcRepositoryException(READING_STREAM_ALL_EXCEPTION, e);
        }

    }

    private Stream<UUID> streamFrom(final PreparedStatementWrapper psWrapper,
                                    final ResultSet resultSet) {
        return streamOf(psWrapper, resultSet, e -> {
            try {
                return (UUID) resultSet.getObject(COL_STREAM_ID);
            } catch (final SQLException e1) {
                throw handled(e1, psWrapper);
            }
        });
    }

    @Override
    protected Event entityFrom(final ResultSet resultSet) {
        try {
            return new Event((UUID) resultSet.getObject(PRIMARY_KEY_ID),
                    (UUID) resultSet.getObject(COL_STREAM_ID),
                    resultSet.getLong(COL_SEQUENCE_ID),
                    resultSet.getString(COL_NAME),
                    resultSet.getString(COL_METADATA),
                    resultSet.getString(COL_PAYLOAD),
                    fromSqlTimestamp(resultSet.getTimestamp(COL_TIMESTAMP)));
        } catch (final SQLException e) {
            throw new JdbcRepositoryException(READING_STREAM_EXCEPTION, e);
        }
    }

    @Override
    protected String jndiName() throws NamingException {
        return format(JNDI_DS_EVENT_STORE_PATTERN, warFileName());
    }

    public boolean recordExists(final UUID streamId, final long position) {
        try {
            PreparedStatementWrapper ps = preparedStatementWrapperOf(SQL_RECORD_EXIST);
            ps.setObject(1, streamId.toString());
            ps.setLong(2, position);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (final SQLException e) {
            throw new JdbcRepositoryException(READING_STREAM_EXCEPTION, e);
        }
    }

    public Stream<Event> first(final UUID streamId, final long pageSize) {
        try {
            final PreparedStatementWrapper ps;
            ps = preparedStatementWrapperOf(SQL_GET_FIRST);
            ps.setObject(1, streamId);
            ps.setLong(2, pageSize);
            return reverseOrder(ps);
        } catch (final SQLException e) {
            throw new JdbcRepositoryException(READING_STREAM_EXCEPTION, e);
        }

    }

    public Stream<Event> forward(final UUID streamId, final long position, final long pageSize) {
        try {
            final PreparedStatementWrapper ps = preparedStatementWrapperOf(SQL_GET_FORWARD);
            ps.setObject(1, streamId);
            ps.setLong(2, position);
            ps.setLong(3, pageSize);
            return reverseOrder(ps);
        } catch (final SQLException e) {
            throw new JdbcRepositoryException(READING_STREAM_EXCEPTION, e);
        }
    }

    public Stream<Event> backward(final UUID streamId, final long position, final long pageSize) {
        try {
            final PreparedStatementWrapper ps = preparedStatementWrapperOf(SQL_GET_BACKWARD);
            ps.setObject(1, streamId);
            ps.setLong(2, position);
            ps.setLong(3, pageSize);
            return streamOf(ps);
        } catch (final SQLException e) {
            throw new JdbcRepositoryException(READING_STREAM_EXCEPTION, e);
        }
    }

    public Stream<Event> head(final UUID streamId, final long pageSize) {
        try {
            final PreparedStatementWrapper ps = preparedStatementWrapperOf(SQL_GET_HEAD);
            ps.setObject(1, streamId);
            ps.setLong(2, pageSize);
            return streamOf(ps);
        } catch (final SQLException e) {
            throw new JdbcRepositoryException(READING_STREAM_EXCEPTION, e);
        }
    }

    private Stream<Event> reverseOrder(final PreparedStatementWrapper ps) {
        try {
            return streamOf(ps).sorted(Comparator.comparing(Event::getSequenceId).reversed());
        } catch (final SQLException e) {
            throw new JdbcRepositoryException(READING_STREAM_EXCEPTION, e);
        }
    }
}