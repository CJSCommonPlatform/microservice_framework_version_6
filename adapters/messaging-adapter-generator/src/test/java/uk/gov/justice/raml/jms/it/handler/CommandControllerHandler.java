package uk.gov.justice.raml.jms.it.handler;

import javax.ejb.Singleton;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.Envelope;

@ServiceComponent(Component.COMMAND_CONTROLLER)
@Singleton
public class CommandControllerHandler extends BasicHandler {
   
    @Handles("structure.commands.commanda")
    public void handleCommandA(final Envelope envelope) {
        receivedEnvelope = envelope;
    }

    @Handles("structure.commands.commandb")
    public void handleCommandB(final Envelope envelope) {
        receivedEnvelope = envelope;
    }

    @Handles("structure.commands.commandc")
    public void handleCommandC(final Envelope envelope) {
        receivedEnvelope = envelope;
    }
    
   }
