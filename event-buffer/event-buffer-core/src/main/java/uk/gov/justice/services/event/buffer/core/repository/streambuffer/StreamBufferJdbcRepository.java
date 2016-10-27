package uk.gov.justice.services.event.buffer.core.repository.streambuffer;

import static java.lang.String.format;

import uk.gov.justice.services.jdbc.persistence.AbstractViewStoreJdbcRepository;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.stream.Stream;


public class StreamBufferJdbcRepository extends AbstractViewStoreJdbcRepository<StreamBufferEvent> {

    private static final String INSERT = "INSERT INTO stream_buffer (stream_id, version, event) VALUES(?, ?, ?)";
    private static final String SELECT_BY_STREAM_ID = "SELECT * FROM stream_buffer WHERE stream_id=? ORDER BY version";
    private static final String DELETE_BY_STREAM_ID_VERSION = "DELETE FROM stream_buffer WHERE stream_id=? AND version=?";

    private static final String STREAM_ID = "stream_id";
    private static final String VERSION = "version";
    private static final String EVENT = "event";

    public void insert(final StreamBufferEvent bufferedEvent) {
        try (final PreparedStatementWrapper ps = preparedStatementWrapperOf(INSERT)) {
            ps.setObject(1, bufferedEvent.getStreamId());
            ps.setLong(2, bufferedEvent.getVersion());
            ps.setString(3, bufferedEvent.getEvent());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcRepositoryException(format("Exception while storing event in the buffer: %s", bufferedEvent), e);
        }
    }

    public Stream<StreamBufferEvent> streamById(final UUID id) {
        try {
            final PreparedStatementWrapper ps = preparedStatementWrapperOf(SELECT_BY_STREAM_ID);
            ps.setObject(1, id);
            return streamOf(ps);

        } catch (SQLException e) {
            throw new JdbcRepositoryException(format("Exception while returning buffered events, streamId: %s", id), e);
        }
    }

    public void remove(final StreamBufferEvent streamBufferEvent) {

        try (final PreparedStatementWrapper ps = preparedStatementWrapperOf(DELETE_BY_STREAM_ID_VERSION)) {
            ps.setObject(1, streamBufferEvent.getStreamId());
            ps.setLong(2, streamBufferEvent.getVersion());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcRepositoryException(format("Exception while removing event from the buffer: %s", streamBufferEvent), e);
        }

    }

    protected StreamBufferEvent entityFrom(final ResultSet rs) throws SQLException {
        return new StreamBufferEvent((UUID) rs.getObject(STREAM_ID), rs.getLong(VERSION), rs.getString(EVENT));
    }


}
