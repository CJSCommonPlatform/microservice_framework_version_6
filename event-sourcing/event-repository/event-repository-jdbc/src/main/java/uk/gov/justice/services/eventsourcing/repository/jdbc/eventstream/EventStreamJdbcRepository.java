package uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream;

import static java.lang.String.format;

import uk.gov.justice.services.jdbc.persistence.AbstractJdbcRepository;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.naming.NamingException;

@ApplicationScoped
public class EventStreamJdbcRepository extends AbstractJdbcRepository<EventStream> {
    private static final String SQL_INSERT_EVENT_STREAM = "INSERT INTO event_stream (stream_id) values (?);";
    private static final String SQL_FIND_ALL = "SELECT * FROM event_stream ORDER BY sequence_number ASC;";

    private static final String READING_STREAM_EXCEPTION = "Exception while reading stream";

    /*
     * Pagination Statements
     */
    private static final String SQL_GET_HEAD = "SELECT * FROM event_stream t ORDER BY t.sequence_id DESC LIMIT ?";
    private static final String SQL_GET_FIRST = "SELECT * FROM event_stream t ORDER BY t.sequence_number ASC LIMIT ?";

    private static final String SQL_GET_PREVIOUS = "SELECT * FROM event_stream t WHERE t.sequence_number  >= ?  ORDER BY t.sequence_number ASC LIMIT ?";
    private static final String SQL_GET_NEXT = "SELECT * FROM event_stream t WHERE t.stream_id=? and t.sequence_number  <= ? ORDER BY t.sequence_number DESC LIMIT ?";
    private static final String SQL_RECORD_EXIST = "SELECT COUNT(*) FROM event_stream t WHERE  t.sequence_number  = ?";


    private static final String COL_STREAM_ID = "stream_id";
    private static final String COL_SEQUENCE_NUMBER = "sequence_number";

    private static final String JNDI_DS_EVENT_STORE_PATTERN = "java:/app/%s/DS.eventstore";

    public void insert(final EventStream eventStream) {
        try (final PreparedStatementWrapper ps = preparedStatementWrapperOf(SQL_INSERT_EVENT_STREAM)) {
            ps.setObject(1, eventStream.getStreamId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcRepositoryException(format("Exception while storing stream %s", eventStream.getStreamId()), e);
        }
    }

    public Stream<EventStream> findAll() {
        try {
            return streamOf(preparedStatementWrapperOf(SQL_FIND_ALL));
        } catch (SQLException e) {
            throw new JdbcRepositoryException(READING_STREAM_EXCEPTION, e);
        }
    }

    @Override
    protected String jndiName() throws NamingException {
        return format(JNDI_DS_EVENT_STORE_PATTERN, warFileName());
    }

    @Override
    protected EventStream entityFrom(final ResultSet resultSet) throws SQLException {
        return new EventStream((UUID) resultSet.getObject(COL_STREAM_ID),
                resultSet.getLong(COL_SEQUENCE_NUMBER));
    }


    public Stream<EventStream> first(final long pageSize)
            throws SQLException {
        final PreparedStatementWrapper ps;
        ps = preparedStatementWrapperOf(SQL_GET_FIRST);
        ps.setLong(1, pageSize);
        return streamOf(ps).sorted(Comparator.comparing(EventStream::getSequenceNumber).reversed());
    }

    public Stream<EventStream> next(final long position, final long pageSize)
            throws SQLException {
        final PreparedStatementWrapper ps = preparedStatementWrapperOf(SQL_GET_NEXT);
        ps.setLong(1, position);
        ps.setLong(2, pageSize);
        return streamOf(ps);
    }

    public Stream<EventStream> previous(final long position, final long pageSize)
            throws SQLException {
        final PreparedStatementWrapper ps = preparedStatementWrapperOf(SQL_GET_PREVIOUS);
        ps.setLong(1, position);
        ps.setLong(2, pageSize);
        return streamOf(ps).sorted(Comparator.comparing(EventStream::getSequenceNumber).reversed());
    }

    public Stream<EventStream> head(final long pageSize) throws SQLException {
        final PreparedStatementWrapper ps = preparedStatementWrapperOf(SQL_GET_HEAD);
        ps.setLong(1, pageSize);
        return streamOf(ps);
    }

    public boolean recordExists(long position) {
        try {
            PreparedStatementWrapper ps = preparedStatementWrapperOf(SQL_RECORD_EXIST);
            ps.setLong(1, position);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new JdbcRepositoryException(READING_STREAM_EXCEPTION, e);
        }
    }
}
