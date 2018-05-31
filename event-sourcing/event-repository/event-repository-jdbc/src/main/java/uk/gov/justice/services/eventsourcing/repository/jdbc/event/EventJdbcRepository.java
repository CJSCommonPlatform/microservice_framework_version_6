package uk.gov.justice.services.eventsourcing.repository.jdbc.event;


import static java.lang.String.format;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.fromSqlTimestamp;

import uk.gov.justice.services.eventsourcing.repository.jdbc.EventInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidPositionException;
import uk.gov.justice.services.jdbc.persistence.JdbcDataSourceProvider;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryHelper;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.slf4j.Logger;

/**
 * JDBC based repository for event log records.
 */
public class EventJdbcRepository {

    /**
     * Column Names
     */
    static final String PRIMARY_KEY_ID = "id";
    static final String COL_STREAM_ID = "stream_id";
    static final String COL_POSITION = "sequence_id";
    static final String COL_NAME = "name";
    static final String COL_METADATA = "metadata";
    static final String COL_PAYLOAD = "payload";
    static final String COL_TIMESTAMP = "date_created";



    /**
     * Statements
     */
    static final String SQL_FIND_ALL = "SELECT * FROM event_log ORDER BY sequence_number ASC";
    static final String SQL_FIND_BY_STREAM_ID = "SELECT * FROM event_log WHERE stream_id=? ORDER BY sequence_id ASC";
    static final String SQL_FIND_BY_STREAM_ID_AND_POSITION = "SELECT * FROM event_log WHERE stream_id=? AND sequence_id>=? ORDER BY sequence_id ASC";
    static final String SQL_FIND_LATEST_POSITION = "SELECT MAX(sequence_id) FROM event_log WHERE stream_id=?";
    static final String SQL_DISTINCT_STREAM_ID = "SELECT DISTINCT stream_id FROM event_log";
    static final String SQL_DELETE_STREAM = "DELETE FROM event_log t WHERE t.stream_id=?";

    /*
     * Error Messages
     */
    private static final String READING_STREAM_ALL_EXCEPTION = "Exception while reading stream";
    private static final String READING_STREAM_EXCEPTION = "Exception while reading stream %s";
    private static final String DELETING_STREAM_EXCEPTION = "Exception while deleting stream %s";
    private static final String DELETING_STREAM_EXCEPTION_DETAILS = DELETING_STREAM_EXCEPTION + ", expected %d rows to be updated but was %d";
    private static final String FAILED_TO_READ_STREAM = "Failed to read stream {}";

    private static final long NO_EXISTING_VERSION = 0L;

    private final String jndiDatasource;
    private final Logger logger;
    private final EventInsertionStrategy eventInsertionStrategy;
    private final JdbcRepositoryHelper jdbcRepositoryHelper;
    private final JdbcDataSourceProvider jdbcDataSourceProvider;

    private DataSource dataSource;

    public EventJdbcRepository(final EventInsertionStrategy eventInsertionStrategy,
                               final JdbcRepositoryHelper jdbcRepositoryHelper,
                               final JdbcDataSourceProvider jdbcDataSourceProvider,
                               final String jndiDatasource,
                               final Logger logger) {
        this.eventInsertionStrategy = eventInsertionStrategy;
        this.jdbcRepositoryHelper = jdbcRepositoryHelper;
        this.jdbcDataSourceProvider = jdbcDataSourceProvider;
        this.jndiDatasource = jndiDatasource;
        this.logger = logger;
    }

