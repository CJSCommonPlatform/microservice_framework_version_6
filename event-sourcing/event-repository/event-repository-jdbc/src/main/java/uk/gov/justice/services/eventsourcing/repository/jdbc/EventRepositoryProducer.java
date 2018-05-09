package uk.gov.justice.services.eventsourcing.repository.jdbc;

import uk.gov.justice.services.messaging.cdi.UnmanagedBeanCreator;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class EventRepositoryProducer {

    @Inject
    UnmanagedBeanCreator unmanagedBeanCreator;

    @Produces
    public EventRepository eventRepository() {
        return unmanagedBeanCreator.create(JdbcBasedEventRepository.class);
    }
}
