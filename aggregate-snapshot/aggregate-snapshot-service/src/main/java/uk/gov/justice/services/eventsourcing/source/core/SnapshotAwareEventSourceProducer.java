package uk.gov.justice.services.eventsourcing.source.core;

import static uk.gov.justice.services.eventsourcing.source.core.annotation.EventSourceName.DEFAULT_EVENT_SOURCE_NAME;

import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepositoryFactory;
import uk.gov.justice.services.eventsourcing.source.core.annotation.EventSourceName;
import uk.gov.justice.services.eventsourcing.source.core.snapshot.SnapshotService;
import uk.gov.justice.services.jdbc.persistence.DataSourceJndiNameProvider;
import uk.gov.justice.subscription.registry.EventSourceRegistry;

import java.util.Optional;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

@ApplicationScoped
@Alternative
@Priority(100)
public class SnapshotAwareEventSourceProducer {

    @Inject
    EventSourceNameExtractor eventSourceNameExtractor;

    @Inject
    EventSourceRegistry eventSourceRegistry;

    @Inject
    EventStreamManagerFactory eventStreamManagerFactory;

    @Inject
    EventRepositoryFactory eventRepositoryFactory;

    @Inject
    EventJdbcRepositoryFactory eventJdbcRepositoryFactory;

    @Inject
    EventStreamJdbcRepositoryFactory eventStreamJdbcRepositoryFactory;

    @Inject
    DataSourceJndiNameProvider dataSourceJndiNameProvider;

    @Inject
    SnapshotService snapshotService;

    /**
     * Backwards compatible support for Unnamed EventSource injection points
     *
     * @return {@link EventSource}
     */

    @Produces
    public EventSource eventSource() {

        final String jndiDatasource = dataSourceJndiNameProvider.jndiName();
        final EventJdbcRepository eventJdbcRepository = eventJdbcRepositoryFactory.eventJdbcRepository(jndiDatasource);
        final EventStreamJdbcRepository eventStreamJdbcRepository = eventStreamJdbcRepositoryFactory.eventStreamJdbcRepository(jndiDatasource);

        final EventRepository eventRepository = eventRepositoryFactory.eventRepository(
                eventJdbcRepository,
                eventStreamJdbcRepository);

        final EventStreamManager eventStreamManager = eventStreamManagerFactory.eventStreamManager(eventRepository);

        return new SnapshotAwareEventSource(eventStreamManager, eventRepository, snapshotService);
    }

    /**
     * Support for Named EventSource injection points.  Annotate injection point with
     * {@code @EventSourceName("name")}
     *
     * @param injectionPoint the injection point for the EventSource
     * @return {@link EventSource}
     */
    @Produces
    @EventSourceName
    public EventSource eventSource(final InjectionPoint injectionPoint) {

        final String eventSourceName = eventSourceNameExtractor.getEventSourceNameFromQualifier(injectionPoint);

        if (DEFAULT_EVENT_SOURCE_NAME.equals(eventSourceName)) {
            return eventSource();
        } else {
            final Optional<uk.gov.justice.subscription.domain.eventsource.EventSource> eventSourceFor = eventSourceRegistry.getEventSourceFor(eventSourceName);
            return eventSourceFor
                    .map(eventSource -> eventSource())
                    .orElseThrow(() -> new UnsatisfiedResolutionException("Use of non default EventSources not yet implemented"));
        }
    }
}

