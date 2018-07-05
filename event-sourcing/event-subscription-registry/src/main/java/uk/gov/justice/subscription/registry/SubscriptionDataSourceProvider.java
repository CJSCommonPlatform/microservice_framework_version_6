package uk.gov.justice.subscription.registry;

import uk.gov.justice.services.jdbc.persistence.JdbcDataSourceProvider;
import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;

import javax.inject.Inject;
import javax.sql.DataSource;

public class SubscriptionDataSourceProvider {

    @Inject
    JdbcDataSourceProvider jdbcDataSourceProvider;

    @Inject
    EventSourceDefinitionRegistry eventSourceDefinitionRegistry;

    private DataSource dataSource;

    public DataSource getEventStoreDataSource() {

        final EventSourceDefinition defaultEventSourceDefinition = eventSourceDefinitionRegistry.getDefaultEventSourceDefinition();

        final String jndiDatasource = defaultEventSourceDefinition
                .getLocation()
                .getDataSource()
                .orElseThrow(() -> new RegistryException("No 'data_source' specified in 'event-sources.yaml'"));

        if (null == dataSource) {
            dataSource = jdbcDataSourceProvider.getDataSource(jndiDatasource);
        }

        return dataSource;
    }
}
