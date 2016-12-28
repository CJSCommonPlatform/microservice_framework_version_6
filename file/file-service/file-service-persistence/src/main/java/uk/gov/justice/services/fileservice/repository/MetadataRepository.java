package uk.gov.justice.services.fileservice.repository;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static javax.json.Json.createReader;

import uk.gov.justice.services.jdbc.persistence.AbstractJdbcRepository;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.postgresql.util.PGobject;

public class MetadataRepository extends AbstractJdbcRepository<Metadata> {

    private static final String INSERT_SQL = "INSERT INTO metadata(metadata_id, json, file_id) values (?, ?, ?)";
    private static final String FIND_BY_FILE_ID_SQL = "SELECT metadata_id, json, file_id FROM metadata WHERE file_id = ?";

    private static final String JNDI_DS_FILE_STORE_PATTERN = "java:/app/file-service-persistence/DS.filestore";

    @Inject
    JsonSetter jsonSetter;

    @Inject
    DataSourceProvider dataSourceProvider;

    public Optional<Metadata> findByFileId(final UUID fileId) {

        ResultSet resultSet = null;
        try (final Connection connection = getDataSource().getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_FILE_ID_SQL)) {

            preparedStatement.setObject(1, fileId);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                final UUID metadataId = (UUID) resultSet.getObject(1);
                final String json = resultSet.getString(2);

                return of(new Metadata(metadataId, toJsonObject(json), fileId));
            }

            return empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert into metadata table. Sql: " + INSERT_SQL, e);
        } finally {
            close(resultSet);
        }
    }

    public void insert(final Metadata metadata) {

        try (final Connection connection = getDataSource().getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(INSERT_SQL)) {

            final JsonObject json = metadata.getJson();

            jsonSetter.setJson(preparedStatement, json);

            preparedStatement.setObject(1, metadata.getId());
            preparedStatement.setObject(3, metadata.getFileId());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert into metadata table. Sql: " + INSERT_SQL, e);
        }
    }


    @Override
    protected String jndiName() throws NamingException {
        return JNDI_DS_FILE_STORE_PATTERN;
    }

    @Override
    protected Metadata entityFrom(final ResultSet resultSet) throws SQLException {

        final UUID metadataId = (UUID) resultSet.getObject(1);
        final String json = resultSet.getString(2);
        final UUID fileId = (UUID) resultSet.getObject(3);

        return new Metadata(metadataId, toJsonObject(json), fileId);
    }

    @Override
    protected DataSource getDataSource() {
        return dataSourceProvider.getDataSource();
    }

    private void close(final ResultSet resultSet) {
        if (resultSet == null) {
            return;
        }

        try {
            resultSet.close();
        } catch (SQLException ignored) {
        }
    }

    private JsonObject toJsonObject(final String json) {
        return createReader(new StringReader(json)).readObject();
    }

    public static class Fred {
        public void setJson(final PreparedStatement preparedStatement, final JsonObject jsonObject) throws SQLException {
            final PGobject pgObject = new PGobject();
            pgObject.setType("json");
            pgObject.setValue(jsonObject.toString());

            preparedStatement.setObject(2, pgObject);
        }
    }
}
