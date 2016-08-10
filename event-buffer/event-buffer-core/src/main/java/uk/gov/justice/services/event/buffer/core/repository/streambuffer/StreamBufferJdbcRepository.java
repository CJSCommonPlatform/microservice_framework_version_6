package uk.gov.justice.services.event.buffer.core.repository.streambuffer;

import static java.lang.String.format;

import uk.gov.justice.services.jdbc.persistence.AbstractViewStoreJdbcRepository;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.naming.NamingException;
import javax.sql.DataSource;


public class StreamBufferJdbcRepository extends AbstractViewStoreJdbcRepository {

    private static final String INSERT_INTO_STREAM_STATUS_STREAM_ID_VERSION_VALUES = "INSERT INTO stream_buffer (stream_id, version, event) VALUES(?, ?, ?)";
    private static final String SELECT_BY_STREAM_ID = "SELECT * FROM stream_buffer WHERE stream_id=? ORDER BY version";
    private static final String DELETE_BY_STREAM_ID_VERSION = "DELETE FROM stream_buffer WHERE stream_id=? AND version=?";

    private static final String STREAM_ID = "stream_id";
    private static final String VERSION = "version";
    private static final String EVENT = "event";

    public void insert(final StreamBufferEvent bufferedEvent) {
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement ps = connection.prepareStatement(INSERT_INTO_STREAM_STATUS_STREAM_ID_VERSION_VALUES)) {

            ps.setObject(1, bufferedEvent.getStreamId());
            ps.setLong(2, bufferedEvent.getVersion());
            ps.setString(3, bufferedEvent.getEvent());
            ps.executeUpdate();
        } catch (SQLException | NamingException e) {
            throw new JdbcRepositoryException(format("Exception while storing event in the buffer: %s", bufferedEvent), e);
        }
    }

    public Stream<StreamBufferEvent> streamById(final UUID id) {

        try {
            return queryToStream(getDataSource(), SELECT_BY_STREAM_ID, id);
        } catch (SQLException | NamingException e) {
            throw new JdbcRepositoryException(format("Exception while returning buffered events, streamId: %s", id), e);
        }
    }


    public void remove(final StreamBufferEvent streamBufferEvent) {

        try (Connection connection = getDataSource().getConnection();
             PreparedStatement ps = connection.prepareStatement(DELETE_BY_STREAM_ID_VERSION)) {
            ps.setObject(1, streamBufferEvent.getStreamId());
            ps.setLong(2, streamBufferEvent.getVersion());
            ps.executeUpdate();
        } catch (SQLException | NamingException e) {
            throw new JdbcRepositoryException(format("Exception while removing event in the buffer: %s", streamBufferEvent), e);
        }

    }

    private StreamBufferEvent streamBufferEventFrom(final ResultSet rs) throws SQLException {
        return new StreamBufferEvent((UUID) rs.getObject(STREAM_ID), rs.getLong(VERSION), rs.getString(EVENT));
    }


    private Stream<StreamBufferEvent> queryToStream(final DataSource dataSource, final String query, final UUID streamId)
            throws SQLException {

        UncheckedCloseable close = null;
        try {
            Connection connection = dataSource.getConnection();
            close = UncheckedCloseable.wrap(connection);

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setObject(1, streamId);

            close = close.nest(ps);
            ResultSet resultSet = ps.executeQuery();

            return StreamSupport.stream(new Spliterators.AbstractSpliterator<StreamBufferEvent>(
                    Long.MAX_VALUE, Spliterator.ORDERED) {
                @Override
                public boolean tryAdvance(Consumer<? super StreamBufferEvent> action) {
                    try {
                        if (!resultSet.next()) {
                            return false;
                        }
                        action.accept(streamBufferEventFrom(resultSet));
                        return true;
                    } catch (SQLException ex) {
                        throw new JdbcRepositoryException("Error reading buffered events", ex);
                    }
                }
            }, false).onClose(close);
        } catch (SQLException sqlEx) {
            if (close != null)
                try {
                    close.close();
                } catch (Exception ex) {
                    sqlEx.addSuppressed(ex);
                }
            throw sqlEx;
        }
    }

}
