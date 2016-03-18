package uk.gov.justice.raml.jms.it.handler;

import javax.ejb.Singleton;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.Envelope;

@ServiceComponent(Component.EVENT_LISTENER)
@Singleton
public class EventListenerHandler extends BasicHandler {
    
    @Handles("structure.events.eventaa")
    public void handleEventA(final Envelope envelope) {
        receivedEnvelope = envelope;
    }
    
    @Handles("structure.events.eventcc")
    public void handleEventC(final Envelope envelope) {
        receivedEnvelope = envelope;
    }

}
