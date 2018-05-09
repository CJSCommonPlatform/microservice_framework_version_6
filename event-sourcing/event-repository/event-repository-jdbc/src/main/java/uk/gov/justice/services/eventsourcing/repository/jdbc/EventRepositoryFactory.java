package uk.gov.justice.services.eventsourcing.repository.jdbc;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.LoggerFactory;

@ApplicationScoped
public class EventRepositoryFactory {

    @Inject
    EventConverter eventConverter;

    public EventRepository eventRepository(
            final EventJdbcRepository eventJdbcRepository,
            final EventStreamJdbcRepository eventStreamJdbcRepository) {

        return new JdbcBasedEventRepository(
                eventConverter,
                eventJdbcRepository,
                eventStreamJdbcRepository,
                LoggerFactory.getLogger(EventRepository.class)
        );
    }
}
