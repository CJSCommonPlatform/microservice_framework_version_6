package uk.gov.justice.services.example.cakeshop.command.handler;

import uk.gov.justice.services.messaging.DefaultEnvelope;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.Metadata;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Stream;

import static uk.gov.justice.services.messaging.JsonObjectMetadata.ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.NAME;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataFrom;

public class TemporaryEventUtil {

    private static final String RECIPE_ADDED_EVENT = "cakeshop.events.recipe-added";

    public Stream<Envelope> eventsFrom(final Envelope command) {

        final JsonObject jsonObject = Json.createObjectBuilder()
                .add(ID, UUID.randomUUID().toString())
                .add(NAME, RECIPE_ADDED_EVENT)
                .build();
        final Metadata metadata = metadataFrom(jsonObject);

        return Collections.singletonList(DefaultEnvelope.envelopeFrom(metadata, command.payload())).stream();
    }

}
