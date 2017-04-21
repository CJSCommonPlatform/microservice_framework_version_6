package uk.gov.justice.services.fileservice.repository.json;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.json.JsonObject;


/**
 * Class to override the setting of a JsonObject on a database column. This is the standard
 * SQL version of this class which converts the JsonObject into a String and sets is as a
 * String on the database column.
 *
 * This class is used to allow us to use Hypersonic in our tests when the real code uses a
 * Postgres specific data type of JsonB
 *
 * See {@link PostgresJsonSetter}
 */
public class StringJsonSetter implements JsonSetter {

    @Override
    public void setJson(final int columnIndex, final JsonObject jsonObject, final PreparedStatement preparedStatement) throws SQLException {
        final String json = jsonObject.toString();
        preparedStatement.setString(columnIndex, json);
    }
}
