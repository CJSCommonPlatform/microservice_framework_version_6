package uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog;


import static java.lang.String.format;

import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidSequenceIdException;
import uk.gov.justice.services.jdbc.persistence.AbstractJdbcRepository;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import javax.naming.NamingException;

/**
 * JDBC based repository for event log records.
 */
public class EventLogJdbcRepository extends AbstractJdbcRepository {

    /**
     * Column Names
     */
    static final String PRIMARY_KEY_ID = "id";
    static final String COL_STREAM_ID = "stream_id";
    static final String COL_SEQUENCE_ID = "sequence_id";
    static final String COL_NAME = "name";
    static final String COL_METADATA = "metadata";
    static final String COL_PAYLOAD = "payload";

    static final long INITIAL_VERSION = 0L;

    /**
     * Statements
     */
    static final String SQL_FIND_BY_STREAM_ID = "SELECT * FROM event_log WHERE stream_id=? ORDER BY sequence_id ASC";
    static final String SQL_FIND_BY_STREAM_ID_AND_SEQUENCE_ID = "SELECT * FROM event_log WHERE stream_id=? AND sequence_id>=? ORDER BY sequence_id ASC";
    static final String SQL_FIND_LATEST_SEQUENCE_ID = "SELECT MAX(sequence_id) FROM event_log WHERE stream_id=?";
    static final String SQL_INSERT_EVENT_LOG = "INSERT INTO event_log (id, stream_id, sequence_id, name, metadata, payload ) " +
            "VALUES(?, ?, ?, ?, ?, ?)";

    private static final String READING_STREAM_EXCEPTION = "Exception while reading stream %s";
    private static final String JNDI_DS_EVENT_STORE_PATTERN = "java:/app/%s/DS.eventstore";


    /**
     * Insert the given event into th event log.
     *
     * @param eventLog the event to insert
     * @throws InvalidSequenceIdException if the version already exists or is null.
     */
    public void insert(final EventLog eventLog) throws InvalidSequenceIdException {

        if (eventLog.getSequenceId() == null) {
            throw new InvalidSequenceIdException(format("Version is null for stream %s", eventLog.getStreamId()));
        }

        try (Connection connection = getDataSource().getConnection();
             PreparedStatement ps = connection.prepareStatement(SQL_INSERT_EVENT_LOG)) {

            ps.setObject(1, eventLog.getId());
            ps.setObject(2, eventLog.getStreamId());
            ps.setLong(3, eventLog.getSequenceId());
            ps.setString(4, eventLog.getName());
            ps.setString(5, eventLog.getMetadata());
            ps.setString(6, eventLog.getPayload());

            ps.executeUpdate();
        } catch (SQLException | NamingException e) {
            throw new JdbcRepositoryException(format("Exception while storing sequence %s of stream %s",
                    eventLog.getSequenceId(), eventLog.getStreamId()), e);
        }
    }

    /**
     * Returns a Stream of {@link EventLog} for the given stream streamId.
     *
     * @param streamId streamId of the stream.
     * @return a stream of {@link EventLog}. Never returns null.
     */
    public Stream<EventLog> findByStreamIdOrderBySequenceIdAsc(final UUID streamId) {
        List<EventLog> events;

        try (final Connection connection = getDataSource().getConnection();
             final PreparedStatement ps = connection.prepareStatement(SQL_FIND_BY_STREAM_ID)) {

            ps.setObject(1, streamId);

            events = extractResults(ps);
        } catch (SQLException | NamingException e) {
            throw new JdbcRepositoryException(format(READING_STREAM_EXCEPTION, streamId), e);
        }

        return events.stream();
    }

    /**
     * Returns a Stream of {@link EventLog} for the given stream streamId starting from the given
     * version.
     *
     * @param streamId    streamId of the stream.
     * @param versionFrom the version to read from.
     * @return a stream of {@link EventLog}. Never returns null.
     */
    public Stream<EventLog> findByStreamIdFromSequenceIdOrderBySequenceIdAsc(final UUID streamId, final Long versionFrom) {
        List<EventLog> events;

        try (final Connection connection = getDataSource().getConnection();
             final PreparedStatement ps = connection.prepareStatement(SQL_FIND_BY_STREAM_ID_AND_SEQUENCE_ID)) {

            ps.setObject(1, streamId);
            ps.setLong(2, versionFrom);

            events = extractResults(ps);
        } catch (SQLException | NamingException e) {
            throw new JdbcRepositoryException(format(READING_STREAM_EXCEPTION, streamId), e);
        }

        return events.stream();
    }

    /**
     * Returns the latest sequence Id for the given stream streamId.
     *
     * @param streamId streamId of the stream.
     * @return latest sequence streamId for the stream.  Returns 0 if stream doesn't exist. Never
     * returns null.
     */
    public Long getLatestSequenceIdForStream(final UUID streamId) {
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement ps = connection.prepareStatement(SQL_FIND_LATEST_SEQUENCE_ID)) {

            ps.setObject(1, streamId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }
            }
        } catch (SQLException | NamingException e) {
            throw new JdbcRepositoryException(format(READING_STREAM_EXCEPTION, streamId), e);
        }

        return INITIAL_VERSION;
    }


    protected List<EventLog> extractResults(final PreparedStatement preparedStatement) throws SQLException {
        List<EventLog> events = new ArrayList<>();

        try (final ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                events.add(createEventLog(resultSet));
            }
        }
        return events;
    }

    private EventLog createEventLog(final ResultSet resultSet) throws SQLException {
        return new EventLog((UUID) resultSet.getObject(PRIMARY_KEY_ID),
                (UUID) resultSet.getObject(COL_STREAM_ID),
                resultSet.getLong(COL_SEQUENCE_ID),
                resultSet.getString(COL_NAME),
                resultSet.getString(COL_METADATA),
                resultSet.getString(COL_PAYLOAD));
    }

    @Override
    protected String jndiName() throws NamingException {
        return format(JNDI_DS_EVENT_STORE_PATTERN, warFileName());
    }
}
