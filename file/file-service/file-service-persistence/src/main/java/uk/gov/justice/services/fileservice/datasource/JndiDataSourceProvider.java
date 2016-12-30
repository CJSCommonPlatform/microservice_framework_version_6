package uk.gov.justice.services.fileservice.datasource;

import static java.lang.String.format;

import uk.gov.justice.services.jdbc.persistence.DataAccessException;
import uk.gov.justice.services.jdbc.persistence.InitialContextFactory;

import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * {@link DataSourceProvider} for getting a DataSource from the container using JNDI.
 * This is the default implementation.
 */
@Default
public class JndiDataSourceProvider implements DataSourceProvider {

    private static final String JNDI_DATASOURCE = "java:/app/fileservice/DS.fileservice";

    @Inject
    InitialContextFactory initialContextFactory;

    /**
     * Gets the database data source using JNDI
     *
     * @return the database data source
     */
    @Override
    public DataSource getDataSource() {

        try {
            return (DataSource) initialContextFactory.create().lookup(JNDI_DATASOURCE);
        } catch (final NamingException e) {
            throw new DataAccessException(format("Failed to get DataSource from container using JNDI name '%s'", JNDI_DATASOURCE), e);
        }
    }
}
