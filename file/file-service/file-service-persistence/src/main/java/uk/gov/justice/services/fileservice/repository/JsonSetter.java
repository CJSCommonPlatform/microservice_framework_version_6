package uk.gov.justice.services.fileservice.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.json.JsonObject;

public interface JsonSetter {

    void setJson(final PreparedStatement preparedStatement, final JsonObject jsonObject) throws SQLException;
}
