package uk.gov.justice.services.eventsourcing.publishing;

import static java.lang.String.format;
import static javax.transaction.Transactional.TxType.REQUIRES_NEW;

import uk.gov.justice.services.eventsourcing.publisher.jms.EventPublisher;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;

/**
 * The EventDeQueuerAndPublisher class provides a method that returns an event from the EventDeQueuer
 * and publishes the event.
 */
public class EventDeQueuerAndPublisher {

    @Inject
    EventDeQueuer eventDeQueuer;

    @Inject
    EventPublisher eventPublisher;

    @Inject
    EventConverter eventConverter;

    @Inject
    Logger logger;

    /**
     * Method that gets the next event to process from the EventDeQueuer,
     * converts the event to a JsonEnvelope type with the EventConverter
     * and then publishes the converted event with the EventPublisher.
     *
     * @return boolean
     */
    @Transactional(REQUIRES_NEW)
    public boolean deQueueAndPublish() {

        final Optional<Event> event = eventDeQueuer.popNextEvent();
        if (event.isPresent()) {
            logger.debug("Publishing event {}", event.get().getName());
            final JsonEnvelope jsonEnvelope = eventConverter.envelopeOf(event.get());
            eventPublisher.publish(jsonEnvelope);

            return true;
        }
        return false;
    }
}
