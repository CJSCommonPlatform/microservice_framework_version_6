package uk.gov.justice.services.example.cakeshop.command.handler;

import org.slf4j.Logger;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.Envelope;

import javax.inject.Inject;

import java.util.UUID;
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

@ServiceComponent(COMMAND_HANDLER)
public class MakeCakeCommandHandler {

    private static final Logger LOGGER = getLogger(MakeCakeCommandHandler.class);
    private static final String FIELD_CAKE_ID = "cakeId";
    private static final String CAKE_MADE_EVENT = "cakeshop.events.cake-made";

    @Inject
    EventSource eventSource;

    @Inject
    TemporaryEventUtil temporaryEventUtil;

    @Handles("cakeshop.commands.make-cake")
    public void handle(final Envelope command) throws EventStreamException {
        LOGGER.info("=============> Inside make-cake Command Handler");
        final UUID cakeId = UUID.fromString(command.payload().getString(FIELD_CAKE_ID));

        final EventStream eventStream = eventSource.getStreamById(cakeId);
        final Stream<Envelope> events = temporaryEventUtil.eventsFrom(command, CAKE_MADE_EVENT);
        eventStream.append(events);
    }

}
