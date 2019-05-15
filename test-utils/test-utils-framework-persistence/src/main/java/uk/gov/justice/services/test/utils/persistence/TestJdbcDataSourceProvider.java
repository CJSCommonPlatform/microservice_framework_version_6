package uk.gov.justice.services.test.utils.persistence;

import static java.lang.String.format;
import static uk.gov.justice.services.test.utils.common.host.TestHostProvider.getHost;

import javax.sql.DataSource;

import org.postgresql.ds.PGSimpleDataSource;

public class TestJdbcDataSourceProvider {

    private static final int PORT = 5432;

    public DataSource getEventStoreDataSource(final String contextName) {

        return getDataSource(
                contextName,
                format("%seventstore", contextName));

    }

    public DataSource getViewStoreDataSource(final String contextName) {

        return getDataSource(
                contextName,
                format("%sviewstore", contextName));

    }

    private DataSource getDataSource(final String contextName, final String databaseName) {

        final PGSimpleDataSource dataSource = new PGSimpleDataSource();

        dataSource.setPortNumber(PORT);
        dataSource.setDatabaseName(databaseName);
        dataSource.setServerName(getHost());
        dataSource.setUser(contextName);
        dataSource.setPassword(contextName);

        return dataSource;
    }
}
