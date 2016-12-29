package uk.gov.justice.services.fileservice.repository.json;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.enterprise.inject.Default;
import javax.json.JsonObject;

import org.postgresql.util.PGobject;

@Default
public class PostgresJsonSetter implements JsonSetter {

    @Override
    public void setJson(final PreparedStatement preparedStatement, final JsonObject jsonObject) throws SQLException {
        final PGobject pgObject = new PGobject();
        pgObject.setType("json");
        pgObject.setValue(jsonObject.toString());

        preparedStatement.setObject(2, pgObject);
    }
}
