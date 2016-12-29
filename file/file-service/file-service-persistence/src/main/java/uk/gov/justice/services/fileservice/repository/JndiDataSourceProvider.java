package uk.gov.justice.services.fileservice.repository;

import static java.lang.String.format;

import uk.gov.justice.services.jdbc.persistence.DataAccessException;
import uk.gov.justice.services.jdbc.persistence.InitialContextFactory;

import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@Default
public class JndiDataSourceProvider implements DataSourceProvider{

    private static final String JNDI_DATASOURCE = "java:/app/fileservice/DS.fileservice";

    @Inject
    InitialContextFactory initialContextFactory;

    @Override
    public DataSource getDataSource() {

        try {
            return (DataSource) initialContextFactory.create().lookup(JNDI_DATASOURCE);
        } catch (final NamingException e) {
            throw new DataAccessException(format("Failed to get DataSource from container using JNDI name '%s'", JNDI_DATASOURCE), e);
        }
    }
}
