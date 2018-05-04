package uk.gov.justice.services.eventsourcing.source.core;

import uk.gov.justice.services.eventsourcing.source.core.annotation.EventSourceName;
import uk.gov.justice.subscription.domain.eventsource.Location;
import uk.gov.justice.subscription.registry.EventSourceRegistry;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.naming.NamingException;
import javax.sql.DataSource;

@ApplicationScoped
public class DataSourceProducer {

    @Inject
    InitialContextProducer initialContextProducer;

    @Inject
    EventSourceNameExtractor eventSourceNameExtractor;

    @Inject
    EventSourceRegistry eventSourceRegistry;

    @Produces
    @EventSourceName
    public Optional<DataSource> dataSource(final InjectionPoint injectionPoint) {

        final String eventSourceName = eventSourceNameExtractor.getEventSourceNameFromQualifier(injectionPoint);

        final Optional<uk.gov.justice.subscription.domain.eventsource.EventSource> eventSource = eventSourceRegistry.getEventSourceFor(eventSourceName);

        if (eventSource.isPresent()) {

            final Location location = eventSource.get().getLocation();

            if (location.getDataSource().isPresent()) {
                return createDataSource(eventSourceName, location.getDataSource().get());
            } else {
                return Optional.empty();
            }
        }
        throw new DataSourceProducerException("Expected Event Source of name: " + eventSourceName + " not found, please check event-sources.yaml");
    }

    private Optional<DataSource> createDataSource(final String eventSourceName, final String dataSourceLocation) {
        try {
            final DataSource dataSource = (DataSource) initialContextProducer.getInitialContext().lookup(dataSourceLocation);
            return Optional.of(dataSource);
        } catch (final NamingException e) {
            throw new DataSourceProducerException("Data Source not found for the provided event source name: " + eventSourceName);

        }
    }
}
