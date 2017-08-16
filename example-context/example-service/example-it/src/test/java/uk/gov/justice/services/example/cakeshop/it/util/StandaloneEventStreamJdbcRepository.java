package uk.gov.justice.services.example.cakeshop.it.util;

import static java.lang.String.format;

import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStream;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapper;

import java.sql.SQLException;
import java.util.UUID;
import java.util.stream.Stream;

import javax.sql.DataSource;

public class StandaloneEventStreamJdbcRepository extends EventStreamJdbcRepository {

    private static final String SQL_FIND_BY_STREAM_ID = "SELECT * FROM event_stream WHERE stream_id=?";

    private static final String READING_STREAM_EXCEPTION = "Exception while reading stream %s";


    private final DataSource datasource;

    public StandaloneEventStreamJdbcRepository(DataSource datasource) {
        this.datasource = datasource;
    }

    @Override
    protected DataSource getDataSource() {
        return datasource;
    }


    /**
     * Returns a Stream of {@link EventStream} for the given stream streamId.
     *
     * @param streamId streamId of the stream.
     * @return a stream of {@link EventStream}. Never returns null.
     */
    public Stream<EventStream> findByStreamIdOrderBySequenceIdAsc(final UUID streamId) {

        try {
            final PreparedStatementWrapper ps = preparedStatementWrapperOf(SQL_FIND_BY_STREAM_ID);
            ps.setObject(1, streamId);
            return streamOf(ps);
        } catch (SQLException e) {
            throw new JdbcRepositoryException(format(READING_STREAM_EXCEPTION, streamId), e);
        }

    }
}
