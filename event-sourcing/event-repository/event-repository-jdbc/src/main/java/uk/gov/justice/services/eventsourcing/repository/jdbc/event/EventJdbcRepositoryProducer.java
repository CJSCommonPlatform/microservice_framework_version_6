package uk.gov.justice.services.eventsourcing.repository.jdbc.event;

import uk.gov.justice.services.messaging.cdi.UnmanagedBeanCreator;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class EventJdbcRepositoryProducer {

    @Inject
    UnmanagedBeanCreator unmanagedBeanCreator;

    @Produces
    public EventJdbcRepository eventJdbcRepository() {
        return unmanagedBeanCreator.create(EventJdbcRepository.class);
    }
}
