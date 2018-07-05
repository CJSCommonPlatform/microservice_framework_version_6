package uk.gov.justice.services.eventsourcing.source.core;

import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepository;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PublishingEventAppenderFactory {

    public PublishingEventAppender publishingEventAppender(final EventRepository eventRepository) {
        return new PublishingEventAppender(eventRepository);
    }
}
