package uk.gov.justice.services.jdbc.persistence;

import static java.lang.String.format;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@ApplicationScoped
public class JdbcDataSourceProvider {

    @Inject
    private InitialContext initialContext;

    private DataSource datasource = null;

    public synchronized DataSource getDataSource(final String jndiName) {

        if (datasource == null) {
            try {
                datasource = (DataSource) initialContext.lookup(jndiName);
            } catch (final NamingException e) {
                throw new JdbcRepositoryException(format("Failed to lookup DataSource using jndi name '%s'", jndiName), e);
            }
        }

        return datasource;
    }
}
