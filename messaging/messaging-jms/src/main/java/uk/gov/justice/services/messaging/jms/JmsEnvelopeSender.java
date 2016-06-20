package uk.gov.justice.services.messaging.jms;

import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.jms.Destination;

public interface JmsEnvelopeSender {
    void send(JsonEnvelope envelope, Destination destination);
    void send(JsonEnvelope envelope, String destinationName);
}
