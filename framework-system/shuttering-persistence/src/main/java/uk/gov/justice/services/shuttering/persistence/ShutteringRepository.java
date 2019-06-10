package uk.gov.justice.services.shuttering.persistence;

import static java.lang.String.format;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.fromSqlTimestamp;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.toSqlTimestamp;

import uk.gov.justice.services.jdbc.persistence.JdbcResultSetStreamer;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapper;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapperFactory;
import uk.gov.justice.services.jdbc.persistence.ViewStoreJdbcDataSourceProvider;
import uk.gov.justice.services.shuttering.domain.ShutteredCommand;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.sql.DataSource;

public class ShutteringRepository {

    private static final String SELECT_SHUTTERED_COMMAND_SQL = "SELECT envelope_id, command_json_envelope, destination, date_received FROM shuttered_command_store";
    private static final String INSERT_SHUTTERED_COMMAND_SQL = "INSERT into shuttered_command_store (envelope_id, command_json_envelope, destination, date_received) VALUES (?, ?, ?, ?)";
    private static final String TRUNCATE_SHUTTERED_COMMAND_SQL = "TRUNCATE shuttered_command_store";
    private static final String DELETE_SHUTTERED_COMMAND_SQL = "DELETE FROM shuttered_command_store WHERE envelope_id = ?";

    @Inject
    private ViewStoreJdbcDataSourceProvider viewStoreJdbcDataSourceProvider;

    @Inject
    private PreparedStatementWrapperFactory preparedStatementWrapperFactory;

    @Inject
    private JdbcResultSetStreamer jdbcResultSetStreamer;

    public Stream<ShutteredCommand> streamShutteredCommands() {

        final DataSource dataSource = viewStoreJdbcDataSourceProvider.getDataSource();

        try {
            final PreparedStatementWrapper preparedStatementWrapper = preparedStatementWrapperFactory.preparedStatementWrapperOf(
                    dataSource,
                    SELECT_SHUTTERED_COMMAND_SQL);

            return jdbcResultSetStreamer.streamOf(preparedStatementWrapper, resultSet -> {

                try {
                    return new ShutteredCommand(
                            (UUID) resultSet.getObject("envelope_id"),
                            resultSet.getString("command_json_envelope"),
                            resultSet.getString("destination"),
                            fromSqlTimestamp(resultSet.getTimestamp("date_received"))
                    );

                } catch (final SQLException e) {
                    throw new ShutteringPersistenceException("Failed to get shuttered command stream", e);
                }
            });

        } catch (final SQLException e) {
            throw new ShutteringPersistenceException("Failed to get shuttered command", e);

        }
    }

    public void save(final ShutteredCommand shutteredCommand) {

        final DataSource dataSource = viewStoreJdbcDataSourceProvider.getDataSource();

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(INSERT_SHUTTERED_COMMAND_SQL)) {

            preparedStatement.setObject(1, shutteredCommand.getEnvelopeId());
            preparedStatement.setString(2, shutteredCommand.getCommandJsonEnvelope());
            preparedStatement.setString(3, shutteredCommand.getDestination());
            preparedStatement.setTimestamp(4, toSqlTimestamp(shutteredCommand.getDateReceived()));
            preparedStatement.executeUpdate();

        } catch (final SQLException e) {
            throw new ShutteringPersistenceException("Failed to insert shuttered command", e);
        }
    }

    public void delete(final UUID envelopeId) {

        final DataSource dataSource = viewStoreJdbcDataSourceProvider.getDataSource();

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(DELETE_SHUTTERED_COMMAND_SQL)) {

            preparedStatement.setObject(1, envelopeId);
            preparedStatement.executeUpdate();

        } catch (final SQLException e) {
            throw new ShutteringPersistenceException(format("Failed to delete shuttered command with envelope id '%s'", envelopeId), e);
        }
    }

    public void deleteAll() {
        final DataSource dataSource = viewStoreJdbcDataSourceProvider.getDataSource();

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(TRUNCATE_SHUTTERED_COMMAND_SQL)) {
            preparedStatement.executeUpdate();

        } catch (final SQLException e) {
            throw new ShutteringPersistenceException("Failed to truncate shuttered_command table", e);
        }
    }
}
