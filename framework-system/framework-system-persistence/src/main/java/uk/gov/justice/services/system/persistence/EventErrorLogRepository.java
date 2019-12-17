package uk.gov.justice.services.system.persistence;

import static java.sql.Types.BIGINT;
import static java.sql.Types.OTHER;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static javax.transaction.Transactional.TxType.REQUIRES_NEW;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.fromSqlTimestamp;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.toSqlTimestamp;

import uk.gov.justice.services.jdbc.persistence.SystemJdbcDataSourceProvider;
import uk.gov.justice.services.system.domain.EventError;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.sql.DataSource;
import javax.transaction.Transactional;

public class EventErrorLogRepository {

    private static final String INSERT_EVENT_ERROR_LOG_SQL = "INSERT INTO event_error_log " +
            "(message_id, component, event_name, event_id, stream_id, event_number, " +
            "metadata, payload, error_message, stacktrace, errored_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String FIND_ALL_EVENT_ERROR_LOG_SQL = "SELECT " +
            "message_id, component, event_name, event_id, stream_id, event_number, " +
            "metadata, payload, error_message, stacktrace, errored_at " +
            "FROM event_error_log";

    private static final String TRUNCATE_EVENT_ERROR_LOG_SQL = "TRUNCATE event_error_log CASCADE";

    @Inject
    private SystemJdbcDataSourceProvider systemJdbcDataSourceProvider;

    @Transactional(REQUIRES_NEW)
    public void save(final EventError eventError) {

        final DataSource dataSource = systemJdbcDataSourceProvider.getDataSource();

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(INSERT_EVENT_ERROR_LOG_SQL)) {

            preparedStatement.setString(1, eventError.getMessageId());
            preparedStatement.setString(2, eventError.getComponent());
            preparedStatement.setString(3, eventError.getEventName());
            preparedStatement.setObject(4, eventError.getEventId());
            final Optional<UUID> streamId = eventError.getStreamId();

            if (streamId.isPresent()) {
                preparedStatement.setObject(5, streamId.get());
            } else {
                preparedStatement.setNull(5, OTHER);
            }
            final Optional<Long> eventNumber = eventError.getEventNumber();

            if (eventNumber.isPresent()) {
                preparedStatement.setLong(6, eventNumber.get());
            } else {
                preparedStatement.setNull(6, BIGINT);
            }
            preparedStatement.setString(7, eventError.getMetadata());
            preparedStatement.setString(8, eventError.getPayload());
            preparedStatement.setString(9, eventError.getErrorMessage());
            preparedStatement.setString(10, eventError.getStacktrace());
            preparedStatement.setTimestamp(11, toSqlTimestamp(eventError.getErroredAt()));
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
                final String messageId = resultSet.getString("message_id");
                final String component = resultSet.getString("component");
                final String eventName = resultSet.getString("event_name");
                final UUID eventId = (UUID) resultSet.getObject("event_id");
                final UUID streamId = (UUID) resultSet.getObject("stream_id");
                final long eventNumber = resultSet.getLong("event_number");
                final String metadata = resultSet.getString("metadata");
                final String payload = resultSet.getString("payload");
                final String errorMessage = resultSet.getString("error_message");
                final String stacktrace = resultSet.getString("stacktrace");
                final ZonedDateTime erroredAt = fromSqlTimestamp(resultSet.getTimestamp("errored_at"));

                final Optional<Long> eventNumberOptional;
                if (eventNumber == 0) {
                    eventNumberOptional = empty();
                } else {
                    eventNumberOptional = of(eventNumber);
                }

                final EventError eventError = new EventError(
                        messageId,
                        component,
                        eventName,
                        eventId,
                        ofNullable(streamId),
                        eventNumberOptional,
                        metadata,
                        payload,
                        errorMessage,
                        stacktrace,
                        erroredAt
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
