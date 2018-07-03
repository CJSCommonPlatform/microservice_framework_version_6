package uk.gov.justice.services.event.buffer.core.repository.streambuffer;

import static java.lang.String.format;

import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryHelper;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapper;
import uk.gov.justice.services.jdbc.persistence.ViewStoreJdbcDataSourceProvider;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.sql.DataSource;

@ApplicationScoped
public class EventBufferJdbcRepository {

    private static final String INSERT = "INSERT INTO event_buffer (stream_id, position, event, source) VALUES (?, ?, ?, ?)";
    private static final String SELECT_STREAM_BUFFER_BY_STREAM_ID_AND_SOURCE = "SELECT stream_id, position, event, source FROM event_buffer WHERE stream_id=? AND source=? ORDER BY position";
    private static final String DELETE_BY_STREAM_ID_POSITION = "DELETE FROM event_buffer WHERE stream_id=? AND position=? AND source=?";

    private static final String STREAM_ID = "stream_id";
    private static final String POSITION = "position";
    private static final String EVENT = "event";
    private static final String SOURCE = "source";

    @Inject
    private JdbcRepositoryHelper jdbcRepositoryHelper;

    @Inject
    private ViewStoreJdbcDataSourceProvider dataSourceProvider;

    private DataSource dataSource;

    public EventBufferJdbcRepository() {}

    public EventBufferJdbcRepository(final DataSource dataSource, final JdbcRepositoryHelper jdbcRepositoryHelper) {
        this.dataSource = dataSource;
        this.jdbcRepositoryHelper = jdbcRepositoryHelper;
    }


    @PostConstruct
    private void initialiseDataSource() {
        dataSource = dataSourceProvider.getDataSource();
    }


    public void insert(final EventBufferEvent bufferedEvent) {
        try (final PreparedStatementWrapper ps = jdbcRepositoryHelper.preparedStatementWrapperOf(dataSource, INSERT)) {
            ps.setObject(1, bufferedEvent.getStreamId());
            ps.setLong(2, bufferedEvent.getPosition());
            ps.setString(3, bufferedEvent.getEvent());
            ps.setString(4, bufferedEvent.getSource());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcRepositoryException(format("Exception while storing event in the buffer: %s", bufferedEvent), e);
        }
    }

    public Stream<EventBufferEvent> findStreamByIdAndSource(final UUID id, final String source) {
        try {
            final PreparedStatementWrapper ps = jdbcRepositoryHelper.preparedStatementWrapperOf(dataSource, SELECT_STREAM_BUFFER_BY_STREAM_ID_AND_SOURCE);
            ps.setObject(1, id);
            ps.setString(2, source);

            return jdbcRepositoryHelper.streamOf(ps, entityFromFunction());

        } catch (SQLException e) {
            throw new JdbcRepositoryException(format("Exception while returning buffered events, streamId: %s", id), e);
        }
    }

    public void remove(final EventBufferEvent eventBufferEvent) {

        try (final PreparedStatementWrapper ps = jdbcRepositoryHelper.preparedStatementWrapperOf(dataSource, DELETE_BY_STREAM_ID_POSITION)) {
            ps.setObject(1, eventBufferEvent.getStreamId());
            ps.setLong(2, eventBufferEvent.getPosition());
            ps.setString(3, eventBufferEvent.getSource());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcRepositoryException(format("Exception while removing event from the buffer: %s", eventBufferEvent), e);
        }

    }

    private Function<ResultSet, EventBufferEvent> entityFromFunction() {
        return resultSet -> {
            try {
                return new EventBufferEvent((UUID) resultSet.getObject(STREAM_ID),
                                                    resultSet.getLong(POSITION),
                                                    resultSet.getString(EVENT),
                                                    resultSet.getString(SOURCE)
                        );
            } catch (final SQLException e) {
                throw new JdbcRepositoryException("Unexpected SQLException mapping ResultSet to StreamBufferEntity instance", e);
            }
        };
    }

}
