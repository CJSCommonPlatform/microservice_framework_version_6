package uk.gov.justice.services.jdbc.persistence;

import static java.lang.String.format;

import java.sql.Connection;
import java.sql.SQLException;

import javax.inject.Inject;

public class JdbcConnectionProvider {

    private static final String JNDI_DS_EVENT_STORE_PATTERN = "java:/app/%s-command-handler/DS.eventstore";
    private static final String JNDI_DS_VIEW_STORE_PATTERN = "java:/DS.%s";

    @Inject
    DataSourceProvider dataSourceProvider;

    /**
     * Gets a hard JDBC connection to the Event Store database for the specified
     * context
     *
     * @param contextName The name of the context which has the event store to connect to
     * @return A JDBC connection to the context's event store
     */
    public Connection getEventStoreConnection(final String contextName) {
        return getConnection(eventStoreName(contextName));
    }

    /**
     * Gets a hard JDBC connection to the View Store database for the specified
     * context
     *
     * @param contextName The name of the context which has the view store to connect to
     * @return A JDBC connection to the context's view store
     */
    public Connection getViewStoreConnection(final String contextName) {
        return getConnection(viewStoreName(contextName));
    }

    private Connection getConnection(final String jndiName) {
        try {
            return dataSourceProvider.getDataSource(jndiName).getConnection();
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Failed to get sql Connection using JNDI name %s", jndiName), e);
        }
    }

    private String eventStoreName(final String contextName) {
        return format(JNDI_DS_EVENT_STORE_PATTERN, contextName);
    }

    private String viewStoreName(final String contextName) {
        return format(JNDI_DS_VIEW_STORE_PATTERN, contextName);
    }
}
