package uk.gov.justice.services.eventsourcing.source.core;

import static java.lang.String.format;

import uk.gov.justice.services.core.cdi.QualifierAnnotationExtractor;
import uk.gov.justice.services.eventsourcing.source.core.annotation.EventSourceName;
import uk.gov.justice.services.jdbc.persistence.JndiDataSourceNameProvider;
import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;
import uk.gov.justice.subscription.domain.eventsource.Location;
import uk.gov.justice.subscription.registry.EventSourceRegistry;

import java.util.Optional;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.CreationException;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

@ApplicationScoped
@Alternative
@Priority(100)
public class SnapshotAwareEventSourceProducer {

    @Inject
    QualifierAnnotationExtractor qualifierAnnotationExtractor;

    @Inject
    EventSourceRegistry eventSourceRegistry;

    @Inject
    JndiDataSourceNameProvider jndiDataSourceNameProvider;

    @Inject
    SnapshotAwareEventSourceFactory snapshotAwareEventSourceFactory;

    private static final String DEFAULT_EVENT_SOURCE = "defaultEventSource";

    /**
     *
     * Backwards compatible support for Unnamed EventSource injection points
     *
     * @return {@link EventSource}
     */
    @Produces
    public EventSource eventSource() {
        return snapshotAwareEventSourceFactory.create(jndiDataSourceNameProvider.jndiName() , DEFAULT_EVENT_SOURCE);
    }

    /**
     * Support for Named EventSource injection points.  Annotate injection point with {@code
     * @EventSourceName("name")}
     *
     * @param injectionPoint the injection point for the EventSource
     * @return {@link EventSource}
     */
    @Produces
    @EventSourceName
    public EventSource eventSource(final InjectionPoint injectionPoint) {

        final String eventSourceName = qualifierAnnotationExtractor.getFrom(injectionPoint, EventSourceName.class).value();

        final Optional<EventSourceDefinition> eventSourceDomainObject = eventSourceRegistry.getEventSourceFor(eventSourceName);
        return eventSourceDomainObject
                .map(this::createEventSourceFrom)
                .orElseThrow(() -> new CreationException(format("Failed to find EventSource named '%s' in event-sources.yaml", eventSourceName)));
    }

    private EventSource createEventSourceFrom(final EventSourceDefinition eventSourceDefinition) {

        final Location location = eventSourceDefinition.getLocation();
        final Optional<String> dataSourceOptional = location.getDataSource();

        return dataSourceOptional
                .map(dataSource -> snapshotAwareEventSourceFactory.create(dataSource, eventSourceDefinition.getName()))
                .orElseThrow(() -> new CreationException(
                        format("No DataSource specified for EventSource '%s' specified in event-sources.yaml", eventSourceDefinition.getName())
                ));
    }
}
