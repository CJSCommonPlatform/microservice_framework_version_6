package uk.gov.justice.services.example.cakeshop.command.handler;

import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.eventsourcing.source.core.Events.streamOf;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.slf4j.Logger;

@ServiceComponent(COMMAND_HANDLER)
public class OrderCakeCommandHandler {

    private static final Logger LOGGER = getLogger(OrderCakeCommandHandler.class);
    private static final String FIELD_STREAM_ID = "orderId";

    @Inject
    EventSource eventSource;

    @Inject
    EventFactory eventFactory;

    @Inject
    Enveloper enveloper;

    @Handles("cakeshop.order-cake")
    public void handle(final JsonEnvelope command) throws EventStreamException {

        LOGGER.info("=============> Inside order-cake Command Handler");

        final UUID streamId = UUID.fromString(command.payloadAsJsonObject().getString(FIELD_STREAM_ID));

        final Stream<Object> events = streamOf(eventFactory.cakeOrderedEventFrom(command));

        eventSource.getStreamById(streamId).append(events.map(enveloper.withMetadataFrom(command)));

    }

}
