package uk.gov.justice.services.fileservice.repository.json;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.enterprise.inject.Default;
import javax.json.JsonObject;

import org.postgresql.util.PGobject;

/**
 * Class that allows a {@link JsonObject} to be set on a database column .
 *
 * This implementation takes advantage of Postgres' JsonB column type. For other databases
 * that do not support this type (i.e. Hypersonic) then JsonObject will be stored as a string of json.
 *
 * There is a test class for Hypersonic: uk.gov.justice.services.fileservice.repository.json.StringJsonSetter.
 * This can be found in the test source
 */
@Default
public class PostgresJsonSetter implements JsonSetter {

    /**
     * Sets the JsonObject at the specified column index. Using the Postgres specific data type
     * of JsonB
     *
     * @param columnIndex the index of the json column
     * @param jsonObject the json to store
     * @param preparedStatement the current prepared statement
     * @throws SQLException if the insert fails
     */
    @Override
    public void setJson(final int columnIndex, final JsonObject jsonObject, final PreparedStatement preparedStatement) throws SQLException {
        final PGobject pgObject = new PGobject();
        pgObject.setType("json");
        pgObject.setValue(jsonObject.toString());

        preparedStatement.setObject(columnIndex, pgObject);
    }
}
