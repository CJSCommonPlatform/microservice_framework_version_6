package uk.gov.justice.services.eventsourcing.source.core;

import static uk.gov.justice.services.eventsourcing.source.core.annotation.EventSourceName.DEFAULT_EVENT_SOURCE_NAME;

import uk.gov.justice.services.eventsourcing.source.core.annotation.EventSourceName;
import uk.gov.justice.subscription.registry.EventSourceRegistry;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Unmanaged;
import javax.inject.Inject;

/**
 * Producer for EventSource, backwards compatible supports Named and Unnamed EventSource injection points
 */
@ApplicationScoped
public class EventSourceProducer {

    @Inject
    EventSourceNameExtractor eventSourceNameExtractor;

    @Inject
    EventSourceRegistry eventSourceRegistry;

    /**
     * Backwards compatible support for Unnamed EventSource injection points
     *
     * @return {@link EventSource}
     */
    @Produces
    public EventSource eventSource() {
        return create(JdbcBasedEventSource.class);
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

    @SuppressWarnings("unchecked")
    private EventSource create(final Class<? extends EventSource> eventSourceClass) {

        final BeanManager beanManager = CDI.current().getBeanManager();
        final Unmanaged<EventSource> unmanaged = new Unmanaged(beanManager, eventSourceClass);
        final Unmanaged.UnmanagedInstance<EventSource> unmanagedInstance = unmanaged.newInstance();

        return unmanagedInstance.produce().inject().get();
    }
}
