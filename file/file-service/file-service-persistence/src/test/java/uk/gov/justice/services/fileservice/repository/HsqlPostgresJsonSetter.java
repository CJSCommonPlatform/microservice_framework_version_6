package uk.gov.justice.services.fileservice.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.json.JsonObject;

public class HsqlPostgresJsonSetter implements JsonSetter {

    @Override
    public void setJson(final PreparedStatement preparedStatement, final JsonObject jsonObject) throws SQLException {
        final String json = jsonObject.toString();
        preparedStatement.setString(2, json);
    }
}
