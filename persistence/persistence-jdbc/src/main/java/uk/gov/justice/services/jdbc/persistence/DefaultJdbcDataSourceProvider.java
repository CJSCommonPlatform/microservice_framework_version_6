package uk.gov.justice.services.jdbc.persistence;

import static java.lang.String.format;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@Singleton
public class DefaultJdbcDataSourceProvider implements JdbcDataSourceProvider {

    @Inject
    private InitialContext initialContext;

    private final Map<String, DataSource> dataSources = new ConcurrentHashMap<>();

    public DataSource getDataSource(final String jndiName) {
        return dataSources.computeIfAbsent(jndiName, this::lookupDatasource);
    }

    private DataSource lookupDatasource(final String jndiName) {
        try {
            return (DataSource) initialContext.lookup(jndiName);
        } catch (final NamingException e) {
            throw new JdbcRepositoryException(format("Failed to lookup DataSource using jndi name '%s'", jndiName), e);
        }
    }
}
