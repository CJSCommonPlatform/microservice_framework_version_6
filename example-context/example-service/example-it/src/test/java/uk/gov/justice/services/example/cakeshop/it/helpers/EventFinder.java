package uk.gov.justice.services.example.cakeshop.it.helpers;

import static java.util.stream.Collectors.toList;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;

import java.util.List;
import java.util.stream.Stream;

public class EventFinder {

    private final CakeShopRepositoryManager cakeShopRepositoryManager;

    public EventFinder(final CakeShopRepositoryManager cakeShopRepositoryManager) {
        this.cakeShopRepositoryManager = cakeShopRepositoryManager;
    }

    public List<Event> eventsWithPayloadContaining(final String string) {

        final EventJdbcRepository eventJdbcRepository = cakeShopRepositoryManager.getEventJdbcRepository();
        try (final Stream<Event> events = eventJdbcRepository.findAll().filter(e -> e.getPayload().contains(string))) {
            return events.collect(toList());
        }
    }
}
