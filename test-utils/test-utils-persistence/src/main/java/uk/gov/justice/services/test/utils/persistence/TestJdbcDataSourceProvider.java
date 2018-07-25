package uk.gov.justice.services.test.utils.persistence;

import uk.gov.justice.services.jdbc.persistence.JdbcDataSourceProvider;

import javax.enterprise.context.ApplicationScoped;
import javax.sql.DataSource;

/**
 * Implementation of JdbcDataSourceProvider that allows the DataSource to be set, rather
 * than needing to get the DataSource using JNDI.
 *
 * For use in integration tests
 */
@ApplicationScoped
public class TestJdbcDataSourceProvider implements JdbcDataSourceProvider {

    private DataSource dataSource;

    public void setDataSource(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public DataSource getDataSource(final String jndiName) {
        return dataSource;
    }
}
