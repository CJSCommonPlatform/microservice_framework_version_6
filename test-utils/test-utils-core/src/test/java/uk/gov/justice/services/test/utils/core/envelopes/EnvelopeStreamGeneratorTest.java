package uk.gov.justice.services.test.utils.core.envelopes;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;
import static uk.gov.justice.services.test.utils.core.envelopes.EnvelopeStreamGenerator.envelopeStreamGenerator;
import static uk.gov.justice.services.test.utils.core.envelopes.StreamDefBuilder.aStream;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;

public class EnvelopeStreamGeneratorTest {

    @Test
    public void shouldGenerateEnvelopesInRandomStreamsWithRandomSizesInCorrectPositionOrder() throws Exception {

        final UUID streamId_1 = randomUUID();
        final UUID streamId_2 = randomUUID();
        final UUID streamId_3 = randomUUID();
        final UUID streamId_4 = randomUUID();

        final int sizeOfStream_1 = 10;
        final int sizeOfStream_2 = 5;
        final int sizeOfStream_3 = 40;
        final int sizeOfStream_4 = 13;

        final List<JsonEnvelope> jsonEnvelopes = envelopeStreamGenerator()
                .withStartingPosition(1L)
                .withStreamOf(aStream()
                        .withStreamId(streamId_1)
                        .withStreamSize(sizeOfStream_1)
                        .withEnvelopeCreator(JsonEnvelopeGenerator_1::new)
                        .withEnvelopeCreator(JsonEnvelopeGenerator_2::new)
                        .withEnvelopeCreator(JsonEnvelopeGenerator_3::new)
                        .withEnvelopeCreator(JsonEnvelopeGenerator_4::new)
                )
                .withStartingPosition(1L)
                .withStreamOf(aStream()
                        .withStreamId(streamId_2)
                        .withStreamSize(sizeOfStream_2)
                        .withEnvelopeCreator(JsonEnvelopeGenerator_2::new)
                        .withEnvelopeCreator(JsonEnvelopeGenerator_4::new)
                )
                .withStartingPosition(1L)
                .withStreamOf(aStream()
                        .withStreamId(streamId_3)
                        .withStreamSize(sizeOfStream_3)
                        .withEnvelopeCreator(JsonEnvelopeGenerator_3::new)
                )
                .withStartingPosition(1L)
                .withStreamOf(aStream()
                        .withStreamId(streamId_4)
                        .withEnvelopeCreator(JsonEnvelopeGenerator_4::new)
                        .withStreamSize(sizeOfStream_4)
                )
                .generateAll();


        final int expectedNumberOdEnvelopes =
                sizeOfStream_1 +
                        sizeOfStream_2 +
                        sizeOfStream_3 +
                        sizeOfStream_4;

        assertThat(jsonEnvelopes.size(), is(expectedNumberOdEnvelopes));

        checkPositionsAreContiguousForStreams(jsonEnvelopes);
    }


    private void checkPositionsAreContiguousForStreams(final List<JsonEnvelope> jsonEnvelopes) {

        final Map<UUID, Long> positions = new HashMap<>();

        for(final JsonEnvelope jsonEnvelope: jsonEnvelopes) {
            final Optional<UUID> streamIdOptional = jsonEnvelope.metadata().streamId();

            if(streamIdOptional.isPresent()) {

                final UUID streamId = streamIdOptional.get();
                if(! positions.containsKey(streamId)) {
                    positions.put(streamId, 1L);
                }

                final Long position = positions.get(streamId);

                assertThat(position, is(jsonEnvelope.metadata().position().orElse(null)));
                positions.put(streamId, position + 1);

            } else {
                fail();
            }
        }

    }

    private static class JsonEnvelopeGenerator_1 implements JsonEnvelopeGenerator {

        private static final String COMMAND = "context.command.command_1";

        @Override
        public JsonEnvelope generate(final UUID streamId, final Long position) {
            final UUID id = randomUUID();

            return envelopeFrom(
                    metadataBuilder()
                            .withId(id)
                            .withName(COMMAND)
                            .withVersion(position)
                            .withStreamId(streamId),
                    createObjectBuilder()
                            .add("someProperty", "value_" + position)
            );
        }
    }

    private static class JsonEnvelopeGenerator_2 implements JsonEnvelopeGenerator {

        private static final String COMMAND = "context.command.command_2";

        @Override
        public JsonEnvelope generate(final UUID streamId, final Long position) {
            final UUID id = randomUUID();

            return envelopeFrom(
                    metadataBuilder()
                            .withId(id)
                            .withName(COMMAND)
                            .withVersion(position)
                            .withStreamId(streamId),
                    createObjectBuilder()
                            .add("someProperty", "value_" + position)
            );
        }
    }

    private static class JsonEnvelopeGenerator_3 implements JsonEnvelopeGenerator {

        private static final String COMMAND = "context.command.command_3";

        @Override
        public JsonEnvelope generate(final UUID streamId, final Long position) {
            final UUID id = randomUUID();

            return envelopeFrom(
                    metadataBuilder()
                            .withId(id)
                            .withName(COMMAND)
                            .withVersion(position)
                            .withStreamId(streamId),
                    createObjectBuilder()
                            .add("someProperty", "value_" + position)
            );
        }
    }

    private static class JsonEnvelopeGenerator_4 implements JsonEnvelopeGenerator {

        private static final String COMMAND = "context.command.command_4";

        @Override
        public JsonEnvelope generate(final UUID streamId, final Long position) {
            final UUID id = randomUUID();

            return envelopeFrom(
                    metadataBuilder()
                            .withId(id)
                            .withName(COMMAND)
                            .withVersion(position)
                            .withStreamId(streamId),
                    createObjectBuilder()
                            .add("someProperty", "value_" + position)
            );
        }
    }
}
