package uk.gov.justice.services.test.utils.core.envelopes;

import java.util.List;
import java.util.UUID;

public class StreamDef {

    private final UUID streamId;
    private final List<JsonEnvelopeGenerator> jsonEnvelopeGenerators;
    private final int streamSize;

    public StreamDef(final UUID streamId, final List<JsonEnvelopeGenerator> jsonEnvelopeGenerators, final int streamSize) {
        this.streamId = streamId;
        this.jsonEnvelopeGenerators = jsonEnvelopeGenerators;
        this.streamSize = streamSize;
    }

    public UUID getStreamId() {
        return streamId;
    }

    public List<JsonEnvelopeGenerator> getJsonEnvelopeGenerators() {
        return jsonEnvelopeGenerators;
    }

    public int getStreamSize() {
        return streamSize;
    }
}
