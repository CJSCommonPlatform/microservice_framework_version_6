package uk.gov.justice.services.eventsourcing.source.core;

import uk.gov.justice.services.eventsourcing.publisher.jms.EventPublisher;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class PublishingEventAppenderFactory {

    @Inject
    EventPublisher eventPublisher;

    public PublishingEventAppender publishingEventAppender(final EventRepository eventRepository) {
        return new PublishingEventAppender(eventRepository, eventPublisher);
    }
}
