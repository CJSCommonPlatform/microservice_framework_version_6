package uk.gov.justice.services.fileservice.repository.json;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.json.JsonObject;

public interface JsonSetter {

    void setJson(final int columnIndex, final PreparedStatement preparedStatement, final JsonObject jsonObject) throws SQLException;
}
