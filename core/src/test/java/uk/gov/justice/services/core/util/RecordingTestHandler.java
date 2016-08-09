package uk.gov.justice.services.core.util;


import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.ArrayList;
import java.util.List;

public class RecordingTestHandler {
    private List<JsonEnvelope> recordedEnvelopes = new ArrayList<>();

    public void reset() {
        recordedEnvelopes = new ArrayList<>();
    }

    public JsonEnvelope recordedEnvelope() {

        return recordedEnvelopes.size() > 0 ? recordedEnvelopes.get(0) : null;
    }

    public List<JsonEnvelope> recordedEnvelopes() {
        return recordedEnvelopes;
    }

    public void doHandle(JsonEnvelope envelope) {
        recordedEnvelopes.add(envelope);
    }

}
