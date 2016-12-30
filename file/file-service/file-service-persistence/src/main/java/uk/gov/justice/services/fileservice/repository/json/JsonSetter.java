package uk.gov.justice.services.fileservice.repository.json;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.json.JsonObject;

/**
 * Interface for a utility that allows a {@link JsonObject} to be set on a database column.
 *
 * The defualt implementation is {@link PostgresJsonSetter} which takes advantage of Postgres'
 * JsonB column type. For other databases that do not support this type (i.e. Hypersonic) then
 * JsonObject will be stored as a string of json.
 *
 * There is a test class for Hypersonic: uk.gov.justice.services.fileservice.repository.json.StringJsonSetter.
 * This can be found in the test source
 */
public interface JsonSetter {

    /**
     * Sets the JsonObject at the specified column index.
     *
     * @param columnIndex the index of the json column
     * @param jsonObject the json to store
     * @param preparedStatement the current prepared statement
     * @throws SQLException if the insert fails
     */
    void setJson(
            final int columnIndex,
            final JsonObject jsonObject,
            final PreparedStatement preparedStatement) throws SQLException;
}
