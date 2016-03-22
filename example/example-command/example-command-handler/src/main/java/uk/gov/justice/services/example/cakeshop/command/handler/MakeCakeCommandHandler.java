package uk.gov.justice.services.example.cakeshop.command.handler;

import org.slf4j.Logger;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.Envelope;

import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

@ServiceComponent(COMMAND_HANDLER)
public class MakeCakeCommandHandler {

    private static final Logger LOGGER = getLogger(MakeCakeCommandHandler.class);

    @Handles("cakeshop.commands.make-cake")
    public void handle(final Envelope envelope) throws EventStreamException {
        LOGGER.info("=============> Inside make-cake Command Handler");
    }

}
