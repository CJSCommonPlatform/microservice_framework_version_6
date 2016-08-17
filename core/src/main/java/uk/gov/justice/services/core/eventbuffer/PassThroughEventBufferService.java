package uk.gov.justice.services.core.eventbuffer;

import uk.gov.justice.services.event.buffer.api.EventBufferService;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.stream.Stream;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import org.slf4j.Logger;

/**
 * Basic implementation of the EventBufferService passing the envelope through without any buffering
 */
@ApplicationScoped
@Alternative
@Priority(1)
public class PassThroughEventBufferService implements EventBufferService {

    @Inject
    Logger logger;

    @Override
    public Stream<JsonEnvelope> currentOrderedEventsWith(final JsonEnvelope jsonEnvelope) {
        logger.trace("Message: {} passing through to dispatcher", jsonEnvelope);
        return Stream.of(jsonEnvelope);
    }
}
