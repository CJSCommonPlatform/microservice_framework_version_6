package uk.gov.justice.services.eventsourcing.source.core;

import uk.gov.justice.services.messaging.cdi.UnmanagedBeanCreator;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class EventStreamManagerProducer {

    @Inject
    UnmanagedBeanCreator unmanagedBeanCreator;

    @Produces
    public EventStreamManager eventStreamManager() {
        return unmanagedBeanCreator.create(EventStreamManager.class);
    }
}
