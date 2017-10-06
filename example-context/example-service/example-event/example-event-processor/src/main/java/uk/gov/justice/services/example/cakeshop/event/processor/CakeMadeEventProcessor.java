package uk.gov.justice.services.example.cakeshop.event.processor;

import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_PROCESSOR)
public class CakeMadeEventProcessor {

    private static final Logger logger = LoggerFactory.getLogger(CakeMadeEventProcessor.class);

    @Inject
    Sender sender;

    @Handles("example.events.cake-made")
    public void handle(final JsonEnvelope event) {

        logger.info("=============> Inside cake-made Event Processor");
        sender.send(event);

    }
}
