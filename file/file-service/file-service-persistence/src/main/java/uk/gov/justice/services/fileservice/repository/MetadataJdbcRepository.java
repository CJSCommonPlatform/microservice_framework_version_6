package uk.gov.justice.services.fileservice.repository;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createReader;

import uk.gov.justice.services.fileservice.repository.json.JsonSetter;
import uk.gov.justice.services.fileservice.repository.json.PostgresJsonSetter;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import javax.json.JsonObject;

import com.google.common.annotations.VisibleForTesting;

/**
 * Class for handling inserts/updates/selects on the 'metadata' database table. This class is not
 * transactional. Each method takes a valid database connection and it is assumed that the transaction
 * would have already been started on that connection. Any failures during insert/update will throw
 * a {@link TransactionFailedException}. In which case the current transaction should be rolled
 * back
 */
public class MetadataJdbcRepository {

    private static final String INSERT_SQL = "INSERT INTO metadata(metadata_id, json, file_id) values (?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE metadata SET json = ? WHERE file_id = ?";
    private static final String FIND_BY_FILE_ID_SQL = "SELECT json FROM metadata WHERE file_id = ?";

    private final JsonSetter jsonSetter;
    private final  Closer closer = new Closer();

    /**
     * Default constructor for CDI
     */
    @SuppressWarnings("unused")
    public MetadataJdbcRepository() {
        this(new PostgresJsonSetter());
    }

    @VisibleForTesting
    public MetadataJdbcRepository(final JsonSetter jsonSetter) {
        this.jsonSetter = jsonSetter;
    }

    /**
     * Finds the json metadata of a file, or {@code empty()} if none exists.
     *
     * @param fileId the file id of the metadata
     * @param connection a live database connection. Assumes any transtactions will have already been
     *                   started on this connection.
     * @return TransactionFailedException if the operation failed and so any current transaction should
     * be rolled back.
     */
    public Optional<JsonObject> findByFileId(final UUID fileId, final Connection connection) {

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement(FIND_BY_FILE_ID_SQL);

            preparedStatement.setObject(1, fileId);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return of(toJsonObject(resultSet.getString(1)));
            }

            return empty();

        } catch (final SQLException e) {
            throw new RuntimeException("Failed to find metadata. Sql: " + FIND_BY_FILE_ID_SQL, e);
        } finally {
            closer.close(resultSet, preparedStatement);
        }
    }

    /**
     * Updates the json metadata of a file
     *
     * @param fileId the id of the file to update
     * @param metadata the json metadata of the file
     * @param connection a live database connection. Assumes any transtactions will have already been
     *                   started on this connection.
     */
    public void update(final UUID fileId, final JsonObject metadata, final Connection connection) {

        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(UPDATE_SQL);

            jsonSetter.setJson(1, metadata, preparedStatement);
            preparedStatement.setObject(2, fileId);

            preparedStatement.executeUpdate();
        } catch (final SQLException e) {
            throw new RuntimeException("Failed to update metadata table. Sql: " + UPDATE_SQL, e);
        } finally {
            closer.close(preparedStatement);
        }
    }

    /**
     * inserts the json metadata of a file into a new row
     *
     * @param fileId the id of the file to insert
     * @param metadata the json metadata of the file
     * @param connection a live database connection. Assumes any transtactions will have already been
     *                   started on this connection.
     */
    public void insert(final UUID fileId, final JsonObject metadata, final Connection connection) {

        final UUID metadataId = randomUUID();

        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(INSERT_SQL);

            preparedStatement.setObject(1, metadataId);
            jsonSetter.setJson(2, metadata, preparedStatement);
            preparedStatement.setObject(3, fileId);

            preparedStatement.executeUpdate();
        } catch (final SQLException e) {
            throw new RuntimeException("Failed to insert into metadata table. Sql: " + INSERT_SQL, e);
        } finally {
             closer.close(preparedStatement);
        }
    }

    private JsonObject toJsonObject(final String json) {
        return createReader(new StringReader(json)).readObject();
    }
}
