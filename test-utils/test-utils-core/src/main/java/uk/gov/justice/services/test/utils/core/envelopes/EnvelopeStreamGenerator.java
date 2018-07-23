package uk.gov.justice.services.test.utils.core.envelopes;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Utility for creating JsonEnvelopes in many streams for performance testing.
 *
 * To Use:
 *
 * <pre>
 *     <blockquote>
 *
 *         import static uk.gov.justice.services.test.utils.core.envelopes.EnvelopeStreamGenerator.envelopeStreamGenerator;
 *         import static uk.gov.justice.services.test.utils.core.envelopes.StreamDefBuilder.aStream;
 *
 *         final JsonEnvelopeGenerator myJsonEnvelopeGenerator_1 = new MyJsonEnvelopeGenerator_1();
 *         final JsonEnvelopeGenerator myJsonEnvelopeGenerator_2 = new MyJsonEnvelopeGenerator_2();
 *         final JsonEnvelopeGenerator myJsonEnvelopeGenerator_3 = new MyJsonEnvelopeGenerator_3();
 *
 *         final int streamSize = 42;
 *         final UUID streamId_1 = randomUUID();
 *         final UUID streamId_2 = randomUUID();
 *
 *
 *         final List<JsonEnvelope> envelopes = envelopeStreamGenerator()
 *         .withStreamOf(aStream()
 *                     .withStreamId(streamId_1)
 *                     .withStreamSize(streamSize)
 *                     .withEnvelopeCreator(() -> myJsonEnvelopeGenerator_1)
 *                     .withEnvelopeCreator(() -> myJsonEnvelopeGenerator_2)
 *         )
 *         .withStreamOf(aStream()
 *                        .withStreamId(streamId_2)
 *                        .withStreamSize(streamSize)
 *                        .withEnvelopeCreator(() -> myJsonEnvelopeGenerator_3)
 *         ).generateAll();
 *
 *     </blockquote>
 * </pre>
 */
public class EnvelopeStreamGenerator {


    private final Map<UUID, StreamDef> streamDefs = new HashMap<>();
    private final Map<UUID, Long> positionCounter = new HashMap<>();
    private final List<UUID> streamIds = new ArrayList<>();

    private final Random random = new Random();
    private long startingPosition = 1L;

    private EnvelopeStreamGenerator() {}

    public static EnvelopeStreamGenerator envelopeStreamGenerator() {
        return new EnvelopeStreamGenerator();
    }

    public EnvelopeStreamGenerator withStreamOf(final StreamDefBuilder streamDefBuilder) {
        final StreamDef streamDef = streamDefBuilder.build();
        final UUID streamId = streamDef.getStreamId();
        streamDefs.put(streamId, streamDef);
        positionCounter.put(streamId, startingPosition);
        streamIds.add(streamId);
        return this;
    }

    public EnvelopeStreamGenerator withStartingPosition(final long startingPosition) {
        this.startingPosition = startingPosition;
        return this;
    }

    public List<JsonEnvelope> generateAll() {

        final ArrayList<JsonEnvelope> jsonEnvelopes = new ArrayList<>();

        while (! streamDefs.isEmpty()) {

            final UUID streamId = getRandom(streamIds);
            final StreamDef streamDef = streamDefs.get(streamId);

            final List<JsonEnvelopeGenerator> jsonEnvelopeGenerators = streamDef.getJsonEnvelopeGenerators();
            final JsonEnvelopeGenerator jsonEnvelopeGenerator = getRandom(jsonEnvelopeGenerators);

            final long position = positionCounter.get(streamId);

            final JsonEnvelope jsonEnvelope = jsonEnvelopeGenerator.generate(
                    streamId,
                    position);
            jsonEnvelopes.add(jsonEnvelope);

            positionCounter.put(streamId, position + 1);

            if(position == streamDef.getStreamSize()) {
                streamDefs.remove(streamId);
                streamIds.remove(streamId);
            }
        }

        return jsonEnvelopes;
    }

    private <T> T getRandom(final List<T> list) {
        return list.get(random.nextInt(list.size()));
    }
}
