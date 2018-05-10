package uk.gov.justice.services.eventsourcing.source.core;

import uk.gov.justice.services.common.configuration.GlobalValue;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

@ApplicationScoped
public class EventStreamManagerFactory {

    @Inject
    @GlobalValue(key = "internal.max.retry", defaultValue = "20")
    long maxRetry;

    @Inject
    Logger logger;

    @Inject
    SystemEventService systemEventService;

    @Inject
    Enveloper enveloper;

    @Inject
    PublishingEventAppenderFactory publishingEventAppenderFactory;

    public EventStreamManager eventStreamManager(final EventRepository eventRepository) {

        final EventAppender publishingEventAppender = publishingEventAppenderFactory.publishingEventAppender(eventRepository);

        return new EventStreamManager(
                publishingEventAppender,
                maxRetry,
                systemEventService,
                enveloper,
                eventRepository,
                logger
        );

    }
}
