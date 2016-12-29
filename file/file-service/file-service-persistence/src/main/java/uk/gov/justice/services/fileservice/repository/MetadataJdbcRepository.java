package uk.gov.justice.services.fileservice.repository;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createReader;

import uk.gov.justice.services.fileservice.datasource.DataSourceProvider;
import uk.gov.justice.services.fileservice.repository.json.JsonSetter;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;

public class MetadataJdbcRepository {

    private static final String INSERT_SQL = "INSERT INTO metadata(metadata_id, json, file_id) values (?, ?, ?)";
    private static final String FIND_BY_FILE_ID_SQL = "SELECT metadata_id, json, file_id FROM metadata WHERE file_id = ?";

    @Inject
    JsonSetter jsonSetter;

    @Inject
    DataSourceProvider dataSourceProvider;

    public Optional<JsonObject> findByFileId(final UUID fileId) {

        ResultSet resultSet = null;
        try (final Connection connection = dataSourceProvider.getDataSource().getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_FILE_ID_SQL)) {

            preparedStatement.setObject(1, fileId);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                // TODO: change query
                final UUID metadataId = (UUID) resultSet.getObject(1);
                final String json = resultSet.getString(2);

                return of(toJsonObject(json));
            }

            return empty();

        } catch (final SQLException e) {
            throw new RuntimeException("Failed to insert into metadata table. Sql: " + INSERT_SQL, e);
        } finally {
            close(resultSet);
        }
    }

    public void update(final UUID fileId, final JsonObject metadata) {


    }

    public void insert(final UUID fileId, final JsonObject metadata) {

        final UUID metadataId = randomUUID();

        try (final Connection connection = dataSourceProvider.getDataSource().getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(INSERT_SQL)) {

            jsonSetter.setJson(preparedStatement, metadata);

            preparedStatement.setObject(1, metadataId);
            preparedStatement.setObject(3, fileId);

            preparedStatement.executeUpdate();
        } catch (final SQLException e) {
            throw new RuntimeException("Failed to insert into metadata table. Sql: " + INSERT_SQL, e);
        }
    }

    private void close(final ResultSet resultSet) {
        if (resultSet == null) {
            return;
        }

        try {
            resultSet.close();
        } catch (final SQLException ignored) {
        }
    }

    private JsonObject toJsonObject(final String json) {
        return createReader(new StringReader(json)).readObject();
    }
}
