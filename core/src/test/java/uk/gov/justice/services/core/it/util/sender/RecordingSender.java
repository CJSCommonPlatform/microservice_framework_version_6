package uk.gov.justice.services.core.it.util.sender;


import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.core.util.TestEnvelopeRecorder;
import uk.gov.justice.services.messaging.JsonEnvelope;


public class RecordingSender extends TestEnvelopeRecorder implements Sender {

    private static final RecordingSender INSTANCE = new RecordingSender();
    public static final RecordingSender instance() {
        return INSTANCE;
    }

    @Override
    public void send(final JsonEnvelope envelope) {
        record(envelope);
    }
}
