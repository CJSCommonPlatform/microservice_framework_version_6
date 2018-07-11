package uk.gov.justice.services.example.cakeshop.event.processor;

import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

@ServiceComponent(EVENT_PROCESSOR)
public class CakeMadeEventProcessor {

    @Inject
    Sender sender;

    @Handles("example.cake-made")
    public void handle(final JsonEnvelope event) {
        sender.send(event);
    }
}
