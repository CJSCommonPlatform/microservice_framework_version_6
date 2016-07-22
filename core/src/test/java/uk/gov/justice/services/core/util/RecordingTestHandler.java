package uk.gov.justice.services.core.util;


import uk.gov.justice.services.messaging.JsonEnvelope;

public class RecordingTestHandler {
    private JsonEnvelope recordedEnvelope;

    public void reset() {
        recordedEnvelope = null;
    }

    public JsonEnvelope recordedEnvelope() {
        return recordedEnvelope;
    }

    public void doHandle(JsonEnvelope envelope) {
        recordedEnvelope = envelope;
    }

}
