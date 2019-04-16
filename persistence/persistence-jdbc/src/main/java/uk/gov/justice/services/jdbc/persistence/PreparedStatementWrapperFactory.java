package uk.gov.justice.services.jdbc.persistence;

import static uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapper.valueOf;

import java.sql.SQLException;

import javax.sql.DataSource;

public class PreparedStatementWrapperFactory {

    public PreparedStatementWrapper preparedStatementWrapperOf(final DataSource dataSource, final String query) throws SQLException {
        return valueOf(dataSource.getConnection(), query);
    }
}
