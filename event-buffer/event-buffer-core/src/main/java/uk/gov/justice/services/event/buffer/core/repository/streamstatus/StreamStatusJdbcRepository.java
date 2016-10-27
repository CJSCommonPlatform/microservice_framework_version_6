package uk.gov.justice.services.event.buffer.core.repository.streamstatus;

import static java.lang.String.format;

import uk.gov.justice.services.jdbc.persistence.AbstractViewStoreJdbcRepository;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class StreamStatusJdbcRepository extends AbstractViewStoreJdbcRepository<StreamStatus> {

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

    /**
     * Insert the given StreamStatus into the stream status table.
     *
     * @param streamStatus the status of the stream to insert
     */
    public void insert(final StreamStatus streamStatus) {
        try (final PreparedStatementWrapper ps = preparedStatementWrapperOf(INSERT)) {
            executeStatement(ps, streamStatus);
        } catch (SQLException e) {
            throw new JdbcRepositoryException(format("Exception while storing status of the stream: %s", streamStatus), e);
        }
    }

    /**
     * Tries to insert if database is PostgresSQL and version&gt;=9.5. Uses PostgreSQl-specific sql
     * clause. Does not fail if status for the given stream already exists
     *
     * @param streamStatus the status of the stream to insert
     */
    public void insertOrDoNothing(final StreamStatus streamStatus) {
        try (final PreparedStatementWrapper ps = preparedStatementWrapperOf(INSERT_ON_CONFLICT_DO_NOTHING)) {
            executeStatement(ps, streamStatus);
        } catch (SQLException e) {
            throw new JdbcRepositoryException(format("Exception while storing status of the stream in PostgreSQL: %s", streamStatus), e);
        }

    }

    /**
     * Insert the given StreamStatus into the stream status table.
     *
     * @param streamStatus the event to insert
     */
    public void update(final StreamStatus streamStatus) {
        try (final PreparedStatementWrapper ps = preparedStatementWrapperOf(UPDATE)) {
            executeStatement(ps, streamStatus);
        } catch (SQLException e) {
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

        try (final PreparedStatementWrapper ps = preparedStatementWrapperOf(SELECT_BY_STREAM_ID)) {
            ps.setObject(1, streamId);
            return streamStatusFrom(ps);

        } catch (SQLException e) {
            throw new JdbcRepositoryException(format("Exception while looking up status of the stream: %s", streamId), e);
        }
    }

    private void executeStatement(final PreparedStatementWrapper ps, final StreamStatus streamStatus) throws SQLException {
        ps.setLong(1, streamStatus.getVersion());
        ps.setObject(2, streamStatus.getStreamId());
        ps.executeUpdate();
    }

    private Optional<StreamStatus> streamStatusFrom(final PreparedStatementWrapper ps) throws SQLException {
        final ResultSet resultSet = ps.executeQuery();
        return resultSet.next()
                ? Optional.of(entityFrom(resultSet))
                : Optional.empty();

    }


    @Override
    protected StreamStatus entityFrom(final ResultSet rs) throws SQLException {
        return new StreamStatus((UUID) rs.getObject(PRIMARY_KEY_ID), rs.getLong(COL_VERSION));
    }
}
