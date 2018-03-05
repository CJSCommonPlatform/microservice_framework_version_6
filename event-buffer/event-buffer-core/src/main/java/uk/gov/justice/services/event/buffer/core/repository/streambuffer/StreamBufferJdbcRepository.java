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
public class StreamBufferJdbcRepository {

    private static final String INSERT = "INSERT INTO stream_buffer (stream_id, version, event, source) VALUES (?, ?, ?, ?)";
    private static final String SELECT_STREAM_BUFFER_BY_STREAM_ID_AND_SOURCE = "SELECT stream_id, version, event, source FROM stream_buffer WHERE stream_id=? AND source=? ORDER BY version";
    private static final String DELETE_BY_STREAM_ID_VERSION = "DELETE FROM stream_buffer WHERE stream_id=? AND version=? AND source=?";

    private static final String STREAM_ID = "stream_id";
    private static final String VERSION = "version";
    private static final String EVENT = "event";
    private static final String SOURCE = "source";

    @Inject
    private JdbcRepositoryHelper jdbcRepositoryHelper;

    @Inject
    private ViewStoreJdbcDataSourceProvider dataSourceProvider;

    private DataSource dataSource;

    public StreamBufferJdbcRepository() {}

    public StreamBufferJdbcRepository(final DataSource dataSource, final JdbcRepositoryHelper jdbcRepositoryHelper) {
        this.dataSource = dataSource;
        this.jdbcRepositoryHelper = jdbcRepositoryHelper;
    }


    @PostConstruct
    private void initialiseDataSource() {
        dataSource = dataSourceProvider.getDataSource();
    }


    public void insert(final StreamBufferEvent bufferedEvent) {
        try (final PreparedStatementWrapper ps = jdbcRepositoryHelper.preparedStatementWrapperOf(dataSource, INSERT)) {
            ps.setObject(1, bufferedEvent.getStreamId());
            ps.setLong(2, bufferedEvent.getVersion());
            ps.setString(3, bufferedEvent.getEvent());
            ps.setString(4, bufferedEvent.getSource());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcRepositoryException(format("Exception while storing event in the buffer: %s", bufferedEvent), e);
        }
    }

    public Stream<StreamBufferEvent> findStreamByIdAndSource(final UUID id, final String source) {
        try {
            final PreparedStatementWrapper ps = jdbcRepositoryHelper.preparedStatementWrapperOf(dataSource, SELECT_STREAM_BUFFER_BY_STREAM_ID_AND_SOURCE);
            ps.setObject(1, id);
            ps.setString(2, source);

            return jdbcRepositoryHelper.streamOf(ps, entityFromFunction());

        } catch (SQLException e) {
            throw new JdbcRepositoryException(format("Exception while returning buffered events, streamId: %s", id), e);
        }
    }

    public void remove(final StreamBufferEvent streamBufferEvent) {

        try (final PreparedStatementWrapper ps = jdbcRepositoryHelper.preparedStatementWrapperOf(dataSource, DELETE_BY_STREAM_ID_VERSION)) {
            ps.setObject(1, streamBufferEvent.getStreamId());
            ps.setLong(2, streamBufferEvent.getVersion());
            ps.setString(3, streamBufferEvent.getSource());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcRepositoryException(format("Exception while removing event from the buffer: %s", streamBufferEvent), e);
        }

    }

    private Function<ResultSet, StreamBufferEvent> entityFromFunction() {
        return resultSet -> {
            try {
                return new StreamBufferEvent((UUID) resultSet.getObject(STREAM_ID),
                                                    resultSet.getLong(VERSION),
                                                    resultSet.getString(EVENT),
                                                    resultSet.getString(SOURCE)
                        );
            } catch (final SQLException e) {
                throw new JdbcRepositoryException("Unexpected SQLException mapping ResultSet to StreamBufferEntity instance", e);
            }
        };
    }

}
