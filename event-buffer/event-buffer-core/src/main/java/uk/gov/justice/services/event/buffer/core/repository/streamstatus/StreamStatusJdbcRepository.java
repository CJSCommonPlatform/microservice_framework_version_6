package uk.gov.justice.services.event.buffer.core.repository.streamstatus;

import static java.lang.String.format;

import uk.gov.justice.services.jdbc.persistence.AbstractViewStoreJdbcRepository;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import javax.naming.NamingException;

public class StreamStatusJdbcRepository extends AbstractViewStoreJdbcRepository {

    /**
     * Column Names
     */
    private static final String PRIMARY_KEY_ID = "stream_id";
    private static final String COL_VERSION = "version";

    /**
     * Statements
     */
    private static final String SELECT_BY_STREAM_ID = "SELECT * FROM stream_status WHERE stream_id=? FOR UPDATE";
    private static final String INSERT_INTO_STREAM_STATUS_STREAM_ID_VERSION_VALUES
            = "INSERT INTO stream_status (stream_id, version) VALUES(?, ?)";
    private static final String UPDATE_STREAM_STATUS_STREAM_ID_VERSION_VALUES
            = "UPDATE stream_status SET version=? WHERE stream_id=?";

    /**
     * Insert the given StreamStatus into the stream status table.
     *
     * @param streamStatus the event to insert
     */
    public void insert(final StreamStatus streamStatus) {

        try (Connection connection = getDataSource().getConnection();
             PreparedStatement ps = connection.prepareStatement(INSERT_INTO_STREAM_STATUS_STREAM_ID_VERSION_VALUES)) {

            ps.setObject(1, streamStatus.getStreamId());
            ps.setLong(2, streamStatus.getVersion());
            ps.executeUpdate();
        } catch (SQLException | NamingException e) {
            throw new JdbcRepositoryException(format("Exception while storing status of the stream: %s", streamStatus), e);
        }

    }

    /**
     * Insert the given StreamStatus into the stream status table.
     *
     * @param streamStatus the event to insert
     */
    public void update(final StreamStatus streamStatus) {
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement ps = connection.prepareStatement(UPDATE_STREAM_STATUS_STREAM_ID_VERSION_VALUES)) {

            ps.setLong(1, streamStatus.getVersion());
            ps.setObject(2, streamStatus.getStreamId());

            ps.executeUpdate();
        } catch (SQLException | NamingException e) {
            throw new JdbcRepositoryException(format("Exception while updating status of the stream: %s", streamStatus), e);
        }

    }

    /**
     * Returns a Stream of {@link StreamStatus} for the given stream streamId.
     *
     * @param streamId streamId of the stream.
     * @return a {@link StreamStatus}.
     */
    public Optional<StreamStatus> findByStreamId(final UUID streamId) {

        try (final Connection connection = getDataSource().getConnection();
             final PreparedStatement ps = connection.prepareStatement(SELECT_BY_STREAM_ID)) {

            ps.setObject(1, streamId);
            return streamStatusFrom(ps);

        } catch (SQLException | NamingException e) {
            throw new JdbcRepositoryException(format("Exception while looking up status of the stream", streamId), e);
        }
    }

    private Optional<StreamStatus> streamStatusFrom(final PreparedStatement preparedStatement) throws SQLException {

        try (final ResultSet resultSet = preparedStatement.executeQuery()) {
            return Optional.ofNullable(resultSet.next() ? new StreamStatus((UUID) resultSet.getObject(PRIMARY_KEY_ID),
                    (Long) resultSet.getObject(COL_VERSION)) : null);
        }

    }

}
