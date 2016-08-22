package uk.gov.justice.services.core.it.util.sender;


import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.common.envelope.TestEnvelopeRecorder;


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
