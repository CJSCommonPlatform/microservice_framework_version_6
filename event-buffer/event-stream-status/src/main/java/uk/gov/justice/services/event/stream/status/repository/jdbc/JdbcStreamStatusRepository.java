package uk.gov.justice.services.event.stream.status.repository.jdbc;

import uk.gov.justice.services.event.stream.status.repository.jdbc.exception.StreamStatusRepositoryException;
import uk.gov.justice.services.jdbc.persistence.AbstractJdbcRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import javax.naming.NamingException;

public class JdbcStreamStatusRepository extends AbstractJdbcRepository {

    /**
     * Column Names
     */
    static final String PRIMARY_KEY_ID = "stream_id";
    static final String COL_VERSION = "version";

    /**
     * Statements
     */
    static final String SQL_FIND_BY_STREAM_ID = "SELECT * FROM stream_status WHERE stream_id=?";
    static final String INSERT_INTO_STREAM_STATUS_STREAM_ID_VERSION_VALUES
            = "INSERT INTO stream_status (stream_id, version) VALUES(?, ?)";
    static final String UPDATE_STREAM_STATUS_STREAM_ID_VERSION_VALUES
            = "UPDATE stream_status SET version=? WHERE stream_id=?";

    public  JdbcStreamStatusRepository() {
        super("viewstore");
    }


    private static final String READING_STREAM_EXCEPTION = "Exception while reading stream %s";

    /**
     * Insert the given StreamStatus into the stream status table.
     *
     * @param streamStatus the event to insert
     */
    public void insert(final StreamStatus streamStatus) throws SQLException, NamingException {

        final Connection connection = getDataSource().getConnection();
        PreparedStatement ps = connection.prepareStatement(INSERT_INTO_STREAM_STATUS_STREAM_ID_VERSION_VALUES);

        ps.setObject(1, streamStatus.getStreamId());
        ps.setLong(2, streamStatus.getVersion());
        ps.executeUpdate();

    }

    /**
     * Insert the given StreamStatus into the stream status table.
     *
     * @param streamStatus the event to insert
     */
    public void update(final StreamStatus streamStatus) throws SQLException, NamingException {

        final Connection connection = getDataSource().getConnection();
        final Optional<StreamStatus> incumbent = findByStreamId(streamStatus.getStreamId());

        PreparedStatement ps = connection.prepareStatement(UPDATE_STREAM_STATUS_STREAM_ID_VERSION_VALUES);

        ps.setLong(1, streamStatus.getVersion());
        ps.setObject(2, streamStatus.getStreamId());

        ps.executeUpdate();

    }

    /**
     * Returns a Stream of {@link StreamStatus} for the given stream streamId.
     *
     * @param streamId streamId of the stream.
     * @return a {@link StreamStatus}.
     */
    public Optional<StreamStatus> findByStreamId(final UUID streamId) {

        try (final Connection connection = getDataSource().getConnection();
             final PreparedStatement ps = connection.prepareStatement(SQL_FIND_BY_STREAM_ID)) {

            ps.setObject(1, streamId);
            return extractResults(ps);

        } catch (SQLException | NamingException e) {
            throw new StreamStatusRepositoryException(String.format(READING_STREAM_EXCEPTION, streamId), e);
        }
    }

    protected Optional<StreamStatus> extractResults(final PreparedStatement preparedStatement) throws SQLException {

        try (final ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                return Optional.ofNullable(createStreamStatus(resultSet));
            }
        }
        return Optional.empty();
    }

    private StreamStatus createStreamStatus(final ResultSet resultSet) throws SQLException {
        return new StreamStatus((UUID) resultSet.getObject(PRIMARY_KEY_ID),
                (Long) resultSet.getObject(COL_VERSION));
    }

}
