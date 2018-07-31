package uk.gov.justice.services.test.utils.core.envelopes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class StreamDefBuilder {

    private final List<JsonEnvelopeGenerator> jsonEnvelopeGenerators = new ArrayList<>();

    private UUID streamId;
    private int streamSize;

    private StreamDefBuilder() {
    }

    public static StreamDefBuilder aStream() {
        return new StreamDefBuilder();
    }

    public StreamDefBuilder withStreamId(final UUID streamId) {
        this.streamId = streamId;
        return this;
    }

    public StreamDefBuilder withEnvelopeCreator(final EnvelopeGeneratorProvider envelopeGeneratorProvider) {
        this.jsonEnvelopeGenerators.add(envelopeGeneratorProvider.getGenerator());
        return this;
    }

    public StreamDefBuilder withStreamSize(final int streamSize) {
        this.streamSize = streamSize;
        return this;
    }

    public StreamDef build() {
        return new StreamDef(streamId, jsonEnvelopeGenerators, streamSize);
    }


}
