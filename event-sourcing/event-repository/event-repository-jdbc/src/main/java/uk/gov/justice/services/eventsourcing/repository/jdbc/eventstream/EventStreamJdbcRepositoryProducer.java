package uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream;

import uk.gov.justice.services.messaging.cdi.UnmanagedBeanCreator;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class EventStreamJdbcRepositoryProducer {

    @Inject
    UnmanagedBeanCreator unmanagedBeanCreator;

    @Produces
    public EventStreamJdbcRepository eventStreamJdbcRepository() {
        return unmanagedBeanCreator.create(EventStreamJdbcRepository.class);
    }
}
