package uk.gov.justice.services.eventsourcing.repository.jdbc.snapshot;


import static java.lang.String.format;

import uk.gov.justice.services.eventsourcing.common.exception.DuplicateSnapshotException;
import uk.gov.justice.services.eventsourcing.common.exception.InvalidSequenceIdException;
import uk.gov.justice.services.eventsourcing.common.snapshot.AggregateSnapshot;
import uk.gov.justice.services.eventsourcing.repository.core.SnapshotRepository;
import uk.gov.justice.services.jdbc.persistence.AbstractJdbcRepository;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import javax.naming.NamingException;

/**
 * JDBC based repository for snapshot records.
 */
public class SnapshotJdbcRepository extends AbstractJdbcRepository
        implements SnapshotRepository {

    /**
     * Column Names
     */
    static final String COL_STREAM_ID = "stream_id";
    static final String COL_SEQUENCE_ID = "sequence_id";
    static final String COL_TYPE = "type";
    static final String COL_AGGREGATE = "aggregate";

    /**
     * Statements
     */
    static final String SQL_FIND_LATEST_BY_STREAM_ID = "SELECT * FROM snapshot WHERE stream_id=? ORDER BY sequence_id DESC";
    static final String SQL_INSERT_EVENT_LOG = "INSERT INTO snapshot (stream_id, sequence_id, type, aggregate ) VALUES(?, ?, ?, ?)";

    private static final String READING_STREAM_EXCEPTION = "Exception while reading stream %s";
    private static final String JNDI_DS_EVENT_STORE_PATTERN = "java:/app/%s/DS.eventstore";


    /**
     * Insert the given aggregateSnapshot into Snapshot log.
     *
     * @param aggregateSnapshot the event to insert
     * @throws DuplicateSnapshotException if the version already exists or is null.
     */
    @Override
    public void storeSnapshot(final AggregateSnapshot aggregateSnapshot) throws DuplicateSnapshotException, InvalidSequenceIdException {

        if (aggregateSnapshot.getSequenceId() == null) {
            throw new InvalidSequenceIdException(format("Version is null for stream %s", aggregateSnapshot.getStreamId()));
        }

        try (Connection connection = getDataSource().getConnection();
             PreparedStatement ps = connection.prepareStatement(SQL_INSERT_EVENT_LOG)) {
            ps.setObject(1, aggregateSnapshot.getStreamId());
            ps.setLong(2, aggregateSnapshot.getSequenceId());
            ps.setString(3, aggregateSnapshot.getType());
            ps.setBytes(4, aggregateSnapshot.getAggregate());
            ps.executeUpdate();
        } catch (SQLException | NamingException e) {
            throw new JdbcRepositoryException(format("Exception while storing sequence %s of stream %s",
                    aggregateSnapshot.getSequenceId(), aggregateSnapshot.getStreamId()), e);
        }
    }

    /**
     * Returns a {@link AggregateSnapshot} for the given stream streamId.
     *
     * @param streamId streamId of the stream.
     * @return a {@link AggregateSnapshot}. Never returns null.
     */
    @Override
    public Optional<AggregateSnapshot> getLatestSnapshot(final UUID streamId) {

        try (final Connection connection = getDataSource().getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(SQL_FIND_LATEST_BY_STREAM_ID)) {

            preparedStatement.setObject(1, streamId);

            return extractResults(preparedStatement);

        } catch (SQLException | NamingException e) {
            throw new JdbcRepositoryException(format(READING_STREAM_EXCEPTION, streamId), e);
        }
    }

    protected Optional<AggregateSnapshot> extractResults(final PreparedStatement preparedStatement) throws SQLException {

        try (final ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                return createAggregateSnapshot(resultSet);
            }
        }
        return Optional.empty();
    }

    private Optional<AggregateSnapshot> createAggregateSnapshot(final ResultSet resultSet) throws
            SQLException {

        return Optional.of(new AggregateSnapshot(
                (UUID) resultSet.getObject(COL_STREAM_ID),
                resultSet.getLong(COL_SEQUENCE_ID),
                resultSet.getString(COL_TYPE),
                resultSet.getBytes(COL_AGGREGATE)));
    }

    @Override
    protected String jndiName() throws NamingException {
        return format(JNDI_DS_EVENT_STORE_PATTERN, warFileName());
    }


}
