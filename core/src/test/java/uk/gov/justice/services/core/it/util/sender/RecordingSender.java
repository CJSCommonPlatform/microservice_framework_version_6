package uk.gov.justice.services.core.it.util.sender;


import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.core.util.TestEnvelopeRecorder;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RecordingSender extends TestEnvelopeRecorder implements Sender {

    @Override
    public void send(final JsonEnvelope envelope) {
        record(envelope);
    }
}