    /**
     * Insert the given event into the event log.
     *
     * @param event the event to insert
     * @throws InvalidPositionException if the version already exists or is null.
     */
    public void insert(final Event event) throws InvalidPositionException {
        try (final PreparedStatementWrapper ps = jdbcRepositoryHelper.preparedStatementWrapperOf(getDataSource(), eventInsertionStrategy.insertStatement())) {
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
    public Stream<Event> findByStreamIdOrderByPositionAsc(final UUID streamId) {
        try {
            final PreparedStatementWrapper ps = jdbcRepositoryHelper.preparedStatementWrapperOf(getDataSource(), SQL_FIND_BY_STREAM_ID);
            ps.setObject(1, streamId);

            return jdbcRepositoryHelper.streamOf(ps, entityFromFunction());
        } catch (final SQLException e) {
            logger.warn(FAILED_TO_READ_STREAM, streamId, e);
            throw new JdbcRepositoryException(format(READING_STREAM_EXCEPTION, streamId), e);
        }
    }

    /**
     * Returns a Stream of {@link Event} for the given stream streamId starting from the given
     * version.
     *
     * @param streamId streamId of the stream.
     * @param position the position to read from.
     * @return a stream of {@link Event}. Never returns null.
     */
    public Stream<Event> findByStreamIdFromPositionOrderByPositionAsc(final UUID streamId,
                                                                      final Long position) {
        try {
            final PreparedStatementWrapper ps = jdbcRepositoryHelper.preparedStatementWrapperOf(getDataSource(), SQL_FIND_BY_STREAM_ID_AND_POSITION);
            ps.setObject(1, streamId);
            ps.setLong(2, position);

            return jdbcRepositoryHelper.streamOf(ps, entityFromFunction());
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
            return jdbcRepositoryHelper
                    .streamOf(jdbcRepositoryHelper
                            .preparedStatementWrapperOf(getDataSource(), SQL_FIND_ALL), entityFromFunction());
        } catch (final SQLException e) {
            throw new JdbcRepositoryException(READING_STREAM_ALL_EXCEPTION, e);
        }
    }

    /**
     * Returns the current position for the given stream streamId.
     *
     * @param streamId streamId of the stream.
     * @return current position streamId for the stream.  Returns 0 if stream doesn't exist.
     */
    public long getStreamSize(final UUID streamId) {
        try (final PreparedStatementWrapper ps = jdbcRepositoryHelper.preparedStatementWrapperOf(getDataSource(), SQL_FIND_LATEST_POSITION)) {
            ps.setObject(1, streamId);

            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong(1);
            }

        } catch (final SQLException e) {
            logger.warn(FAILED_TO_READ_STREAM, streamId, e);
            throw new JdbcRepositoryException(format(READING_STREAM_EXCEPTION, streamId), e);
        }

        return NO_EXISTING_VERSION;
    }


    /**
     * Returns stream of event stream ids
     *
     * @return event stream ids
     */
    public Stream<UUID> getStreamIds() {
        try {
            final PreparedStatementWrapper psWrapper = jdbcRepositoryHelper.preparedStatementWrapperOf(getDataSource(), SQL_DISTINCT_STREAM_ID);
            return streamFrom(psWrapper);
        } catch (final SQLException e) {
            throw new JdbcRepositoryException(READING_STREAM_ALL_EXCEPTION, e);
        }

    }

    private DataSource getDataSource() {
        if (null == dataSource) {
            dataSource = jdbcDataSourceProvider.getDataSource(jndiDatasource);
        }

        return dataSource;
    }

    private Stream<UUID> streamFrom(final PreparedStatementWrapper psWrapper) throws SQLException {
        return jdbcRepositoryHelper.streamOf(psWrapper, e -> {
            try {
                return (UUID) e.getObject(COL_STREAM_ID);
            } catch (final SQLException e1) {
                throw jdbcRepositoryHelper.handled(e1, psWrapper);
            }
        });
    }

    protected Function<ResultSet, Event> entityFromFunction() {
        return resultSet -> {
            try {
                return new Event((UUID) resultSet.getObject(PRIMARY_KEY_ID),
                        (UUID) resultSet.getObject(COL_STREAM_ID),
                        resultSet.getLong(COL_POSITION),
                        resultSet.getString(COL_NAME),
                        resultSet.getString(COL_METADATA),
                        resultSet.getString(COL_PAYLOAD),
                        fromSqlTimestamp(resultSet.getTimestamp(COL_TIMESTAMP)));
            } catch (final SQLException e) {
                throw new JdbcRepositoryException(e);
            }
        };
    }

    public void clear(final UUID streamId) {
        final long eventCount = getStreamSize(streamId);

        try (final PreparedStatementWrapper ps = jdbcRepositoryHelper.preparedStatementWrapperOf(getDataSource(), SQL_DELETE_STREAM)) {
            ps.setObject(1, streamId);

            final int deletedRows = ps.executeUpdate();

            if (deletedRows != eventCount) {
                // Rollback, something went wrong
                throw new JdbcRepositoryException(format(DELETING_STREAM_EXCEPTION_DETAILS, streamId, eventCount, deletedRows));
            }
        } catch (final SQLException e) {
            throw new JdbcRepositoryException(format(DELETING_STREAM_EXCEPTION, streamId), e);
        }
    }
}
