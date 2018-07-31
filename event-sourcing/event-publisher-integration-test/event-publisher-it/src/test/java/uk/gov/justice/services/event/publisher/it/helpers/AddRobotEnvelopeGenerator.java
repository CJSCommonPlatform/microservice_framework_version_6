package uk.gov.justice.services.event.publisher.it.helpers;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.envelopes.JsonEnvelopeGenerator;

import java.util.UUID;

public class AddRobotEnvelopeGenerator implements JsonEnvelopeGenerator {

    private static final String ADD_ROBOT_COMMAND = "publish.command.add-robot";

    @Override
    public JsonEnvelope generate(final UUID robotId, final Long position) {
        final UUID id = randomUUID();

        return envelopeFrom(
                metadataBuilder()
                        .withId(id)
                        .withName(ADD_ROBOT_COMMAND)
                        .withStreamId(robotId),
                createObjectBuilder()
                        .add("robotId", robotId.toString())
                        .add("robotType", "Protection Droid " + position)
                        .add("isEvil", true)
                        .add("brainTheSizeOfAPlanet", true)
        );
    }
}
