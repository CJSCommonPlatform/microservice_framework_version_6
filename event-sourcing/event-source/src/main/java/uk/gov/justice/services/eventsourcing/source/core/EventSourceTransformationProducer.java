package uk.gov.justice.services.eventsourcing.source.core;

import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepositoryFactory;
import uk.gov.justice.services.jdbc.persistence.JndiDataSourceNameProvider;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class EventSourceTransformationProducer {

    @Inject
    EventStreamManagerFactory eventStreamManagerFactory;

    @Inject
    EventRepositoryFactory eventRepositoryFactory;

    @Inject
    EventJdbcRepositoryFactory eventJdbcRepositoryFactory;

    @Inject
    EventStreamJdbcRepositoryFactory eventStreamJdbcRepositoryFactory;

    @Inject
    JndiDataSourceNameProvider dataSourceJndiNameProvider;

    @Produces
    public EventSourceTransformation eventSourceTransformation() {

        final String jndiDatasource = dataSourceJndiNameProvider.jndiName();
        final EventJdbcRepository eventJdbcRepository = eventJdbcRepositoryFactory.eventJdbcRepository(jndiDatasource);
        final EventStreamJdbcRepository eventStreamJdbcRepository = eventStreamJdbcRepositoryFactory.eventStreamJdbcRepository(jndiDatasource);

        final EventRepository eventRepository = eventRepositoryFactory.eventRepository(
                eventJdbcRepository,
                eventStreamJdbcRepository);

        final EventStreamManager eventStreamManager = eventStreamManagerFactory.eventStreamManager(eventRepository);

        return new DefaultEventSourceTransformation(eventStreamManager);
    }
}
