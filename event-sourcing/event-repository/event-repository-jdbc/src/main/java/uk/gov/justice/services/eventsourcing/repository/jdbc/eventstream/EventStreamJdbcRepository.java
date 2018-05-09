package uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream;

import static java.lang.String.format;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.fromSqlTimestamp;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.toSqlTimestamp;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidStreamIdException;
import uk.gov.justice.services.jdbc.persistence.DataSourceJndiNameProvider;
import uk.gov.justice.services.jdbc.persistence.JdbcDataSourceProvider;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryHelper;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;
import javax.sql.DataSource;

import org.slf4j.Logger;

@Vetoed
public class EventStreamJdbcRepository {

    private static final String SQL_FIND_BY_POSITION = "SELECT * FROM event_stream WHERE sequence_number>=? ORDER BY sequence_number ASC";
    private static final String SQL_FIND_POSITION_BY_STREAM = "SELECT sequence_number FROM event_stream s WHERE s.stream_id=?";
    private static final String SQL_FIND_EVENT_STREAM = "SELECT * FROM event_stream s WHERE s.stream_id=?";
    private static final String SQL_INSERT_EVENT_STREAM = "INSERT INTO event_stream (stream_id, date_created, active) values (?, ?, ?)";
    private static final String SQL_UPDATE_EVENT_STREAM_ACTIVE = "UPDATE event_stream SET active=? WHERE stream_id=?";
    private static final String SQL_DELETE_EVENT_STREAM = "DELETE FROM event_stream t WHERE t.stream_id=?";
    private static final String SQL_FIND_ALL = "SELECT * FROM event_stream ORDER BY sequence_number ASC";
    private static final String SQL_FIND_ALL_ACTIVE = "SELECT * FROM event_stream s WHERE s.active=true ORDER BY sequence_number ASC";

    private static final String READING_STREAM_EXCEPTION = "Exception while reading stream";

    private static final String COL_STREAM_ID = "stream_id";
    private static final String COL_POSITION = "sequence_number";
    private static final String COL_ACTIVE = "active";
    private static final String COL_DATE_CREATED = "date_created";
    private static final String EVENT_STREAM_EXCEPTION_MESSAGE = "Exception while deleting stream %s";

    @Inject
    protected Logger logger;

    @Inject
    JdbcRepositoryHelper eventStreamJdbcRepositoryHelper;

    @Inject
    JdbcDataSourceProvider jdbcDataSourceProvider;

    @Inject
    DataSourceJndiNameProvider dataSourceJndiNameProvider;

    @Inject
    UtcClock clock;

    DataSource dataSource;

    public void insert(final UUID streamId) {
        insert(streamId, true);
    }

    public void insert(final UUID streamId, final boolean active) {
        if (!isExistingStream(streamId)) {
            try (final PreparedStatementWrapper ps = eventStreamJdbcRepositoryHelper.preparedStatementWrapperOf(getDataSource(), SQL_INSERT_EVENT_STREAM)) {
                ps.setObject(1, streamId);
                ps.setTimestamp(2, toSqlTimestamp(clock.now()));
                ps.setBoolean(3, active);

                ps.executeUpdate();
            } catch (SQLException e) {
                throw new JdbcRepositoryException(format("Exception while storing stream %s", streamId), e);
            }
        }
    }

    public void markActive(final UUID streamId, final boolean active) {
        try (final PreparedStatementWrapper ps = eventStreamJdbcRepositoryHelper.preparedStatementWrapperOf(getDataSource(), SQL_UPDATE_EVENT_STREAM_ACTIVE)) {
            ps.setBoolean(1, active);
            ps.setObject(2, streamId);

            ps.executeUpdate();
        } catch (final SQLException e) {
            throw new JdbcRepositoryException(format("Exception while update stream %s active status to %s", streamId, active), e);
        }
    }

    public void delete(final UUID streamId) {
        try (final PreparedStatementWrapper ps = eventStreamJdbcRepositoryHelper.preparedStatementWrapperOf(getDataSource(), SQL_DELETE_EVENT_STREAM)) {
            ps.setObject(1, streamId);

            ps.executeUpdate();
        } catch (final SQLException e) {
            throw new JdbcRepositoryException(format(EVENT_STREAM_EXCEPTION_MESSAGE, streamId), e);
        }
    }

    public Stream<EventStream> findAll() {
        try {
            return eventStreamJdbcRepositoryHelper.streamOf(eventStreamJdbcRepositoryHelper.preparedStatementWrapperOf(getDataSource(), SQL_FIND_ALL),
                    entityFromFunction());
        } catch (SQLException e) {
            throw new JdbcRepositoryException(READING_STREAM_EXCEPTION, e);
        }
    }


    public Stream<EventStream> findActive() {
        try {
            return eventStreamJdbcRepositoryHelper.streamOf(eventStreamJdbcRepositoryHelper.preparedStatementWrapperOf(getDataSource(), SQL_FIND_ALL_ACTIVE),
                    entityFromFunction());
        } catch (SQLException e) {
            throw new JdbcRepositoryException(READING_STREAM_EXCEPTION, e);
        }
    }

    public Stream<EventStream> findEventStreamWithPositionFrom(final long position) {
        try {
            final PreparedStatementWrapper preparedStatementWrapper = eventStreamJdbcRepositoryHelper
                    .preparedStatementWrapperOf(getDataSource(), SQL_FIND_BY_POSITION);
            preparedStatementWrapper.setLong(1, position);
            return eventStreamJdbcRepositoryHelper.streamOf(preparedStatementWrapper, entityFromFunction());
        } catch (SQLException e) {
            throw new JdbcRepositoryException(READING_STREAM_EXCEPTION, e);
        }
    }

    private DataSource getDataSource() {
        if (null == dataSource) {
            final String jndiName = dataSourceJndiNameProvider.jndiName();
            dataSource = jdbcDataSourceProvider.getDataSource(jndiName);
        }

        return dataSource;
    }

    private boolean isExistingStream(final UUID streamId) {
        try (final PreparedStatementWrapper psquery = eventStreamJdbcRepositoryHelper.preparedStatementWrapperOf(getDataSource(), SQL_FIND_EVENT_STREAM)) {
            psquery.setObject(1, streamId);
            return psquery.executeQuery().next();
        } catch (final SQLException e) {
            throw new JdbcRepositoryException(format(EVENT_STREAM_EXCEPTION_MESSAGE, streamId), e);
        }
    }

    public long getPosition(final UUID streamId) {
        try (final PreparedStatementWrapper psquery = eventStreamJdbcRepositoryHelper.preparedStatementWrapperOf(getDataSource(), SQL_FIND_POSITION_BY_STREAM)) {
            psquery.setObject(1, streamId);
            ResultSet resultSet = psquery.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
            throw new InvalidStreamIdException("Invalid Stream Id: " + streamId.toString());
        } catch (final SQLException e) {
            throw new JdbcRepositoryException(format(EVENT_STREAM_EXCEPTION_MESSAGE, streamId), e);
        }
    }

    protected Function<ResultSet, EventStream> entityFromFunction() {
        return resultSet -> {
            try {
                return new EventStream((UUID) resultSet.getObject(COL_STREAM_ID),
                        resultSet.getLong(COL_POSITION),
                        resultSet.getBoolean(COL_ACTIVE),
                        fromSqlTimestamp(resultSet.getTimestamp(COL_DATE_CREATED)));
            } catch (final SQLException e) {
                throw new JdbcRepositoryException(e);
            }
        };
    }
}
