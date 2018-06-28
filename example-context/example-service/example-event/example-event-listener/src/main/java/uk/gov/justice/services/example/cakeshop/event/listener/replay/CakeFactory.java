package uk.gov.justice.services.example.cakeshop.event.listener.replay;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;

public class CakeFactory {

    private final int numberOfStreams;

    private final Random random = new Random();
    private final Map<UUID, List<JsonEnvelope>> eventsByStream = new HashMap<>();

    public CakeFactory(final int numberOfStreams) {
        this.numberOfStreams = numberOfStreams;
    }

    public List<JsonEnvelope> generateEvents(final int numberOfEventsToCreate) {

        final List<UUID> streamIds = generateStreamIds();

        streamIds.forEach(streamId -> eventsByStream.put(streamId, new ArrayList<>()));

        return IntStream.range(0, numberOfEventsToCreate)
                .mapToObj(index -> generateEnvelope(streamIds))
                .collect(toList());
    }

    private List<UUID> generateStreamIds() {
        return range(0, numberOfStreams)
                .mapToObj(index -> randomUUID())
                .collect(toList());
    }

    private UUID getARandomStreamId(final List<UUID> streamIds) {

        final int index = random.nextInt(streamIds.size());

        return streamIds.get(index);
    }


    private JsonEnvelope generateEnvelope(final List<UUID> streamIds) {

        final UUID streamId = getARandomStreamId(streamIds);

        final List<JsonEnvelope> jsonEnvelopesByStream = eventsByStream.get(streamId);
        final int version = jsonEnvelopesByStream.size() + 1;

        final JsonEnvelope jsonEnvelope = generateJsonEnvelope(streamId, version);

        jsonEnvelopesByStream.add(jsonEnvelope);

        return jsonEnvelope;
    }

    private JsonEnvelope generateJsonEnvelope(final UUID streamId, final long version) {

        if (version == 1L) {
            return envelopeFrom(
                    metadataBuilder()
                            .withId(randomUUID())
                            .withName("example.recipe-added")
                            .withStreamId(streamId)
                            .withVersion(version)
                            .withSource("example"),
                    createObjectBuilder()
                            .add("recipeId", streamId.toString())
                            .add("name", "Carrot Cake")
                            .add("glutenFree", false)
                            .add("ingredients", createArrayBuilder()
                                    .add(createObjectBuilder()
                                            .add("name", "carrot")
                                            .add("quantity", 1)
                                    ).build())
                            .build()
            );
        }

        return envelopeFrom(
                metadataBuilder()
                        .withId(randomUUID())
                        .withName("example.recipe-renamed")
                        .withStreamId(streamId)
                        .withVersion(version)
                        .withSource("example"),
                createObjectBuilder()
                        .add("recipeId", streamId.toString())
                        .add("name", "Not Carrot Cake")
                        .build()
        );
    }
}

