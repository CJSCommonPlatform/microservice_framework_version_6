package uk.gov.justice.services.jdbc.persistence;

import static java.lang.String.format;

import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class DataSourceProvider {

    @Inject
    private InitialContextFactory initialContextFactory;

    public DataSource getDataSource(String jndiName) {
        try {
            return (DataSource) getInitialContext().lookup(jndiName);
        } catch (NamingException e) {
            throw new DataAccessException(format("Failed to get DataSource from container using JNDI name '%s'", jndiName), e);
        }
    }

    private Context getInitialContext() {
        try {
            return initialContextFactory.create();
        } catch (NamingException e) {
            throw new DataAccessException("Failed to get InitialContext from container", e);
        }
    }
}
