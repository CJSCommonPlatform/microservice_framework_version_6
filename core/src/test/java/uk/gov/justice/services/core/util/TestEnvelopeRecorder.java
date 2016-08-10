package uk.gov.justice.services.core.util;


import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.LinkedList;
import java.util.List;

/**
 * To be used in unit & integration tests for testing invocations of handlers, senders etc..
 */
public abstract class TestEnvelopeRecorder {
    private final List<JsonEnvelope> recordedEnvelopes = new LinkedList<>();

    public void reset() {
        recordedEnvelopes.clear();
    }

    public JsonEnvelope firstRecordedEnvelope() {
        return !recordedEnvelopes.isEmpty() ? recordedEnvelopes.get(0) : null;
    }

    public List<JsonEnvelope> recordedEnvelopes() {
        return recordedEnvelopes;
    }

    protected void record(final JsonEnvelope envelope) {
        recordedEnvelopes.add(envelope);
    }

}
