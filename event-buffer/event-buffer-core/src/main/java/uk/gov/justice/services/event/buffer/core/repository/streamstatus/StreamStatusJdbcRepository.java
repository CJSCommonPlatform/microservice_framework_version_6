package uk.gov.justice.services.event.buffer.core.repository.streamstatus;

import static java.lang.String.format;

import uk.gov.justice.services.jdbc.persistence.AbstractViewStoreJdbcRepository;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
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
    private static final String INSERT_ON_CONFLICT_DO_NOTHING = new StringBuilder().append(INSERT).append(" ON CONFLICT DO NOTHING").toString();
    private static final String UPDATE = "UPDATE stream_status SET version=? WHERE stream_id=?";
    private static final String POSTGRE_SQL = "PostgreSQL";

    /**
     * Insert the given StreamStatus into the stream status table.
     *
     * @param streamStatus the status of the stream to insert
     */
    public void insert(final StreamStatus streamStatus) {
        try (Connection connection = getDataSource().getConnection()) {
            executeStatement(INSERT, streamStatus, connection);
        } catch (SQLException | NamingException e) {
            throw new JdbcRepositoryException(format("Exception while storing status of the stream: %s", streamStatus), e);
        }
    }

    /**
     * Tries to insert if database is PostgresSQL and version>=9.5. Uses PostgreSQl-specific sql
     * clause. Does not fail if status for the given stream already exists
     *
     * @param streamStatus the status of the stream to insert
     */
    public void tryInsertingInPostgres95(final StreamStatus streamStatus) {
        try (Connection connection = getDataSource().getConnection()) {
            if (postgreSQL95(connection)) {
                executeStatement(INSERT_ON_CONFLICT_DO_NOTHING, streamStatus, connection);
            }
        } catch (SQLException | NamingException e) {
            throw new JdbcRepositoryException(format("Exception while storing status of the stream in PostgreSQL: %s", streamStatus), e);
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

    private boolean postgreSQL95(final Connection connection) throws SQLException {
        final DatabaseMetaData databaseMetaData = connection.getMetaData();
        final int databaseMajorVersion = databaseMetaData.getDatabaseMajorVersion();
        final int databaseMinorVersion = databaseMetaData.getDatabaseMinorVersion();
        return POSTGRE_SQL.equals(databaseMetaData.getDatabaseProductName())
                && ((databaseMajorVersion == 9 && databaseMinorVersion >= 5)
                || databaseMajorVersion > 9);
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
