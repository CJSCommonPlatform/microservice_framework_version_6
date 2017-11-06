package uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream;

import static java.lang.String.format;

import uk.gov.justice.services.jdbc.persistence.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import javax.sql.DataSource;

import org.slf4j.Logger;

@ApplicationScoped
public class EventStreamJdbcRepository {

    private static final String SQL_INSERT_EVENT_STREAM = "INSERT INTO event_stream (stream_id) values (?);";
    private static final String SQL_FIND_ALL = "SELECT * FROM event_stream ORDER BY sequence_number ASC;";

    private static final String READING_STREAM_EXCEPTION = "Exception while reading stream";

    /*
     * Pagination Statements
     */
    private static final String SQL_GET_HEAD = "SELECT * FROM event_stream t ORDER BY t.sequence_number DESC LIMIT ?";
    private static final String SQL_GET_FIRST = "SELECT * FROM event_stream t ORDER BY t.sequence_number ASC LIMIT ?";

    private static final String SQL_GET_FORWARD = "SELECT * FROM event_stream t WHERE t.sequence_number  >= ?  ORDER BY t.sequence_number ASC LIMIT ?";
    private static final String SQL_GET_BACKWARD = "SELECT * FROM event_stream t WHERE  t.sequence_number  <= ? ORDER BY t.sequence_number DESC LIMIT ?";
    private static final String SQL_RECORD_EXIST = "SELECT COUNT(*) FROM event_stream t WHERE  t.sequence_number  = ?";


    private static final String COL_STREAM_ID = "stream_id";
    private static final String COL_SEQUENCE_NUMBER = "sequence_number";

    @Inject
    protected Logger logger;

    @Inject
    JdbcRepositoryHelper eventStreamJdbcRepositoryHelper;

    @Inject
    JdbcDataSourceProvider jdbcDataSourceProvider;

    DataSource dataSource;

    @PostConstruct
    private void initialiseDataSource() {
        dataSource = jdbcDataSourceProvider.getDataSource();
    }


    public void insert(final UUID streamId) {
        try (final PreparedStatementWrapper ps = eventStreamJdbcRepositoryHelper.preparedStatementWrapperOf(dataSource, SQL_INSERT_EVENT_STREAM)) {
            ps.setObject(1, streamId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcRepositoryException(format("Exception while storing stream %s", streamId), e);
        }
    }

    public Stream<EventStream> findAll() {
        try {
            return eventStreamJdbcRepositoryHelper.streamOf(eventStreamJdbcRepositoryHelper.preparedStatementWrapperOf(dataSource, SQL_FIND_ALL),
                                                            entityFromFunction());
        } catch (SQLException e) {
            throw new JdbcRepositoryException(READING_STREAM_EXCEPTION, e);
        }
    }


    protected Function<ResultSet, EventStream> entityFromFunction() {

        return resultSet1 -> {
            try {
                return new EventStream((UUID) resultSet1.getObject(COL_STREAM_ID),
                                              resultSet1.getLong(COL_SEQUENCE_NUMBER));
            } catch (final SQLException e) {
                throw new JdbcRepositoryException(e);
            }
        };
    }
    public Stream<EventStream> first(final long pageSize) {
        try {
            final PreparedStatementWrapper ps;
            ps = eventStreamJdbcRepositoryHelper.preparedStatementWrapperOf(dataSource, SQL_GET_FIRST);
            ps.setLong(1, pageSize);
            return reverseOrder(ps);
        } catch (final SQLException e) {
            throw new JdbcRepositoryException(READING_STREAM_EXCEPTION, e);
        }
    }

    public Stream<EventStream> forward(final long sequenceNumber, final long pageSize) {
        try {
            final PreparedStatementWrapper ps = eventStreamJdbcRepositoryHelper.preparedStatementWrapperOf(dataSource, SQL_GET_FORWARD);
            ps.setLong(1, sequenceNumber);
            ps.setLong(2, pageSize);
            return reverseOrder(ps);
        } catch (final SQLException e) {
            throw new JdbcRepositoryException(READING_STREAM_EXCEPTION, e);
        }
    }

    public Stream<EventStream> backward(final long sequenceNumber, final long pageSize) {
        try {
            final PreparedStatementWrapper ps = eventStreamJdbcRepositoryHelper.preparedStatementWrapperOf(dataSource, SQL_GET_BACKWARD);
            ps.setLong(1, sequenceNumber);
            ps.setLong(2, pageSize);
            return eventStreamJdbcRepositoryHelper.streamOf(ps, entityFromFunction());
        } catch (final SQLException e) {
            throw new JdbcRepositoryException(READING_STREAM_EXCEPTION, e);
        }
    }

    public Stream<EventStream> head(final long pageSize) {
        try {
            final PreparedStatementWrapper ps = eventStreamJdbcRepositoryHelper.preparedStatementWrapperOf(dataSource, SQL_GET_HEAD);
            ps.setLong(1, pageSize);
            return eventStreamJdbcRepositoryHelper.streamOf(ps, entityFromFunction());
        } catch (final SQLException e) {
            throw new JdbcRepositoryException(READING_STREAM_EXCEPTION, e);
        }
    }

    public boolean recordExists(final long sequenceNumber) {
        try {
            final PreparedStatementWrapper ps = eventStreamJdbcRepositoryHelper.preparedStatementWrapperOf(dataSource, SQL_RECORD_EXIST);
            ps.setLong(1, sequenceNumber);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (final SQLException e) {
            throw new JdbcRepositoryException(READING_STREAM_EXCEPTION, e);
        }
    }

    private Stream<EventStream> reverseOrder(final PreparedStatementWrapper ps) {
        try {
            return eventStreamJdbcRepositoryHelper.streamOf(ps, entityFromFunction())
                                                  .sorted(Comparator.comparing(EventStream::getSequenceNumber)
                                                                    .reversed());
        } catch (final SQLException e) {
            throw new JdbcRepositoryException(READING_STREAM_EXCEPTION, e);
        }
    }
}
