package uk.gov.justice.services.eventsourcing.source.core;

import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepositoryFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class JdbcEventSourceFactory {

    @Inject
    EventStreamManagerFactory eventStreamManagerFactory;

    @Inject
    EventRepositoryFactory eventRepositoryFactory;

    @Inject
    EventJdbcRepositoryFactory eventJdbcRepositoryFactory;

    @Inject
    EventStreamJdbcRepositoryFactory eventStreamJdbcRepositoryFactory;

    public JdbcBasedEventSource create(final String jndiDatasource, String eventSourceName) {

        final EventJdbcRepository eventJdbcRepository = eventJdbcRepositoryFactory.eventJdbcRepository(jndiDatasource);
        final EventStreamJdbcRepository eventStreamJdbcRepository = eventStreamJdbcRepositoryFactory.eventStreamJdbcRepository(jndiDatasource);

        final EventRepository eventRepository = eventRepositoryFactory.eventRepository(
                eventJdbcRepository,
                eventStreamJdbcRepository);

        final EventStreamManager eventStreamManager = eventStreamManagerFactory.eventStreamManager(eventRepository);

        return new JdbcBasedEventSource(eventStreamManager, eventRepository , eventSourceName);
    }
}
