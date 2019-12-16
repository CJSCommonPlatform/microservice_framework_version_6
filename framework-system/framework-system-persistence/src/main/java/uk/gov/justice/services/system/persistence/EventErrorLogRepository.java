package uk.gov.justice.services.system.persistence;

import static javax.transaction.Transactional.TxType.REQUIRES_NEW;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.fromSqlTimestamp;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.toSqlTimestamp;

import uk.gov.justice.services.jdbc.persistence.SystemJdbcDataSourceProvider;
import uk.gov.justice.services.system.domain.EventError;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.sql.DataSource;
import javax.transaction.Transactional;

public class EventErrorLogRepository {

    private static final String INSERT_EVENT_ERROR_LOG_SQL = "INSERT INTO event_error_log " +
            "(event_id, event_number, component, message_id, metadata, payload, error_message, stacktrace, errored_at, comments) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String FIND_ALL_EVENT_ERROR_LOG_SQL = "SELECT " +
            "event_id, event_number, component, message_id, metadata, payload, error_message, stacktrace, errored_at, comments " +
            "FROM event_error_log";

    private static final String TRUNCATE_EVENT_ERROR_LOG_SQL = "TRUNCATE event_error_log CASCADE";

    @Inject
    private SystemJdbcDataSourceProvider systemJdbcDataSourceProvider;

    @Transactional(REQUIRES_NEW)
    public void save(final EventError eventError) {

        final DataSource dataSource = systemJdbcDataSourceProvider.getDataSource();

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(INSERT_EVENT_ERROR_LOG_SQL)) {

            preparedStatement.setObject(1, eventError.getEventId());
            preparedStatement.setLong(2, eventError.getEventNumber());
            preparedStatement.setString(3, eventError.getComponent());
            preparedStatement.setString(4, eventError.getMessageId());
            preparedStatement.setString(5, eventError.getMetadata());
            preparedStatement.setString(6, eventError.getPayload());
            preparedStatement.setString(7, eventError.getErrorMessage());
            preparedStatement.setString(8, eventError.getStacktrace());
            preparedStatement.setTimestamp(9, toSqlTimestamp(eventError.getErroredAt()));
            preparedStatement.setString(10, eventError.getComments());
            preparedStatement.executeUpdate();
        } catch (final SQLException e) {
            throw new SystemPersistenceException("Failed to insert into event_error_log", e);
        }
    }

    public List<EventError> findAll() {
        final DataSource dataSource = systemJdbcDataSourceProvider.getDataSource();


        final List<EventError> eventErrors = new ArrayList<>();
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(FIND_ALL_EVENT_ERROR_LOG_SQL);
             final ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                final UUID eventId = (UUID) resultSet.getObject("event_id");
                final long eventNumber = resultSet.getLong("event_number");
                final String component = resultSet.getString("component");
                final String messageId = resultSet.getString("message_id");
                final String metadata = resultSet.getString("metadata");
                final String payload = resultSet.getString("payload");
                final String errorMessage = resultSet.getString("error_message");
                final String stacktrace = resultSet.getString("stacktrace");
                final ZonedDateTime erroredAt = fromSqlTimestamp(resultSet.getTimestamp("errored_at"));
                final String comment = resultSet.getString("comments");

                final EventError eventError = new EventError(
                        messageId,
                        component,
                        eventId,
                        eventNumber,
                        metadata,
                        payload,
                        errorMessage,
                        stacktrace,
                        erroredAt,
                        comment
                );

                eventErrors.add(eventError);
            }

            return eventErrors;

        } catch (final SQLException e) {
            throw new SystemPersistenceException("Failed to select from event_error_log", e);
        }
    }

    public void deleteAll() {

        final DataSource dataSource = systemJdbcDataSourceProvider.getDataSource();

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(TRUNCATE_EVENT_ERROR_LOG_SQL)) {
            preparedStatement.executeUpdate();

        } catch (final SQLException e) {
            throw new SystemPersistenceException("Failed to TRUNCATE event_error_log");
        }
    }
}
