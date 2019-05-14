package uk.gov.justice.services.messaging.jms;

import uk.gov.justice.services.messaging.JsonEnvelope;

public interface EnvelopeSender {
    void send(final JsonEnvelope command, final String destinationName);
}
