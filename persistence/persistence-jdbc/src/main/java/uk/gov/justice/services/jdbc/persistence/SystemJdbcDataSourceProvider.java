package uk.gov.justice.services.jdbc.persistence;

import static java.lang.String.format;

import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class SystemJdbcDataSourceProvider {

    @Inject
    private InitialContext initialContext;

    @Inject
    private SystemDataSourceNameProvider systemDataSourceNameProvider;

    private DataSource datasource = null;

    public synchronized DataSource getDataSource() {

        if (datasource == null) {
            datasource = lookupDataSource();
        }

        return datasource;
    }

    private DataSource lookupDataSource() {
        final String dataSourceName = systemDataSourceNameProvider.getDataSourceName();
        try {
            return (DataSource) initialContext.lookup(dataSourceName);
        } catch (final NamingException e) {
            throw new JdbcRepositoryException(format("Failed to lookup System DataSource using JNDI name '%s'", dataSourceName), e);
        }
    }
}

