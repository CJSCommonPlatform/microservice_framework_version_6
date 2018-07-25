package uk.gov.justice.services.jdbc.persistence;

import javax.sql.DataSource;

public interface JdbcDataSourceProvider {

    DataSource getDataSource(final String jndiName);
}
