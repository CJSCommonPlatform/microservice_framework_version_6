package uk.gov.justice.services.event.buffer.core.repository.streamstatus;

import static java.lang.String.format;

import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryHelper;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapper;
import uk.gov.justice.services.jdbc.persistence.ViewStoreJdbcDataSourceProvider;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.sql.DataSource;

@ApplicationScoped
public class StreamStatusJdbcRepository {

    /**
     * Column Names
     */
    private static final String PRIMARY_KEY_ID = "stream_id";
    private static final String COL_VERSION = "version";
    private static final String SOURCE = "source";


    /**
     * Statements
     */
    private static final String SELECT_BY_STREAM_ID_AND_SOURCE = "SELECT stream_id, version, source FROM stream_status WHERE stream_id=? AND source=? FOR UPDATE";
    private static final String INSERT = "INSERT INTO stream_status (version, stream_id, source) VALUES (?, ?, ?)";
    private static final String INSERT_ON_CONFLICT_DO_NOTHING = new StringBuilder().append(INSERT).append(" ON CONFLICT DO NOTHING").toString();
    private static final String UPDATE = "UPDATE stream_status SET version=?,source=? WHERE stream_id=? and source in (?,'unknown')";


    @Inject
    JdbcRepositoryHelper jdbcRepositoryHelper;

    @Inject
    ViewStoreJdbcDataSourceProvider dataSourceProvider;

    DataSource dataSource;

    public StreamStatusJdbcRepository() {}

    public StreamStatusJdbcRepository(final DataSource dataSource, final JdbcRepositoryHelper jdbcRepositoryHelper) {
        this.dataSource = dataSource;
        this.jdbcRepositoryHelper = jdbcRepositoryHelper;
    }

    @PostConstruct
    private void initialiseDataSource() {
        dataSource = dataSourceProvider.getDataSource();
    }


    /**
     * Insert the given StreamStatus into the stream status table.
     *
     * @param streamStatus the status of the stream to insert
     */
    public void insert(final StreamStatus streamStatus) {
        try (final PreparedStatementWrapper ps = jdbcRepositoryHelper.preparedStatementWrapperOf(dataSource, INSERT)) {
            ps.setLong(1, streamStatus.getVersion());
            ps.setObject(2, streamStatus.getStreamId());
            ps.setString(3, streamStatus.getSource());
            ps.executeUpdate();
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
        try (final PreparedStatementWrapper ps = jdbcRepositoryHelper.preparedStatementWrapperOf(dataSource, INSERT_ON_CONFLICT_DO_NOTHING)) {
            ps.setLong(1, streamStatus.getVersion());
            ps.setObject(2, streamStatus.getStreamId());
            ps.setString(3, streamStatus.getSource());
            ps.executeUpdate();
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
        try (final PreparedStatementWrapper ps = jdbcRepositoryHelper.preparedStatementWrapperOf(dataSource, UPDATE)) {
            ps.setLong(1, streamStatus.getVersion());
            ps.setString(2, streamStatus.getSource());
            ps.setObject(3, streamStatus.getStreamId());
            ps.setString(4, streamStatus.getSource());
            ps.executeUpdate();
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
    public Optional<StreamStatus> findByStreamIdAndSource(final UUID streamId, final String source) {
        try (final PreparedStatementWrapper ps = jdbcRepositoryHelper.preparedStatementWrapperOf(dataSource, SELECT_BY_STREAM_ID_AND_SOURCE)) {
            ps.setObject(1, streamId);
            ps.setObject(2, source);
            return streamStatusFrom(ps);

        } catch (SQLException e) {
            throw new JdbcRepositoryException(format("Exception while looking up status of the stream: %s", streamId), e);
        }
    }

    private Optional<StreamStatus> streamStatusFrom(final PreparedStatementWrapper ps) throws SQLException {
        final ResultSet resultSet = ps.executeQuery();
        return resultSet.next()
                ? Optional.of(entityFrom(resultSet))
                : Optional.empty();

    }

    protected StreamStatus entityFrom(final ResultSet rs) throws SQLException {
        return new StreamStatus((UUID) rs.getObject(PRIMARY_KEY_ID), rs.getLong(COL_VERSION), rs.getString(SOURCE));
    }
}
