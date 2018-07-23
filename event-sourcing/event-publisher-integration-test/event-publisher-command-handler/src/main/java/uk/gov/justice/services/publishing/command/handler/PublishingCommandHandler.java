package uk.gov.justice.services.publishing.command.handler;

import static java.util.UUID.fromString;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.core.enveloper.Enveloper.toEnvelopeWithMetadataFrom;
import static uk.gov.justice.services.eventsourcing.source.core.Events.streamOf;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.json.JsonObject;

@ServiceComponent(COMMAND_HANDLER)
public class PublishingCommandHandler {

    @Inject
    EventSource eventSource;

    @Handles("publish.command.add-robot")
    public void addRobot(final JsonEnvelope command) throws EventStreamException {

        final JsonObject payload = command.payloadAsJsonObject();

        final UUID robotId = fromString(payload.getString("robotId"));
        final String robotType = payload.getString("robotType");
        final boolean evil = payload.getBoolean("isEvil");
        final boolean brainTheSizeOfAPlanet = payload.getBoolean("brainTheSizeOfAPlanet");

        final Stream<Object> events = streamOf(new RobotAdded(
                robotId,
                robotType,
                evil,
                brainTheSizeOfAPlanet
        ));

        eventSource.getStreamById(robotId).append(events.map(toEnvelopeWithMetadataFrom(command)));
    }
}
