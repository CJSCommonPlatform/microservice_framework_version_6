package uk.gov.justice.services.jmx.state.persistence;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.fromSqlTimestamp;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.toSqlTimestamp;
import static uk.gov.justice.services.jmx.api.domain.CommandState.valueOf;

import uk.gov.justice.services.jdbc.persistence.SystemJdbcDataSourceProvider;
import uk.gov.justice.services.jmx.api.domain.SystemCommandStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.sql.DataSource;

public class SystemCommandStatusRepository {

    private static final String INSERT_COMMAND_STATUS = "INSERT into system_command_status (" +
            "command_id, command_name, command_state, status_changed_at, message) " +
            "VALUES (?, ?, ?, ?, ?)";

    private static final String FIND_ALL = "SELECT " +
            "command_id, command_name, command_state, status_changed_at, message " +
            "FROM system_command_status " +
            "ORDER BY status_changed_at";

    private static final String FIND_ALL_BY_ID = "SELECT " +
            "command_id, command_name, command_state, status_changed_at, message " +
            "FROM system_command_status " +
            "WHERE command_id = ? " +
            "ORDER BY status_changed_at";

    private static final String FIND_LATEST = "SELECT " +
            "command_id, command_name, command_state, status_changed_at, message " +
            "FROM system_command_status " +
            "WHERE command_id = ? " +
            "ORDER BY status_changed_at DESC " +
            "LIMIT 1";


    @Inject
    private SystemJdbcDataSourceProvider systemJdbcDataSourceProvider;

    public void add(final SystemCommandStatus systemCommandStatus) {

        final DataSource systemDataSource = systemJdbcDataSourceProvider.getDataSource();

        try (final Connection connection = systemDataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(INSERT_COMMAND_STATUS)) {

            preparedStatement.setObject(1, systemCommandStatus.getCommandId());
            preparedStatement.setString(2, systemCommandStatus.getSystemCommandName());
            preparedStatement.setString(3, systemCommandStatus.getCommandState().name());
            preparedStatement.setTimestamp(4, toSqlTimestamp(systemCommandStatus.getStatusChangedAt()));
            preparedStatement.setString(5, systemCommandStatus.getMessage());

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new SystemCommandStatusPersistenceException("Failed to insert SystemCommandStatus", e);
        }
    }

    public List<SystemCommandStatus> findAll() {
        final DataSource systemDataSource = systemJdbcDataSourceProvider.getDataSource();

        final List<SystemCommandStatus> systemCommandStatuses = new ArrayList<>();

        try (final Connection connection = systemDataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(FIND_ALL);
             final ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                final SystemCommandStatus systemCommandStatus = toSystemCommandStatus(resultSet);
                systemCommandStatuses.add(systemCommandStatus);
            }

        } catch (SQLException e) {
            throw new SystemCommandStatusPersistenceException("Failed to find all SystemCommandStatus", e);
        }

        return systemCommandStatuses;
    }

    public List<SystemCommandStatus> findAllByCommandId(final UUID commandId) {
        final DataSource systemDataSource = systemJdbcDataSourceProvider.getDataSource();

        final List<SystemCommandStatus> systemCommandStatuses = new ArrayList<>();

        try (final Connection connection = systemDataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(FIND_ALL_BY_ID)) {

            preparedStatement.setObject(1, commandId);
            try(final ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    final SystemCommandStatus systemCommandStatus = toSystemCommandStatus(resultSet);
                    systemCommandStatuses.add(systemCommandStatus);
                }
            }

        } catch (SQLException e) {
            throw new SystemCommandStatusPersistenceException(format("Failed to find SystemCommandStatus for command id %s", commandId), e);
        }

        return systemCommandStatuses;
    }

    public Optional<SystemCommandStatus> findLatestStatus(final UUID commandId) {
        final DataSource systemDataSource = systemJdbcDataSourceProvider.getDataSource();

        try (final Connection connection = systemDataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(FIND_LATEST)) {

            preparedStatement.setObject(1, commandId);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {

                if (resultSet.next()) {

                    final SystemCommandStatus systemCommandStatus = toSystemCommandStatus(resultSet);

                    return of(systemCommandStatus);
                }

                return empty();
            }

        } catch (SQLException e) {
            throw new SystemCommandStatusPersistenceException("Failed to find latest SystemCommandStatus", e);
        }
    }

    private SystemCommandStatus toSystemCommandStatus(final ResultSet resultSet) throws SQLException {

        final UUID commandId = (UUID) resultSet.getObject("command_id");
        final String commandName = resultSet.getString("command_name");
        final String commandState = resultSet.getString("command_state");
        final ZonedDateTime statusChangedAt = fromSqlTimestamp(resultSet.getTimestamp("status_changed_at"));
        final String message = resultSet.getString("message");

        return new SystemCommandStatus(
                commandId,
                commandName,
                valueOf(commandState),
                statusChangedAt,
                message
        );
    }
}
