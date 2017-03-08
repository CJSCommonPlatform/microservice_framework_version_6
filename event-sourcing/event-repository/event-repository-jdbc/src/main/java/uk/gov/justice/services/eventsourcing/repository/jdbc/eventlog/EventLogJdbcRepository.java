package uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog;


import static java.lang.String.format;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.fromSqlTimestamp;

import uk.gov.justice.services.eventsourcing.repository.jdbc.EventLogInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidSequenceIdException;
import uk.gov.justice.services.jdbc.persistence.AbstractJdbcRepository;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.naming.NamingException;

import org.slf4j.Logger;

/**
 * JDBC based repository for event log records.
 */
public class EventLogJdbcRepository extends AbstractJdbcRepository<EventLog> {

    //TODO @mrich 2016-09-29 - Re-enable the date_created field once liquibase scripts are configured
    @Inject
    Logger logger;

    @Inject
    EventLogInsertionStrategy eventLogInsertionStrategy;

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

    private static final String READING_STREAM_ALL_EXCEPTION = "Exception while reading stream";
    private static final String READING_STREAM_EXCEPTION = "Exception while reading stream %s";
    private static final String JNDI_DS_EVENT_STORE_PATTERN = "java:/app/%s/DS.eventstore";

    /**
     * Insert the given event into the event log.
     *
     * @param eventLog the event to insert
     * @throws InvalidSequenceIdException if the version already exists or is null.
     */
    public void insert(final EventLog eventLog) throws InvalidSequenceIdException {
        try (final PreparedStatementWrapper ps = preparedStatementWrapperOf(eventLogInsertionStrategy.insertStatement())) {
            eventLogInsertionStrategy.insert(ps, eventLog);
        } catch (SQLException e) {
            logger.error("Error persisting event to the database", e);
            throw new JdbcRepositoryException(format("Exception while storing sequence %s of stream %s",
                    eventLog.getSequenceId(), eventLog.getStreamId()), e);
        }
    }

    /**
     * Returns a Stream of {@link EventLog} for the given stream streamId.
     *
     * @param streamId streamId of the stream.
     * @return a stream of {@link EventLog}. Never returns null.
     */
    public Stream<EventLog> findByStreamIdOrderBySequenceIdAsc(final UUID streamId) {

        try {
            final PreparedStatementWrapper ps = preparedStatementWrapperOf(SQL_FIND_BY_STREAM_ID);
            ps.setObject(1, streamId);
            return streamOf(ps);
        } catch (SQLException e) {
            logger.warn(format("Failed to read stream %s", streamId), e);
            throw new JdbcRepositoryException(format(READING_STREAM_EXCEPTION, streamId), e);
        }

    }

    /**
     * Returns a Stream of {@link EventLog} for the given stream streamId starting from the given
     * version.
     *
     * @param streamId    streamId of the stream.
     * @param versionFrom the version to read from.
     * @return a stream of {@link EventLog}. Never returns null.
     */
    public Stream<EventLog> findByStreamIdFromSequenceIdOrderBySequenceIdAsc(final UUID streamId, final Long versionFrom) {

        try {
            final PreparedStatementWrapper ps = preparedStatementWrapperOf(SQL_FIND_BY_STREAM_ID_AND_SEQUENCE_ID);

            ps.setObject(1, streamId);
            ps.setLong(2, versionFrom);

            return streamOf(ps);
        } catch (SQLException e) {
            logger.warn(format("Failed to read stream %s", streamId), e);
            throw new JdbcRepositoryException(format(READING_STREAM_EXCEPTION, streamId), e);
        }
    }

    /**
     * Returns a Stream of {@link EventLog}
     *
     * @return a stream of {@link EventLog}. Never returns null.
     */
    public Stream<EventLog> findAll() {
        try {
            return streamOf(preparedStatementWrapperOf(SQL_FIND_ALL));
        } catch (SQLException e) {
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
        try (final PreparedStatementWrapper ps = preparedStatementWrapperOf(SQL_FIND_LATEST_SEQUENCE_ID)) {

            ps.setObject(1, streamId);

            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong(1);
            }

        } catch (SQLException e) {
            logger.warn(format("Failed to read stream %s", streamId), e);
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

            return streamOf(psWrapper, resultSet, e -> {
                try {
                    return (UUID) resultSet.getObject(COL_STREAM_ID);
                } catch (SQLException e1) {
                    throw handled(e1, psWrapper);
                }
            });
        } catch (SQLException e) {
            throw new JdbcRepositoryException(READING_STREAM_ALL_EXCEPTION, e);
        }

    }

    @Override
    protected EventLog entityFrom(final ResultSet resultSet) throws SQLException {
        return new EventLog((UUID) resultSet.getObject(PRIMARY_KEY_ID),
                (UUID) resultSet.getObject(COL_STREAM_ID),
                resultSet.getLong(COL_SEQUENCE_ID),
                resultSet.getString(COL_NAME),
                resultSet.getString(COL_METADATA),
                resultSet.getString(COL_PAYLOAD),
                fromSqlTimestamp(resultSet.getTimestamp(COL_TIMESTAMP)));
    }


    @Override
    protected String jndiName() throws NamingException {
        return format(JNDI_DS_EVENT_STORE_PATTERN, warFileName());
    }
}
