package uk.gov.justice.services.system.persistence;

import static java.lang.String.format;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.fromSqlTimestamp;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.toSqlTimestamp;

import uk.gov.justice.services.jdbc.persistence.JdbcResultSetStreamer;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapper;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapperFactory;
import uk.gov.justice.services.jdbc.persistence.SystemJdbcDataSourceProvider;
import uk.gov.justice.services.system.domain.StoredCommand;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.slf4j.Logger;

public class StoredCommandRepository {

    private static final String SELECT_STORED_COMMAND_SQL = "SELECT envelope_id, command_json_envelope, destination, date_received FROM stored_command";
    private static final String INSERT_STORED_COMMAND_SQL = "INSERT into stored_command " +
            "(envelope_id, command_json_envelope, destination, date_received) " +
            "VALUES (?, ?, ?, ?) " +
            "ON CONFLICT DO NOTHING";
    private static final String TRUNCATE_STORED_COMMAND_SQL = "TRUNCATE stored_command";
    private static final String DELETE_STORED_COMMAND_SQL = "DELETE FROM stored_command WHERE envelope_id = ?";

    @Inject
    private SystemJdbcDataSourceProvider systemJdbcDataSourceProvider;

    @Inject
    private PreparedStatementWrapperFactory preparedStatementWrapperFactory;

    @Inject
    private JdbcResultSetStreamer jdbcResultSetStreamer;

    @Inject
    private Logger logger;

    public Stream<StoredCommand> streamStoredCommands() {

        final DataSource dataSource = systemJdbcDataSourceProvider.getDataSource();

        try {
            final PreparedStatementWrapper preparedStatementWrapper = preparedStatementWrapperFactory.preparedStatementWrapperOf(
                    dataSource,
                    SELECT_STORED_COMMAND_SQL);

            return jdbcResultSetStreamer.streamOf(preparedStatementWrapper, resultSet -> {

                try {
                    return new StoredCommand(
                            (UUID) resultSet.getObject("envelope_id"),
                            resultSet.getString("command_json_envelope"),
                            resultSet.getString("destination"),
                            fromSqlTimestamp(resultSet.getTimestamp("date_received"))
                    );

                } catch (final SQLException e) {
                    throw new SystemPersistenceException("Failed to get stored command stream", e);
                }
            });

        } catch (final SQLException e) {
            throw new SystemPersistenceException("Failed to get stored command", e);

        }
    }

    public void save(final StoredCommand storedCommand) {

        final DataSource dataSource = systemJdbcDataSourceProvider.getDataSource();

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(INSERT_STORED_COMMAND_SQL)) {

            preparedStatement.setObject(1, storedCommand.getEnvelopeId());
            preparedStatement.setString(2, storedCommand.getCommandJsonEnvelope());
            preparedStatement.setString(3, storedCommand.getDestination());
            preparedStatement.setTimestamp(4, toSqlTimestamp(storedCommand.getDateReceived()));
            final int rowsChanged = preparedStatement.executeUpdate();

            if (rowsChanged == 0) {
               logger.warn(format("Command with id '%s' not inserted into stored_command table. Command with that id already exists", storedCommand.getEnvelopeId()));
            }

        } catch (final SQLException e) {
            throw new SystemPersistenceException("Failed to insert stored command", e);
        }
    }

    public void delete(final UUID envelopeId) {

        final DataSource dataSource = systemJdbcDataSourceProvider.getDataSource();

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(DELETE_STORED_COMMAND_SQL)) {

            preparedStatement.setObject(1, envelopeId);
            preparedStatement.executeUpdate();

        } catch (final SQLException e) {
            throw new SystemPersistenceException(format("Failed to delete stored command with envelope id '%s'", envelopeId), e);
        }
    }

    public void deleteAll() {
        final DataSource dataSource = systemJdbcDataSourceProvider.getDataSource();

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(TRUNCATE_STORED_COMMAND_SQL)) {
            preparedStatement.executeUpdate();

        } catch (final SQLException e) {
            throw new SystemPersistenceException("Failed to truncate stored_command table", e);
        }
    }
}
