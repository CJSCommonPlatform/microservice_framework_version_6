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
    private static final String INSERT = "INSERT INTO stream_status (version, stream_id) VALUES(?, ?)";
    private static final String UPDATE = "UPDATE stream_status SET version=? WHERE stream_id=?";

    /**
     * Insert the given StreamStatus into the stream status table.
     *
     * @param streamStatus the event to insert
     */
    public void insert(final StreamStatus streamStatus) {
        try (Connection connection = getDataSource().getConnection()) {
            executeStatement(INSERT, streamStatus, connection);
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
        try (Connection connection = getDataSource().getConnection()) {
            executeStatement(UPDATE, streamStatus, connection);
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
            throw new JdbcRepositoryException(format("Exception while looking up status of the stream: %s", streamId), e);
        }
    }

    private void executeStatement(final String sqlStatement, final StreamStatus streamStatus, final Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(sqlStatement)) {
            ps.setLong(1, streamStatus.getVersion());
            ps.setObject(2, streamStatus.getStreamId());
            ps.executeUpdate();
        }
    }

    private Optional<StreamStatus> streamStatusFrom(final PreparedStatement preparedStatement) throws SQLException {

        try (final ResultSet resultSet = preparedStatement.executeQuery()) {
            return resultSet.next()
                    ? Optional.of(new StreamStatus((UUID) resultSet.getObject(PRIMARY_KEY_ID), resultSet.getLong(COL_VERSION)))
                    : Optional.empty();
        }

    }

}
