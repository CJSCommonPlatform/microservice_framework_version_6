package uk.gov.justice.services.eventsourcing.source.core;

import static uk.gov.justice.services.core.cdi.EventSourceName.DEFAULT_EVENT_SOURCE_NAME;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Unmanaged;
import javax.inject.Inject;

@ApplicationScoped
public class EventSourceProducer {

    @Inject
    EventSourceNameExtractor eventSourceNameExtractor;

    @Produces
    public EventSource eventSource(final InjectionPoint injectionPoint) {

        final String eventSourceName = eventSourceNameExtractor.getEventSourceNameFromQualifier(injectionPoint);

        if (DEFAULT_EVENT_SOURCE_NAME.equals(eventSourceName)) {
            return create(DefaultEventSource.class);
        }

        throw new UnsatisfiedResolutionException("Use of non default EventSources not yet implemented");
    }


    @SuppressWarnings("unchecked")
    EventSource create(final Class<? extends EventSource> eventSourceClass) {

        final BeanManager beanManager = CDI.current().getBeanManager();
        final Unmanaged<EventSource> unmanaged = new Unmanaged(beanManager, eventSourceClass);
        final Unmanaged.UnmanagedInstance<EventSource> unmanagedInstance = unmanaged.newInstance();

        return unmanagedInstance.produce().inject().get();
    }
}
